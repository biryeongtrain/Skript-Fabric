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
import org.skriptlang.skript.bukkit.base.conditions.CondIsInvisible;
import org.skriptlang.skript.bukkit.base.effects.EffInvisible;

final class InvisibleSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void invisibleEffectParsesExactUpstreamForms() throws Exception {
        EffInvisible invisible = parseEffectInEvent("make event-entity invisible", EffInvisible.class, FabricUseEntityHandle.class);
        assertTrue(readBoolean(invisible, "invisible"));
        assertEquals("event-entity", expression(invisible, "livingEntities").toString(null, false));

        EffInvisible notVisible = parseEffectInEvent("make event-entity not visible", EffInvisible.class, FabricUseEntityHandle.class);
        assertTrue(readBoolean(notVisible, "invisible"));
        assertEquals("event-entity", expression(notVisible, "livingEntities").toString(null, false));

        EffInvisible visible = parseEffectInEvent("make event-entity visible", EffInvisible.class, FabricUseEntityHandle.class);
        assertFalse(readBoolean(visible, "invisible"));
        assertEquals("event-entity", expression(visible, "livingEntities").toString(null, false));

        EffInvisible notInvisible = parseEffectInEvent("make event-entity not invisible", EffInvisible.class, FabricUseEntityHandle.class);
        assertFalse(readBoolean(notInvisible, "invisible"));
        assertTrue(expression(notInvisible, "livingEntities").toString(null, false).contains("event-entity"));
    }

    @Test
    void invisibleConditionParsesExactUpstreamForms() throws Exception {
        CondIsInvisible invisible = parseConditionInEvent("event-entity is invisible", CondIsInvisible.class, FabricUseEntityHandle.class);
        assertFalse(invisible.isNegated());
        assertEquals("event-entity", expression(invisible, "livingEntities").toString(null, false));

        CondIsInvisible visible = parseConditionInEvent("event-entity is visible", CondIsInvisible.class, FabricUseEntityHandle.class);
        assertTrue(visible.isNegated());
        assertEquals("event-entity", expression(visible, "livingEntities").toString(null, false));

        CondIsInvisible notInvisible = parseConditionInEvent("event-entity is not invisible", CondIsInvisible.class, FabricUseEntityHandle.class);
        assertTrue(notInvisible.isNegated());
        assertEquals("event-entity", expression(notInvisible, "livingEntities").toString(null, false));

        CondIsInvisible notVisible = parseConditionInEvent("event-entity is not visible", CondIsInvisible.class, FabricUseEntityHandle.class);
        assertFalse(notVisible.isNegated());
        assertEquals("event-entity", expression(notVisible, "livingEntities").toString(null, false));
    }

    private <T> T parseEffectInEvent(String effect, Class<T> effectClass, Class<?>... eventClasses) {
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
