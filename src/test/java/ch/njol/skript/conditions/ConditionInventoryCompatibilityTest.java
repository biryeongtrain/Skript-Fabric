package ch.njol.skript.conditions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ConditionInventoryCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
    }

    @Test
    void containsSupportsInventoryStringAndObjectChecks() {
        SimpleContainer container = new SimpleContainer(2);
        container.setItem(0, new ItemStack(Items.STICK, 2));
        container.setItem(1, new ItemStack(Items.STICK, 1));
        FabricInventory inventory = new FabricInventory(container, MenuType.GENERIC_9x1);

        CondContains inventoryContains = new CondContains();
        inventoryContains.init(new Expression[]{
                new SimpleLiteral<>(inventory, false),
                new SimpleLiteral<>(new FabricItemType(Items.STICK, 3, null), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(inventoryContains.check(SkriptEvent.EMPTY));

        CondContains stringContains = new CondContains();
        stringContains.init(new Expression[]{
                new SimpleLiteral<>("Skript Fabric", false),
                new SimpleLiteral<>("fabric", false)
        }, 2, Kleenean.FALSE, parseResult(""));
        assertTrue(stringContains.check(SkriptEvent.EMPTY));

        CondContains objectContains = new CondContains();
        objectContains.init(new Expression[]{
                new NumberListExpression(1, 5),
                new SimpleLiteral<>(5, false)
        }, 2, Kleenean.FALSE, parseResult(""));
        assertTrue(objectContains.check(SkriptEvent.EMPTY));
    }

    @Test
    void handAndWearingConditionsKeepReadableToStrings() {
        CondItemInHand holding = new CondItemInHand();
        holding.init(new Expression[]{
                new TestExpression<>("mob", LivingEntity.class),
                new SimpleLiteral<>(new FabricItemType(Items.STICK), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("mob is holding [stick]", holding.toString(SkriptEvent.EMPTY, false));

        CondIsWearing wearing = new CondIsWearing();
        wearing.init(new Expression[]{
                new TestExpression<>("mob", LivingEntity.class),
                new SimpleLiteral<>(new FabricItemType(Items.DIAMOND_HELMET), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("mob is wearing [diamond_helmet]", wearing.toString(SkriptEvent.EMPTY, false));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static final class NumberListExpression extends SimpleExpression<Integer> {

        private final Integer[] values;

        private NumberListExpression(Integer... values) {
            this.values = values;
        }

        @Override
        protected Integer @Nullable [] get(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean isSingle() {
            return values.length == 1;
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "numbers";
        }
    }

    private static final class TestExpression<T> extends SimpleExpression<T> {

        private final String text;
        private final Class<? extends T> returnType;

        private TestExpression(String text, Class<? extends T> returnType) {
            this.text = text;
            this.returnType = returnType;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            @SuppressWarnings("unchecked")
            T[] empty = (T[]) java.lang.reflect.Array.newInstance(returnType, 0);
            return empty;
        }

        @Override
        public boolean isSingle() {
            return !text.endsWith("s");
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return text;
        }
    }
}
