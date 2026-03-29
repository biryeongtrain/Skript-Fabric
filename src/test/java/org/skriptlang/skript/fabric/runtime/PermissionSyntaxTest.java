package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.conditions.CondPermission;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class PermissionSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void permissionConditionParsesExactForms() throws Exception {
        CondPermission positive = parseConditionInEvent("event-player has permission \"skript.tree\"", CondPermission.class, FabricUseEntityHandle.class);
        assertFalse(positive.isNegated());
        assertEquals("event-player", expression(positive, "players").toString(null, false));
        assertEquals("\"skript.tree\"", expression(positive, "permissions").toString(null, false));

        CondPermission negative = parseConditionInEvent(
                "event-player does not have permission \"skript.tree\"",
                CondPermission.class,
                FabricUseEntityHandle.class
        );
        assertTrue(negative.isNegated());
        assertEquals("event-player", expression(negative, "players").toString(null, false));
        assertEquals("\"skript.tree\"", expression(negative, "permissions").toString(null, false));
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
