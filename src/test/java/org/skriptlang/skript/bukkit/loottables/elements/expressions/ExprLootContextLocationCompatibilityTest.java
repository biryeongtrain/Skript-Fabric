package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExprLootContextLocationCompatibilityTest {

    @BeforeEach
    void clearVariables() {
        Variables.clearAll();
    }

    @Test
    void resolvesObjectBackedLootContextExpressionWithoutArrayCast() {
        ExprLootContextLocation expression = new ExprLootContextLocation();
        LootContextWrapper wrapper = new LootContextWrapper(new FabricLocation(null, Vec3.ZERO));

        boolean initialized = expression.init(
                new Expression<?>[]{new ObjectBackedExpression(wrapper)},
                0,
                Kleenean.FALSE,
                new ParseResult()
        );

        assertTrue(initialized);
        FabricLocation resolved = expression.getSingle(SkriptEvent.EMPTY);
        assertNotNull(resolved);
        assertSame(wrapper.getLocation(), resolved);
    }

    @Test
    @SuppressWarnings("unchecked")
    void resolvesLootContextStoredInObjectTypedLocalVariable() {
        ExprLootContextLocation expression = new ExprLootContextLocation();
        LootContextWrapper wrapper = new LootContextWrapper(new FabricLocation(null, new Vec3(4, 5, 6)));
        SkriptEvent event = new SkriptEvent(new Object(), null, null, null);
        Variables.setVariable("context", wrapper, event, true);

        Variable<Object> variable = Variable.newInstance("_context", new Class[]{Object.class});
        assertNotNull(variable);

        boolean initialized = expression.init(
                new Expression<?>[]{variable},
                0,
                Kleenean.FALSE,
                new ParseResult()
        );

        assertTrue(initialized);
        FabricLocation resolved = expression.getSingle(event);
        assertNotNull(resolved);
        assertSame(wrapper.getLocation(), resolved);
    }

    private static final class ObjectBackedExpression implements Expression<Object> {

        private final Object[] values;

        private ObjectBackedExpression(Object... values) {
            this.values = values;
        }

        @Override
        public Object[] getArray(SkriptEvent event) {
            return values;
        }

        @Override
        public Object[] getAll(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean canReturn(Class<?> returnType) {
            return returnType == Object.class || returnType.isAssignableFrom(LootContextWrapper.class);
        }

        @Override
        public boolean isSingle() {
            return values.length == 1;
        }

        @Override
        public Class<? extends Object> getReturnType() {
            return Object.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "object backed loot context";
        }
    }
}
