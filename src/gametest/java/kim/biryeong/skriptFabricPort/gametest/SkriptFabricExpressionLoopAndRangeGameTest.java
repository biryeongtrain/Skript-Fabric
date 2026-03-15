package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionLoopAndRangeGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void cycleKSyntax1ExpressionsExecuteRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/loop-and-range/core_expressions_record_values.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            int executed = dispatch(runtime, helper, new LoopAndRangeHandle());
            assertExecuted(helper, executed, "loop and range expressions");

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(net.minecraft.world.level.block.Blocks.EMERALD_BLOCK),
                    Component.literal("Expected loop and range fixture to mark the verification block.")
            );
            assertString(helper, "looprange::first", "alpha");
            assertString(helper, "looprange::range::second", "beta");
            assertString(helper, "looprange::range::third", "gamma");
            assertString(helper, "looprange::previous", "alpha");
            assertString(helper, "looprange::current", "beta");
            assertString(helper, "looprange::next", "gamma");
            assertScaledItem(helper, "looprange::scaled", 3, "diamond");

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    /**
     * Verifies that loop-iteration-1 and loop-iteration-2 correctly reference
     * the outer and inner loops respectively in a nested loop structure.
     */
    @GameTest
    public void nestedLoopIterationReferencesCorrectLoop(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/loop-and-range/nested_loop_iteration_record_values.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            int executed = dispatch(runtime, helper, new LoopAndRangeHandle());
            assertExecuted(helper, executed, "nested loop iteration");

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(net.minecraft.world.level.block.Blocks.EMERALD_BLOCK),
                    Component.literal("Expected nested loop iteration fixture to mark the verification block.")
            );
            Object outer = Variables.getVariable("looprange::nested::outer", null, false);
            helper.assertTrue(
                    outer instanceof Number n && n.longValue() == 2,
                    Component.literal("Expected loop-iteration-1 to be 2 but got " + outer)
            );
            Object inner = Variables.getVariable("looprange::nested::inner", null, false);
            helper.assertTrue(
                    inner instanceof Number n && n.longValue() == 1,
                    Component.literal("Expected loop-iteration-2 to be 1 but got " + inner)
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestLoopAndRangeEvent.class, "gametest loop and range context");
        Skript.registerExpression(LoopRangeValuesExpression.class, Object.class, "looprange-values");
        Skript.registerExpression(LoopRangeItemTypeExpression.class, FabricItemType.class, "looprange-itemtype");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(handle, helper.getLevel().getServer(), helper.getLevel(), null));
    }

    private static void assertExecuted(GameTestHelper helper, int executed, String description) {
        helper.assertTrue(
                executed == 1,
                Component.literal("Expected 1 trigger for " + description + " but got " + executed + ".")
        );
    }

    private static void assertString(GameTestHelper helper, String name, String expected) {
        Object value = Variables.getVariable(name, null, false);
        helper.assertTrue(
                expected.equals(value),
                Component.literal("Expected " + name + " to be " + expected + " but got " + value + ".")
        );
    }

    private static void assertScaledItem(GameTestHelper helper, String name, int expectedAmount, String expectedItemId) {
        Object value = Variables.getVariable(name, null, false);
        helper.assertTrue(
                value instanceof FabricItemType itemType
                        && itemType.amount() == expectedAmount
                        && expectedItemId.equals(itemType.itemId()),
                Component.literal("Expected " + name + " to be " + expectedAmount + " of " + expectedItemId + " but got " + value + ".")
        );
    }

    private record LoopAndRangeHandle() {
    }

    public static final class GameTestLoopAndRangeEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof LoopAndRangeHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{LoopAndRangeHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest loop and range context";
        }
    }

    public static final class LoopRangeValuesExpression extends ch.njol.skript.lang.util.SimpleExpression<Object>
            implements ch.njol.skript.lang.KeyProviderExpression<Object> {
        private static final Object[] VALUES = new Object[]{"alpha", "beta", "gamma"};
        private static final String[] KEYS = new String[]{"first", "second", "third"};

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return Arrays.copyOf(VALUES, VALUES.length);
        }

        @Override
        public @Nullable Iterator<? extends Object> iterator(SkriptEvent event) {
            return Arrays.asList(VALUES).iterator();
        }

        @Override
        public boolean supportsLoopPeeking() {
            return true;
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public String[] getArrayKeys(SkriptEvent event) {
            return Arrays.copyOf(KEYS, KEYS.length);
        }

        @Override
        public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
            return Arrays.asList(KeyedValue.zip(VALUES, KEYS)).iterator();
        }
    }

    public static final class LoopRangeItemTypeExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return new FabricItemType[]{new FabricItemType(Items.DIAMOND)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }
    }
}
