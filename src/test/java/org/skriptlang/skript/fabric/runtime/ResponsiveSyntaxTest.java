package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;

final class ResponsiveSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void responsiveConditionParsesExactUpstreamForms() throws Exception {
        CondIsResponsive responsive = parseConditionInEvent("event-entity is responsive", CondIsResponsive.class, FabricUseEntityHandle.class);
        assertTrue(readBoolean(responsive, "responsive"));
        assertFalse(responsive.isNegated());
        assertEquals("event-entity", expression(responsive, "entities").toString(null, false));

        CondIsResponsive unresponsive = parseConditionInEvent(
                "event-entity is unresponsive",
                CondIsResponsive.class,
                FabricUseEntityHandle.class
        );
        assertFalse(readBoolean(unresponsive, "responsive"));
        assertFalse(unresponsive.isNegated());
        assertEquals("event-entity", expression(unresponsive, "entities").toString(null, false));

        CondIsResponsive notResponsive = parseConditionInEvent(
                "event-entity is not responsive",
                CondIsResponsive.class,
                FabricUseEntityHandle.class
        );
        assertTrue(readBoolean(notResponsive, "responsive"));
        assertTrue(notResponsive.isNegated());
        assertEquals("event-entity", expression(notResponsive, "entities").toString(null, false));

        CondIsResponsive notUnresponsive = parseConditionInEvent(
                "event-entity is not unresponsive",
                CondIsResponsive.class,
                FabricUseEntityHandle.class
        );
        assertFalse(readBoolean(notUnresponsive, "responsive"));
        assertTrue(notUnresponsive.isNegated());
        assertEquals("event-entity", expression(notUnresponsive, "entities").toString(null, false));
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
