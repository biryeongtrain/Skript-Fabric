package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.level.ServerPlayer;
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
}
