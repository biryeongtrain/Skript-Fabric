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
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.PrivateBeaconAccess;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionBeaconAndSpatialQueryGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest(skyAccess = true)
    public void appliedEffectExpressionExecutesRealScript(GameTestHelper helper) {
        BlockPos beaconPos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(beaconPos, Blocks.BEACON.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(beaconPos.above(), Blocks.AIR.defaultBlockState());
        buildSingleTierBeaconBase(helper, beaconPos);
        invokeBeaconTick(helper, beaconPos);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.removeAllEffects();
        player.teleportTo(beaconPos.getX() + 0.5D, beaconPos.getY() + 1.0D, beaconPos.getZ() + 0.5D);

        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/beacon-and-spatial-query/applied_effect_marks_block.sk");

            BeaconBlockEntity beacon = beaconAt(helper, beaconPos);
            PrivateBeaconAccess.setPrimaryPower(beacon, MobEffects.SPEED);
            beacon.setChanged();
            invokeBeaconApplyEffects(
                    helper,
                    beaconPos,
                    PrivateBeaconAccess.levels(beacon),
                    PrivateBeaconAccess.primaryPower(beacon),
                    null
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(beaconPos.above()).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected applied effect GameTest fixture to recognize the live speed beacon effect.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void nearestEntityExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/beacon-and-spatial-query/nearest_entity_names_closest_cow.sk");

            Cow nearCow = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.COW, 2.5F, 1.0F, 0.5F);
            Cow farCow = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.COW, 6.5F, 1.0F, 0.5F);
            nearCow.setCustomName(null);
            farCow.setCustomName(null);

            Variables.setVariable(
                    "spatial::origin",
                    new FabricLocation(
                            helper.getLevel(),
                            Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(0, 1, 0)))
                    ),
                    SkriptEvent.EMPTY,
                    false
            );

            int executed = dispatch(runtime, helper, new BeaconAndSpatialQueryHandle());
            assertExecuted(helper, executed, "cycle j nearest entity");

            helper.assertTrue(
                    nearCow.getCustomName() != null && "nearest cow".equals(nearCow.getCustomName().getString()),
                    Component.literal("Expected nearest entity fixture to rename the closest cow.")
            );
            helper.assertTrue(
                    farCow.getCustomName() == null,
                    Component.literal("Expected nearest entity fixture to leave the farther cow unchanged.")
            );

            Variables.setVariable("spatial::origin", null, SkriptEvent.EMPTY, false);
            runtime.clearScripts();
        });
    }

    @GameTest
    public void targetedBlockExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/beacon-and-spatial-query/targeted_block_marks_above.sk");

            BlockPos targetAbsolute = helper.absolutePos(new BlockPos(2, 2, 0));
            helper.getLevel().setBlockAndUpdate(targetAbsolute, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(targetAbsolute.above(), Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            BlockPos playerAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
            player.teleportTo(playerAbsolute.getX() + 0.5D, playerAbsolute.getY(), playerAbsolute.getZ() + 0.5D);
            player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(targetAbsolute));
            assertPlayerTargetsBlock(helper, player, targetAbsolute);

            int executed = dispatch(runtime, helper, new BeaconAndSpatialQueryHandle(), player);
            assertExecuted(helper, executed, "cycle j targeted block");

            helper.assertTrue(
                    helper.getLevel().getBlockState(targetAbsolute.above()).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected targeted block fixture to mark the block above the real crosshair target.")
            );
            runtime.clearScripts();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestBeaconAndSpatialQueryEvent.class, "gametest beacon and spatial query context");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(handle, helper.getLevel().getServer(), helper.getLevel(), null));
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle, @Nullable ServerPlayer player) {
        return runtime.dispatch(new SkriptEvent(handle, helper.getLevel().getServer(), helper.getLevel(), player));
    }

    private static void assertExecuted(GameTestHelper helper, int executed, String description) {
        helper.assertTrue(
                executed == 1,
                Component.literal("Expected 1 trigger for " + description + " but got " + executed + ".")
        );
    }

    private static void assertPlayerTargetsBlock(GameTestHelper helper, ServerPlayer player, BlockPos expectedTarget) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().normalize().scale(100.0D));
        HitResult hit = helper.getLevel().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        helper.assertTrue(
                hit instanceof BlockHitResult blockHit
                        && hit.getType() == HitResult.Type.BLOCK
                        && expectedTarget.equals(blockHit.getBlockPos()),
                Component.literal("Expected cycle j player ray to target " + expectedTarget + " but got "
                        + (hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos() : hit.getType()) + ".")
        );
    }

    private static void buildSingleTierBeaconBase(GameTestHelper helper, BlockPos beaconPos) {
        BlockPos baseCenter = beaconPos.below();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.getLevel().setBlockAndUpdate(baseCenter.offset(x, 0, z), Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
    }

    private record BeaconAndSpatialQueryHandle() {
    }

    public static final class GameTestBeaconAndSpatialQueryEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof BeaconAndSpatialQueryHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{BeaconAndSpatialQueryHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest beacon and spatial query context";
        }
    }
}
