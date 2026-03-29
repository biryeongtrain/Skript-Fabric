package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricInventoryGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void givePlayerDirtAddsItemToInventory(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/give_player_dirt_marks_block.sk");

            Cow cow = createCow(helper, false);
            BlockPos markerRelative = new BlockPos(9, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GREEN_WOOL),
                    Component.literal("Expected give effect script to update the marker block.")
            );
            helper.assertTrue(
                    containsItem(player, Items.DIRT, 1),
                    Component.literal("Expected player to have 1 dirt after 'give player 1 dirt'.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void giveDiamondSwordToPlayerAddsItemToInventory(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/give_diamond_sword_to_player_marks_block.sk");

            Cow cow = createCow(helper, false);
            BlockPos markerRelative = new BlockPos(9, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GREEN_WOOL),
                    Component.literal("Expected give effect script to update the marker block.")
            );
            helper.assertTrue(
                    containsItem(player, Items.DIAMOND_SWORD, 1),
                    Component.literal("Expected player to have 1 diamond sword after 'give 1 of minecraft:diamond_sword to player'.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void addPlankToInventoryAddsItem(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/add_plank_to_inventory_marks_block.sk");

            Cow cow = createCow(helper, false);
            BlockPos markerRelative = new BlockPos(9, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GREEN_WOOL),
                    Component.literal("Expected add-to-inventory script to update the marker block.")
            );
            helper.assertTrue(
                    containsItem(player, Items.OAK_PLANKS, 1),
                    Component.literal("Expected player to have 1 oak planks after 'add 1 oak planks to inventory of player'.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void clearInventoryRemovesAllItems(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/clear_inventory_marks_block.sk");

            Cow cow = createCow(helper, false);
            BlockPos markerRelative = new BlockPos(9, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().add(new ItemStack(Items.DIAMOND, 64));
            player.getInventory().add(new ItemStack(Items.IRON_INGOT, 32));
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GREEN_WOOL),
                    Component.literal("Expected clear inventory script to update the marker block.")
            );
            helper.assertTrue(
                    player.getInventory().isEmpty(),
                    Component.literal("Expected player inventory to be empty after 'clear the inventory of player'.")
            );
            runtime.clearScripts();
        });
    }

    private static boolean containsItem(ServerPlayer player, net.minecraft.world.item.Item item, int minCount) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item) && stack.getCount() >= minCount) {
                return true;
            }
        }
        return false;
    }
}
