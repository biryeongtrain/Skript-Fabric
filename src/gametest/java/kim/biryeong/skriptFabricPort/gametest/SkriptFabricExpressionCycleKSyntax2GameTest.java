package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.PrivateBellAccess;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycleKSyntax2GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void ringingTimeExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        BlockPos bellPos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(bellPos, Blocks.BELL.defaultBlockState());

        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-k/syntax2/ringing_time_marks_block.sk");

            BellBlockEntity bell = bellAt(helper, bellPos);
            PrivateBellAccess.setNearbyEntities(bell, List.of());
            PrivateBellAccess.setRinging(bell, true);
            PrivateBellAccess.setRingingTicks(bell, 7);

            int executed = dispatch(runtime, helper, new CycleKSyntax2Handle(helper.getLevel(), bellPos));
            assertExecuted(helper, executed, "cycle k ringing time");

            helper.assertTrue(
                    helper.getLevel().getBlockState(bellPos.above()).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected ringing time fixture to mark the block above the bell.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void resonatingTimeExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        BlockPos bellPos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(bellPos, Blocks.BELL.defaultBlockState());

        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-k/syntax2/resonating_time_marks_block.sk");

            BellBlockEntity bell = bellAt(helper, bellPos);
            PrivateBellAccess.setNearbyEntities(bell, List.of());
            PrivateBellAccess.setResonating(bell, true);
            PrivateBellAccess.setResonatingTicks(bell, 9);

            int executed = dispatch(runtime, helper, new CycleKSyntax2Handle(helper.getLevel(), bellPos));
            assertExecuted(helper, executed, "cycle k resonating time");

            helper.assertTrue(
                    helper.getLevel().getBlockState(bellPos.above()).is(Blocks.IRON_BLOCK),
                    Component.literal("Expected resonating time fixture to mark the block above the bell.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void lowestHighestSolidBlockExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        BlockPos column = helper.absolutePos(new BlockPos(5, 0, 5));
        helper.getLevel().setBlockAndUpdate(column.atY(1), Blocks.COBBLESTONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(column.atY(4), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(column.atY(5), Blocks.AIR.defaultBlockState());

        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-k/syntax2/lowest_highest_solid_block_marks_blocks.sk");

            Variables.setVariable(
                    "cyclek::syntax2::origin",
                    new FabricLocation(helper.getLevel(), new Vec3(column.getX() + 0.5D, 12.0D, column.getZ() + 0.5D)),
                    SkriptEvent.EMPTY,
                    false
            );

            int executed = dispatch(runtime, helper, new CycleKSyntax2Handle(helper.getLevel(), column));
            assertExecuted(helper, executed, "cycle k solid block");

            helper.assertTrue(
                    helper.getLevel().getBlockState(column.atY(5)).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected highest solid block fixture to mark above the highest solid block.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(column.atY(2)).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected lowest solid block fixture to mark above the lowest solid block.")
            );

            Variables.setVariable("cyclek::syntax2::origin", null, SkriptEvent.EMPTY, false);
            runtime.clearScripts();
        });
    }

    private static BellBlockEntity bellAt(GameTestHelper helper, BlockPos pos) {
        return (BellBlockEntity) helper.getLevel().getBlockEntity(pos);
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestCycleKSyntax2Event.class, "gametest cycle k syntax2 context");
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

    private record CycleKSyntax2Handle(net.minecraft.server.level.ServerLevel level, BlockPos position) implements FabricBlockEventHandle {
    }

    public static final class GameTestCycleKSyntax2Event extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleKSyntax2Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleKSyntax2Handle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle k syntax2 context";
        }
    }
}
