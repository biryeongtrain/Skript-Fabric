package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricFeedGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void feedEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/feed_event_player_by_beefs_marks_block.sk");

            Cow cow = createCow(helper, false);
            BlockPos markerRelative = new BlockPos(9, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.getFoodData().setFoodLevel(5);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected feed effect script to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GREEN_WOOL),
                    Component.literal("Expected feed effect script to update the marker block.")
            );
            helper.assertTrue(
                    player.getFoodData().getFoodLevel() == 7,
                    Component.literal("Expected feed effect to increase the player's food level by the scripted beef amount.")
            );
            runtime.clearScripts();
        });
    }
}
