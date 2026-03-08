package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExprSecCreateLootContextCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @BeforeEach
    void clearVariables() {
        Variables.clearAll();
    }

    @Test
    void lootContextSectionMutatesWrapperAndPropagatesLocals() {
        ExprSecCreateLootContext expression = new ExprSecCreateLootContext();
        SectionNode node = new SectionNode("set {_context} to a loot context at 1, 2, 3:");
        node.add(new SimpleNode("set {_state} to \"inside\""));
        node.add(new SimpleNode("set loot location to 0, 1, 0"));

        FabricLocation initial = new FabricLocation(null, new Vec3(1, 2, 3));
        SkriptEvent event = new SkriptEvent(new Object(), null, null, null);
        Variables.setVariable("state", "outside", event, true);

        boolean initialized = expression.init(
                new Expression<?>[]{new ConstantExpression<>(FabricLocation.class, initial)},
                0,
                Kleenean.FALSE,
                new ParseResult(),
                node,
                null
        );

        assertTrue(initialized);

        LootContextWrapper wrapper = expression.getSingle(event);

        assertNotNull(wrapper);
        assertEquals(0.0D, wrapper.getLocation().position().x());
        assertEquals(1.0D, wrapper.getLocation().position().y());
        assertEquals(0.0D, wrapper.getLocation().position().z());
        assertEquals("inside", Variables.getVariable("state", event, true));
    }

    private static final class ConstantExpression<T> extends SimpleExpression<T> {

        private final Class<? extends T> returnType;
        private final T[] values;

        @SafeVarargs
        private ConstantExpression(Class<? extends T> returnType, T... values) {
            this.returnType = returnType;
            this.values = values;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean isSingle() {
            return values.length == 1;
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }
    }
}
