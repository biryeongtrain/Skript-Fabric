package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

final class ConditionBindingTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void responsiveFixtureBindsEntityTargetAndResponsiveMode() throws Exception {
        CondIsResponsive condition = loadFirstCondition(
                "skript/gametest/condition/responsive_interaction_names_entity.sk",
                CondIsResponsive.class
        );
        assertTrue(readBoolean(condition, "responsive"));
        assertFalse(condition.isNegated());
        assertEquals("event-entity", expression(condition, "entities").toString(null, false));
    }

    @Test
    void unresponsiveFixtureBindsEntityTargetAndUnresponsiveMode() throws Exception {
        CondIsResponsive condition = loadFirstCondition(
                "skript/gametest/condition/unresponsive_interaction_names_entity.sk",
                CondIsResponsive.class
        );
        assertFalse(readBoolean(condition, "responsive"));
        assertFalse(condition.isNegated());
        assertEquals("event-entity", expression(condition, "entities").toString(null, false));
    }

    private <T> T loadFirstCondition(String resourcePath, Class<T> conditionClass) throws Exception {
        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.clearScripts();
        runtime.loadFromPath(Path.of("src/gametest/resources").resolve(resourcePath));
        Trigger trigger = onlyLoadedTrigger(runtime);
        Object first = firstTriggerItem(trigger);
        assertInstanceOf(conditionClass, first);
        return conditionClass.cast(first);
    }

    private Trigger onlyLoadedTrigger(SkriptRuntime runtime) throws ReflectiveOperationException {
        Field scriptsField = SkriptRuntime.class.getDeclaredField("scripts");
        scriptsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Script> scripts = (List<Script>) scriptsField.get(runtime);
        Script script = scripts.getFirst();
        Structure structure = script.getStructures().getFirst();
        return ((ch.njol.skript.lang.SkriptEvent) structure).getTrigger();
    }

    private Object firstTriggerItem(Trigger trigger) throws ReflectiveOperationException {
        Field firstField = TriggerSection.class.getDeclaredField("first");
        firstField.setAccessible(true);
        TriggerItem first = (TriggerItem) firstField.get(trigger);
        assertNotNull(first);
        return first;
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
