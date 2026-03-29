package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.conditions.CondIsSilent;
import org.skriptlang.skript.bukkit.base.effects.EffSilence;

final class SilentSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void silenceEffectParsesPositiveAndNegativeSyntax() throws Exception {
        EffSilence positive = parseEffectInEvent("silence event-entity", EffSilence.class, FabricUseEntityHandle.class);
        assertTrue(readBoolean(positive, "silence"));
        assertEquals("event-entity", expression(positive, "entities").toString(null, false));

        EffSilence negative = parseEffectInEvent("make event-entity not silent", EffSilence.class, FabricUseEntityHandle.class);
        assertFalse(readBoolean(negative, "silence"));
        assertEquals("event-entity", expression(negative, "entities").toString(null, false));
    }

    @Test
    void silentConditionParsesForEntityEvents() {
        CondIsSilent positive = parseConditionInEvent("event-entity is silent", CondIsSilent.class, FabricUseEntityHandle.class);
        assertFalse(positive.isNegated());

        CondIsSilent negative = parseConditionInEvent("event-entity is not silent", CondIsSilent.class, FabricUseEntityHandle.class);
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
