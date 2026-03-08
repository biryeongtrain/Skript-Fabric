package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.parser.ParserInstance;
import java.lang.reflect.Field;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.conditions.CondIsInvulnerable;
import org.skriptlang.skript.bukkit.base.effects.EffInvulnerability;

final class InvulnerableSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void invulnerabilityEffectParsesPositiveAndNegativeSyntax() throws Exception {
        EffInvulnerability positive = parseEffectInEvent("make event-entity invincible", EffInvulnerability.class, FabricUseEntityHandle.class);
        assertTrue(readBoolean(positive, "invulnerable"));
        assertEquals("event-entity", expression(positive, "entities").toString(null, false));

        EffInvulnerability negative = parseEffectInEvent("make event-entity vulnerable", EffInvulnerability.class, FabricUseEntityHandle.class);
        assertFalse(readBoolean(negative, "invulnerable"));
        assertEquals("event-entity", expression(negative, "entities").toString(null, false));
    }

    @Test
    void invulnerableConditionParsesForEntityEvents() {
        CondIsInvulnerable positive = parseConditionInEvent("event-entity is invulnerable", CondIsInvulnerable.class, FabricUseEntityHandle.class);
        assertFalse(positive.isNegated());

        CondIsInvulnerable negative = parseConditionInEvent("event-entity is not invincible", CondIsInvulnerable.class, FabricUseEntityHandle.class);
        assertTrue(negative.isNegated());
    }

    private <T> T parseEffectInEvent(String effect, Class<T> effectClass, Class<?>... eventClasses) throws Exception {
        Statement statement = parseStatementInEvent(effect, eventClasses);
        assertNotNull(statement);
        assertInstanceOf(effectClass, statement);
        return effectClass.cast(statement);
    }

    private <T> T parseConditionInEvent(String condition, Class<T> conditionClass, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            Condition parsed = Condition.parse(condition, null);
            assertNotNull(parsed);
            assertInstanceOf(conditionClass, parsed);
            return conditionClass.cast(parsed);
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private Statement parseStatementInEvent(String statement, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return Statement.parse(statement, "failed");
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private void restoreEventContext(ParserInstance parser, String previousEventName, Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readObject(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Object readObject(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(owner);
    }

    private Field findField(Class<?> owner, String fieldName) throws NoSuchFieldException {
        Class<?> current = owner;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
