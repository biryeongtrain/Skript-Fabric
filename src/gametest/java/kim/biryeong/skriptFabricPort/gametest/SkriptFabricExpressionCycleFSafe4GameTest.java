package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.variables.Variables;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycleFSafe4GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);
    private static final BlockPos PROOF_MARKER = new BlockPos(4, 1, 0);

    private static volatile @Nullable FabricLocation centerLocation;

    @GameTest
    public void hexCodeExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            try {
                runtime.loadFromResource("skript/gametest/expression/cycle-f-safe4/hex_code_records_value.sk");

                int executed = dispatch(runtime, helper);
                assertExecuted(helper, executed, "hex code expression");
                assertText(helper, "cyclef::hex_code", "FF0000");
                helper.assertBlockPresent(Blocks.EMERALD_BLOCK, PROOF_MARKER);
            } finally {
                runtime.clearScripts();
                Variables.clearAll();
                resetContext();
            }
        });
    }

    @GameTest
    public void colorFromHexCodeExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            try {
                runtime.loadFromResource("skript/gametest/expression/cycle-f-safe4/color_from_hex_records_channels.sk");

                int executed = dispatch(runtime, helper);
                assertExecuted(helper, executed, "color from hex expression");
                assertColor(helper, "cyclef::color_from_hex::rgb", 255, 187, 167);
                assertColor(helper, "cyclef::color_from_hex::argb", 51, 102, 204);
                helper.assertBlockPresent(Blocks.EMERALD_BLOCK, PROOF_MARKER);
            } finally {
                runtime.clearScripts();
                Variables.clearAll();
                resetContext();
            }
        });
    }

    @GameTest
    public void recursiveSizeExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            try {
                runtime.loadFromResource("skript/gametest/expression/cycle-f-safe4/recursive_size_records_value.sk");

                int executed = dispatch(runtime, helper);
                assertExecuted(helper, executed, "recursive size expression");
                assertNumber(helper, "cyclef::recursive_size::count", 4);
                helper.assertBlockPresent(Blocks.EMERALD_BLOCK, PROOF_MARKER);
            } finally {
                runtime.clearScripts();
                Variables.clearAll();
                resetContext();
            }
        });
    }

    @GameTest
    public void blockSphereExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            try {
                BlockPos center = helper.absolutePos(new BlockPos(1, 1, 0));
                centerLocation = new FabricLocation(helper.getLevel(), Vec3.atCenterOf(center));

                runtime.loadFromResource("skript/gametest/expression/cycle-f-safe4/block_sphere_records_blocks.sk");

                int executed = dispatch(runtime, helper);
                assertExecuted(helper, executed, "block sphere expression");
                Collection<Object> entries = variableValues("cyclef::block_sphere::entries::");
                helper.assertTrue(
                        entries.size() == 7,
                        Component.literal("Expected 7 stored sphere entries but got " + entries.size() + ".")
                );
                assertContainsBlock(helper, entries, center);
                assertContainsBlock(helper, entries, center.above());
                assertContainsBlock(helper, entries, center.below());
                assertContainsBlock(helper, entries, center.north());
                assertContainsBlock(helper, entries, center.south());
                assertContainsBlock(helper, entries, center.east());
                assertContainsBlock(helper, entries, center.west());
                helper.assertBlockPresent(Blocks.EMERALD_BLOCK, PROOF_MARKER);
            } finally {
                runtime.clearScripts();
                Variables.clearAll();
                resetContext();
            }
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestCycleFSafe4ContextEvent.class, "gametest cycle f safe4 context");
        Skript.registerExpression(LaneFCenterLocationExpression.class, FabricLocation.class, "lane-f-center-location");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper) {
        return runtime.dispatch(new SkriptEvent(new CycleFSafe4Handle(), helper.getLevel().getServer(), helper.getLevel(), null));
    }

    private static void assertExecuted(GameTestHelper helper, int executed, String description) {
        helper.assertTrue(
                executed == 1,
                Component.literal("Expected 1 trigger for " + description + " but got " + executed + ".")
        );
    }

    private static void assertText(GameTestHelper helper, String variable, String expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                expected.equals(value),
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertNumber(GameTestHelper helper, String variable, double expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number number && Double.compare(number.doubleValue(), expected) == 0,
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertColor(GameTestHelper helper, String variable, int red, int green, int blue) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Color color
                        && color.red() == red
                        && color.green() == green
                        && color.blue() == blue,
                Component.literal("Expected " + variable + " to be rgb(" + red + ", " + green + ", " + blue + ") but got " + value + ".")
        );
    }

    private static Collection<Object> variableValues(String prefix) {
        return Variables.getVariablesWithPrefix(prefix, null, false).entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(Map.Entry::getValue)
                .toList();
    }

    private static void assertContainsBlock(GameTestHelper helper, Collection<Object> values, BlockPos expected) {
        helper.assertTrue(
                values.stream().anyMatch(value -> value instanceof FabricBlock block && expected.equals(block.position())),
                Component.literal("Expected block list to contain " + expected + " but got " + values + ".")
        );
    }

    private static void resetContext() {
        centerLocation = null;
    }

    private record CycleFSafe4Handle() {
    }

    public static final class GameTestCycleFSafe4ContextEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleFSafe4Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleFSafe4Handle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle f safe4 context";
        }
    }

    public static final class LaneFCenterLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return centerLocation == null ? null : new FabricLocation[]{centerLocation};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricLocation> getReturnType() {
            return FabricLocation.class;
        }
    }

}
