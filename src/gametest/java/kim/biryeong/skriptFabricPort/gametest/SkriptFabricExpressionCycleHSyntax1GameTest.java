package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycleHSyntax1GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void quitReasonExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax1/quit_reason_records_value.sk");

            int executed = dispatch(runtime, helper, new TestQuitHandle("cycle-h quit reason"));
            assertExecuted(helper, executed, "cycle h quit reason");
            helper.assertTrue(
                    "cycle-h quit reason".equals(Variables.getVariable("cycleh::quit_reason", null, false)),
                    Component.literal("Expected quit reason GameTest fixture to record the current quit reason.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void sourceBlockExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax1/source_block_marks_above.sk");

            BlockPos relative = new BlockPos(0, 1, 0);
            BlockPos absolute = helper.absolutePos(relative);
            helper.getLevel().setBlockAndUpdate(absolute, Blocks.STONE.defaultBlockState());

            int executed = dispatch(runtime, helper, new TestSourceHandle(new FabricBlock(helper.getLevel(), absolute)));
            assertExecuted(helper, executed, "cycle h source block");
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, relative.above());
            runtime.clearScripts();
        });
    }

    @GameTest
    public void tamerExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax1/tamer_changes_xp.sk");

            ServerPlayer owner = helper.makeMockServerPlayerInLevel();
            owner.setGameMode(GameType.SURVIVAL);

            int executed = dispatch(runtime, helper, new TestTameHandle(owner));
            assertExecuted(helper, executed, "cycle h tamer");
            helper.assertTrue(
                    owner.experienceLevel == 5,
                    Component.literal("Expected tamer GameTest fixture to update the tamer player's xp level.")
            );
            runtime.clearScripts();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestQuitEvent.class, "gametest cycle h quit context");
        Skript.registerEvent(GameTestSourceEvent.class, "gametest cycle h source context");
        Skript.registerEvent(GameTestTameEvent.class, "gametest cycle h tame context");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(
                handle,
                helper.getLevel().getServer(),
                helper.getLevel(),
                null
        ));
    }

    private static void assertExecuted(GameTestHelper helper, int executed, String description) {
        helper.assertTrue(
                executed == 1,
                Component.literal("Expected 1 trigger for " + description + " but got " + executed + ".")
        );
    }

    private record TestQuitHandle(String reason) {
    }

    private record TestSourceHandle(FabricBlock source) {
    }

    private record TestTameHandle(ServerPlayer owner) {
    }

    public static final class GameTestQuitEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof TestQuitHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{TestQuitHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle h quit context";
        }
    }

    public static final class GameTestSourceEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof TestSourceHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{TestSourceHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle h source context";
        }
    }

    public static final class GameTestTameEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof TestTameHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{TestTameHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle h tame context";
        }
    }
}
