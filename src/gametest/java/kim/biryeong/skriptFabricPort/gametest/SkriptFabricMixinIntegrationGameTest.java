package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.block.Blocks;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

/**
 * Integration tests that verify Mixin hookups fire events via real game actions,
 * rather than calling SkriptFabricEventBridge.dispatch*() directly.
 */
public final class SkriptFabricMixinIntegrationGameTest extends AbstractSkriptFabricGameTestSupport {

	// ==================== Entity Mixins ====================

	@GameTest
	public void combustViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/combust_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
			cow.igniteForSeconds(5.0F);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected EntityIgniteMixin to dispatch combust event when igniteForSeconds is called.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void entityMountViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/entity_mount_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity pig = helper.spawnWithNoFreeWill(EntityType.PIG, 0.5F, 1.0F, 0.5F);
			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 1.5F, 1.0F, 0.5F);
			cow.startRiding(pig, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected EntityRidingMixin to dispatch entity mount event when startRiding succeeds.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void entityDismountViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity pig = helper.spawnWithNoFreeWill(EntityType.PIG, 0.5F, 1.0F, 0.5F);
			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 1.5F, 1.0F, 0.5F);
			cow.startRiding(pig, true);

			runtime.loadFromResource("skript/gametest/event/entity_dismount_marks_block.sk");
			cow.removeVehicle();

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected EntityRidingMixin to dispatch entity dismount event when removeVehicle is called.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void swimToggleViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/swim_toggle_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
			cow.setSwimming(true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected EntitySwimMixin to dispatch swim toggle event when setSwimming is called.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void tameViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/tame_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			TamableAnimal wolf = (TamableAnimal) helper.spawnWithNoFreeWill(EntityType.WOLF, 0.5F, 1.0F, 0.5F);
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			wolf.tame(player);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected TamableAnimalMixin to dispatch tame event when tame is called.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void batToggleSleepViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/bat_toggle_sleep_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Bat bat = (Bat) helper.spawnWithNoFreeWill(EntityType.BAT, 0.5F, 1.0F, 0.5F);
			bat.setResting(true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected BatMixin to dispatch bat toggle sleep event when setResting is called.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void slimeSplitViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/slime_split_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Slime slime = (Slime) helper.spawnWithNoFreeWill(EntityType.SLIME, 0.5F, 1.0F, 0.5F);
			slime.setSize(4, false);
			slime.remove(Entity.RemovalReason.KILLED);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected SlimeSplitMixin to dispatch slime split event when large slime is killed.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== ServerLevel.addFreshEntity Mixins ====================

	@GameTest
	public void projectileLaunchViaMixinMarksBlock(GameTestHelper helper) {
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

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected ProjectileLaunchMixin to dispatch projectile launch when arrow is added to level.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void vehicleCreateViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_create_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos mcPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(mcPos.getX() + 0.5D, mcPos.getY(), mcPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected VehicleCreateMixin to dispatch vehicle create when minecart is added to level.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== Vehicle Damage/Destroy Mixins ====================

	@GameTest
	public void vehicleDamageViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_damage_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos mcPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(mcPos.getX() + 0.5D, mcPos.getY(), mcPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);

			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			ServerLevel level = helper.getLevel();
			minecart.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected VehicleDamageMixin to dispatch vehicle damage when minecart is hurt.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void vehicleDestroyViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_destroy_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			MinecartChest minecart = (MinecartChest) EntityType.CHEST_MINECART.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BlockPos mcPos = helper.absolutePos(new BlockPos(0, 2, 0));
			minecart.setPos(mcPos.getX() + 0.5D, mcPos.getY(), mcPos.getZ() + 0.5D);
			helper.getLevel().addFreshEntity(minecart);

			ServerLevel level = helper.getLevel();
			minecart.destroy(level, level.damageSources().generic());

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected VehicleDestroyMixin to dispatch vehicle destroy when minecart is destroyed.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== Vehicle Enter/Exit via EntityRidingMixin ====================

	@GameTest
	public void vehicleEnterViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();
			runtime.loadFromResource("skript/gametest/event/vehicle_enter_marks_block.sk");

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity pig = helper.spawnWithNoFreeWill(EntityType.PIG, 0.5F, 1.0F, 0.5F);
			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 1.5F, 1.0F, 0.5F);
			cow.startRiding(pig, true);

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected EntityRidingMixin to dispatch vehicle enter event when startRiding succeeds.")
			);
			runtime.clearScripts();
		});
	}

	@GameTest
	public void vehicleExitViaMixinMarksBlock(GameTestHelper helper) {
		runWithRuntimeLock(helper, () -> {
			SkriptRuntime runtime = SkriptRuntime.instance();
			runtime.clearScripts();

			BlockPos marker = helper.absolutePos(new BlockPos(0, 1, 0));
			helper.getLevel().setBlockAndUpdate(marker, Blocks.AIR.defaultBlockState());

			Entity pig = helper.spawnWithNoFreeWill(EntityType.PIG, 0.5F, 1.0F, 0.5F);
			Entity cow = helper.spawnWithNoFreeWill(EntityType.COW, 1.5F, 1.0F, 0.5F);
			cow.startRiding(pig, true);

			runtime.loadFromResource("skript/gametest/event/vehicle_exit_marks_block.sk");
			cow.removeVehicle();

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected EntityRidingMixin to dispatch vehicle exit event when removeVehicle is called.")
			);
			runtime.clearScripts();
		});
	}

	// ==================== LightningBolt Mixin ====================

	@GameTest
	public void lightningStrikeViaMixinMarksBlock(GameTestHelper helper) {
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

			// LightningBoltMixin fires at HEAD of tick() when tickCount == 1.
			// First tick: tickCount 0 -> 1 (mixin sees 0, no dispatch)
			// Second tick: tickCount 1 -> 2 (mixin sees 1, dispatches!)
			bolt.tick();
			bolt.tick();

			helper.assertTrue(
					helper.getLevel().getBlockState(marker).is(Blocks.REDSTONE_BLOCK),
					Component.literal("Expected LightningBoltMixin to dispatch lightning strike on second tick.")
			);
			runtime.clearScripts();
		});
	}
}
