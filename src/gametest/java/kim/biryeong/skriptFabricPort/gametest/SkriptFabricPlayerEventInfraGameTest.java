package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricPlayerEventInfraGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void resourcePackAcceptedEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/resource_pack_accepted_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(12, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            sendResourcePackResponse(player, ServerboundResourcePackPacket.Action.ACCEPTED);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected resource pack accepted event script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void moveEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_move_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(13, 1, 0));

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() - 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            sendPlayerMove(
                    player,
                    new Vec3(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D),
                    player.getYRot(),
                    player.getXRot()
            );

            helper.assertTrue(
                    player.getCustomName() != null && "move hook".equals(player.getCustomName().getString()),
                    Component.literal("Expected move event script to mutate the moving player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void chunkEnterEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_chunk_enter_marks_block.sk");

            BlockPos anchorAbsolute = helper.absolutePos(new BlockPos(14, 1, 0));
            int boundaryX = ((anchorAbsolute.getX() >> 4) << 4) + 16;
            int floorY = anchorAbsolute.getY();
            int floorZ = anchorAbsolute.getZ();

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(boundaryX - 0.5D, floorY + 1.0D, floorZ + 0.5D);

            sendPlayerMove(
                    player,
                    new Vec3(boundaryX + 0.5D, floorY + 1.0D, floorZ + 0.5D),
                    player.getYRot(),
                    player.getXRot()
            );

            helper.assertTrue(
                    player.getCustomName() != null && "chunk enter".equals(player.getCustomName().getString()),
                    Component.literal("Expected chunk-enter event script to mutate the player after crossing the chunk boundary.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playerCommandSendEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_command_list_send_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(15, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            helper.getLevel().getServer().getCommands().sendCommands(player);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected command-list send event script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void commandEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/command_say_marks_sender.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(16, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "say live hook");

            helper.assertTrue(
                    player.getCustomName() != null && "say live hook".equals(player.getCustomName().getString()),
                    Component.literal("Expected command event script to expose the full command on the player sender.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected command event script to mark the block under the command sender.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void levelAndExperienceChangeEventsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(17.5D, 2.0D, 0.5D);

            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/level_up_marks_player.sk");
            player.setCustomName(null);
            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.giveExperienceLevels(1);
            helper.assertTrue(
                    player.getCustomName() != null && "level up".equals(player.getCustomName().getString()),
                    Component.literal("Expected level-up event script to rename the player.")
            );

            runtime.clearScripts();
            player.setCustomName(null);
            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.giveExperiencePoints(5);
            runtime.loadFromResource("skript/gametest/event/experience_decrease_marks_player.sk");
            player.giveExperiencePoints(-1);
            helper.assertTrue(
                    player.getCustomName() != null && "xp down".equals(player.getCustomName().getString()),
                    Component.literal("Expected experience-decrease event script to rename the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void teleportEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/zombie_teleport_names_entity.sk");

            var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 17.5F, 2.0F, 1.5F);
            zombie.teleportTo(18.5D, 2.0D, 1.5D);

            helper.assertTrue(
                    zombie.getCustomName() != null && "teleported".equals(zombie.getCustomName().getString()),
                    Component.literal("Expected teleport event script to rename the teleported zombie.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void spectateEventsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/spectate_start_swap_stop_marks_player.sk");

            BlockPos playerStartAbsolute = helper.absolutePos(new BlockPos(18, 1, 0));

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SPECTATOR);
            player.teleportTo(playerStartAbsolute.getX() + 0.5D, playerStartAbsolute.getY() + 1.0D, playerStartAbsolute.getZ() + 0.5D);

            ArmorStand firstTarget = new ArmorStand(helper.getLevel(), 18.5D, 2.0D, 1.5D);
            ArmorStand secondTarget = new ArmorStand(helper.getLevel(), 19.5D, 2.0D, 1.5D);
            helper.getLevel().addFreshEntity(firstTarget);
            helper.getLevel().addFreshEntity(secondTarget);
            BlockPos firstMarkerAbsolute = firstTarget.blockPosition().below();
            BlockPos secondMarkerAbsolute = secondTarget.blockPosition().below();
            helper.getLevel().setBlockAndUpdate(firstMarkerAbsolute, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(secondMarkerAbsolute, Blocks.AIR.defaultBlockState());

            player.setCamera(firstTarget);
            helper.assertTrue(
                    player.getCustomName() != null && "spectate start".equals(player.getCustomName().getString()),
                    Component.literal("Expected spectate-start event script to rename the player.")
            );

            player.setCamera(secondTarget);
            helper.assertTrue(
                    helper.getLevel().getBlockState(secondMarkerAbsolute).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected spectate-swap event script to mark the block under the player.")
            );

            player.setCamera(player);
            helper.assertTrue(
                    helper.getLevel().getBlockState(secondMarkerAbsolute).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected spectate-stop event script to update the marker block.")
            );
            runtime.clearScripts();
        });
    }
}
