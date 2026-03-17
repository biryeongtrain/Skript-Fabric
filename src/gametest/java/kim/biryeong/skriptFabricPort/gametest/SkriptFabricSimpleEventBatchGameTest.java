package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricSimpleEventBatchGameTest extends AbstractSkriptFabricGameTestSupport {

	// ==================== Toggle Events ====================

	@GameTest
	public void sprintToggleEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/sprint_toggle_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchSprintToggle(player, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected sprint toggle event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void flightToggleEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/flight_toggle_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchFlightToggle(player, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected flight toggle event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void glideToggleEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/glide_toggle_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchGlideToggle(player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected glide toggle event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void swimToggleEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/swim_toggle_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
			SkriptFabricEventBridge.dispatchSwimToggle(cow, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected swim toggle event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void batToggleSleepEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/bat_toggle_sleep_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity bat = helper.spawnWithNoFreeWill(EntityType.BAT, 0.5F, 1.0F, 0.5F);
			SkriptFabricEventBridge.dispatchBatToggleSleep(helper.getLevel(), bat, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected bat toggle sleep event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== Entity Events ====================

	@GameTest
	public void tameEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/tame_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			TamableAnimal wolf = (TamableAnimal) helper.spawnWithNoFreeWill(EntityType.WOLF, 0.5F, 1.0F, 0.5F);
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchTame(helper.getLevel(), wolf, player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected tame event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void combustEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/combust_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
			SkriptFabricEventBridge.dispatchCombust(helper.getLevel(), cow, 100);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected combust event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void projectileHitEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/projectile_hit_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Arrow arrow = (Arrow) EntityType.ARROW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos arrowPos = helper.absolutePos(new BlockPos(0, 2, 0));
			arrow.setPos(arrowPos.getX() + 0.5D, arrowPos.getY(), arrowPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(arrow);
			SkriptFabricEventBridge.dispatchProjectileHit(helper.getLevel(), arrow, null);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected projectile hit event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void projectileLaunchEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/projectile_launch_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Arrow arrow = (Arrow) EntityType.ARROW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos arrowPos = helper.absolutePos(new BlockPos(0, 2, 0));
			arrow.setPos(arrowPos.getX() + 0.5D, arrowPos.getY(), arrowPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(arrow);
			SkriptFabricEventBridge.dispatchProjectileLaunch(helper.getLevel(), arrow);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected projectile launch event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void lightningStrikeEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/lightning_strike_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			LightningBolt bolt = (LightningBolt) EntityType.LIGHTNING_BOLT.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos boltPos = helper.absolutePos(new BlockPos(0, 2, 0));
			bolt.setPos(boltPos.getX() + 0.5D, boltPos.getY(), boltPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(bolt);
			SkriptFabricEventBridge.dispatchLightningStrike(helper.getLevel(), bolt);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected lightning strike event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void resurrectAttemptEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/resurrect_attempt_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchResurrectAttempt(helper.getLevel(), player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected resurrect attempt event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void sheepRegrowWoolEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/sheep_regrow_wool_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity sheep = helper.spawnWithNoFreeWill(EntityType.SHEEP, 0.5F, 1.0F, 0.5F);
			SkriptFabricEventBridge.dispatchSheepRegrowWool(helper.getLevel(), sheep);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected sheep regrow wool event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void slimeSplitEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/slime_split_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity slime = helper.spawnWithNoFreeWill(EntityType.SLIME, 0.5F, 1.0F, 0.5F);
			SkriptFabricEventBridge.dispatchSlimeSplit(helper.getLevel(), slime);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected slime split event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void entityMountEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/entity_mount_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity pig = helper.spawnWithNoFreeWill(EntityType.PIG, 0.5F, 1.0F, 0.5F);
			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 1.5F, 1.0F, 0.5F);
			SkriptFabricEventBridge.dispatchEntityMount(helper.getLevel(), cow, pig);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected entity mount event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void entityDismountEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/entity_dismount_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity pig = helper.spawnWithNoFreeWill(EntityType.PIG, 0.5F, 1.0F, 0.5F);
			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 1.5F, 1.0F, 0.5F);
			SkriptFabricEventBridge.dispatchEntityDismount(helper.getLevel(), cow, pig);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected entity dismount event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== Player Events ====================

	@GameTest
	public void bedEnterEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/bed_enter_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchBedEnter(helper.getLevel(), player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected bed enter event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void bedLeaveEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/bed_leave_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchBedLeave(helper.getLevel(), player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected bed leave event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void foodLevelChangeEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/food_level_change_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchFoodLevelChange(helper.getLevel(), player, 20, 15);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected food level change event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void toolChangeEventNamesPlayer(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/tool_change_names_player.sk");

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchToolChange(player, 0, 1);

			helper.assertTrue(
					player.getCustomName() != null && player.getCustomName().getString().equals("tool changed"),
					Component.literal("Expected tool change event to name the player.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void languageChangeEventNamesPlayer(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/language_change_names_player.sk");

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchLanguageChange(player, "ko_KR");

			helper.assertTrue(
					player.getCustomName() != null && player.getCustomName().getString().equals("language changed"),
					Component.literal("Expected language change event to name the player.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void playerWorldChangeEventNamesPlayer(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/player_world_change_names_player.sk");

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchPlayerWorldChange(player, helper.getLevel());

			helper.assertTrue(
					player.getCustomName() != null && player.getCustomName().getString().equals("world changed"),
					Component.literal("Expected player world change event to name the player.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void inventoryOpenEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/inventory_open_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchInventoryOpen(helper.getLevel(), player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected inventory open event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void inventoryCloseEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/inventory_close_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchInventoryClose(helper.getLevel(), player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected inventory close event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void inventoryDragEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/inventory_drag_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchInventoryDrag(helper.getLevel(), player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected inventory drag event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== Vehicle Events ====================

	@GameTest
	public void vehicleCreateEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_create_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos minecartPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(minecartPos.getX() + 0.5D, minecartPos.getY(), minecartPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);
			SkriptFabricEventBridge.dispatchVehicleCreate(helper.getLevel(), minecart);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected vehicle create event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void vehicleDamageEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_damage_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos minecartPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(minecartPos.getX() + 0.5D, minecartPos.getY(), minecartPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchVehicleDamage(helper.getLevel(), minecart, player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected vehicle damage event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void vehicleDestroyEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_destroy_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos minecartPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(minecartPos.getX() + 0.5D, minecartPos.getY(), minecartPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchVehicleDestroy(helper.getLevel(), minecart, player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected vehicle destroy event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void vehicleEnterEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_enter_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos minecartPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(minecartPos.getX() + 0.5D, minecartPos.getY(), minecartPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchVehicleEnter(helper.getLevel(), minecart, player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected vehicle enter event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void vehicleExitEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_exit_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos minecartPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(minecartPos.getX() + 0.5D, minecartPos.getY(), minecartPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchVehicleExit(helper.getLevel(), minecart, player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected vehicle exit event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== Block Events ====================

	@GameTest
	public void signChangeEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/sign_change_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			BlockPos signPos = helper.absolutePos(new BlockPos(1, 1, 0));
			helper.getLevel().setBlockAndUpdate(signPos, Blocks.OAK_SIGN.defaultBlockState());
			SkriptFabricEventBridge.dispatchSignChange(helper.getLevel(), player, signPos, new String[]{"line1", "line2", "line3", "line4"}, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected sign change event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void blockDamageEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/block_damage_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			BlockPos stonePos = helper.absolutePos(new BlockPos(1, 1, 0));
			helper.getLevel().setBlockAndUpdate(stonePos, Blocks.STONE.defaultBlockState());
			SkriptFabricEventBridge.dispatchBlockDamage(helper.getLevel(), player, stonePos);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected block damage event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void bucketUseEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/bucket_use_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			SkriptFabricEventBridge.dispatchBucketUse(helper.getLevel(), player, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected bucket use event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void leavesDecayEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/leaves_decay_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			BlockPos leavesPos = helper.absolutePos(new BlockPos(1, 1, 0));
			helper.getLevel().setBlockAndUpdate(leavesPos, Blocks.OAK_LEAVES.defaultBlockState());
			SkriptFabricEventBridge.dispatchLeavesDecay(helper.getLevel(), leavesPos);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected leaves decay event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void spongeAbsorbEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/sponge_absorb_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			BlockPos spongePos = helper.absolutePos(new BlockPos(1, 1, 0));
			helper.getLevel().setBlockAndUpdate(spongePos, Blocks.SPONGE.defaultBlockState());
			SkriptFabricEventBridge.dispatchSpongeAbsorb(helper.getLevel(), spongePos);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected sponge absorb event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void spawnChangeEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/spawn_change_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			SkriptFabricEventBridge.dispatchSpawnChange(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 0)));

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected spawn change event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void bellRingEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/bell_ring_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			BlockPos bellPos = helper.absolutePos(new BlockPos(1, 1, 0));
			helper.getLevel().setBlockAndUpdate(bellPos, Blocks.BELL.defaultBlockState());
			SkriptFabricEventBridge.dispatchBellRing(helper.getLevel(), bellPos);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected bell ring event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void bellResonateEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/bell_resonate_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			BlockPos bellPos = helper.absolutePos(new BlockPos(1, 1, 0));
			helper.getLevel().setBlockAndUpdate(bellPos, Blocks.BELL.defaultBlockState());
			SkriptFabricEventBridge.dispatchBellResonate(helper.getLevel(), bellPos);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected bell resonate event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== Chunk Events ====================

	@GameTest
	public void chunkLoadEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/chunk_load_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			LevelChunk chunk = helper.getLevel().getChunkAt(marker);
			SkriptFabricEventBridge.dispatchChunkLoad(helper.getLevel(), chunk);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected chunk load event to mark the block.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void chunkUnloadEventMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/chunk_unload_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			LevelChunk chunk = helper.getLevel().getChunkAt(marker);
			SkriptFabricEventBridge.dispatchChunkUnload(helper.getLevel(), chunk);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected chunk unload event to mark the block.")
			);
			runtime.clearScripts();
		});
	}
}
