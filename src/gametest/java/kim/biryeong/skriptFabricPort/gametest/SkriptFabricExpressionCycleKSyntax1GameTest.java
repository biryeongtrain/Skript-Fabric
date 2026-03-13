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

public final class SkriptFabricExpressionCycleKSyntax1GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void cycleKSyntax1ExpressionsExecuteRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-k/syntax1/core_expressions_record_values.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            int executed = dispatch(runtime, helper, new CycleKSyntax1Handle());
            assertExecuted(helper, executed, "cycle k syntax1 expressions");

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(net.minecraft.world.level.block.Blocks.EMERALD_BLOCK),
                    Component.literal("Expected cycle k syntax1 fixture to mark the verification block.")
            );
            assertString(helper, "cyclek::syntax1::first", "alpha");
            assertString(helper, "cyclek::syntax1::range::second", "beta");
            assertString(helper, "cyclek::syntax1::range::third", "gamma");
            assertString(helper, "cyclek::syntax1::previous", "alpha");
            assertString(helper, "cyclek::syntax1::current", "beta");
            assertString(helper, "cyclek::syntax1::next", "gamma");
            assertScaledItem(helper, "cyclek::syntax1::scaled", 3, "diamond");

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestCycleKSyntax1Event.class, "gametest cycle k syntax1 context");
        Skript.registerExpression(LaneKValuesExpression.class, Object.class, "lane-k-values");
        Skript.registerExpression(LaneKItemTypeExpression.class, FabricItemType.class, "lane-k-itemtype");
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

    private record CycleKSyntax1Handle() {
    }

    public static final class GameTestCycleKSyntax1Event extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleKSyntax1Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleKSyntax1Handle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle k syntax1 context";
        }
    }

    public static final class LaneKValuesExpression extends ch.njol.skript.lang.util.SimpleExpression<Object>
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

    public static final class LaneKItemTypeExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricItemType> {
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
