package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Brightness;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.PrivateBlockEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateFishingHookAccess;
import org.skriptlang.skript.fabric.runtime.FabricAttackEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricBlockBreakHandle;
import org.skriptlang.skript.fabric.runtime.FabricBucketCatchEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricBucketCatchHandle;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelHandle;
import org.skriptlang.skript.fabric.runtime.FabricDamageHandle;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricFishingHandle;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseItemHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

import java.io.IOException;
import java.nio.file.Path;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SkriptFabricExpressionGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void brewingFuelSlotExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/brewing_fuel_slot_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(0, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing slot expression test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricBrewingFuelHandle(helper.getLevel(), brewingAbsolute, brewingStand, true),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected brewing fuel slot script to execute exactly one trigger.")
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 2, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingHookExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/fishing_hook_names_hook.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected fishing hook expression script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    hook.getCustomName() != null && "fishing hook".equals(hook.getCustomName().getString()),
                    Component.literal("Expected fishing hook expression script to name the hook.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void hookedEntityExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/hooked_entity_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);
            setFishingHookedEntity(hook, armorStand);

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected hooked entity expression script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    armorStand.getCustomName() != null && "hooked entity".equals(armorStand.getCustomName().getString()),
                    Component.literal("Expected hooked entity expression script to name the hooked entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingWaitTimeExpressionExecutesRealScript(GameTestHelper helper) {
        FishingHook hook = createFishingHook(helper);
        FabricFishingState.minWaitTime(hook, 40);
        FabricFishingState.maxWaitTime(hook, 80);
        assertFishingScriptSetsMarker(
                helper,
                "skript/gametest/expression/fishing_wait_time_marks_block.sk",
                hook,
                new BlockPos(3, 1, 0),
                Blocks.GOLD_BLOCK,
                () -> {
                    helper.assertTrue(
                            FabricFishingState.minWaitTime(hook) == 5,
                            Component.literal("Expected fishing wait time script to set the minimum wait time to 5 ticks.")
                    );
                    helper.assertTrue(
                            FabricFishingState.maxWaitTime(hook) == 10,
                            Component.literal("Expected fishing wait time script to set the maximum wait time to 10 ticks.")
                    );
                }
        );
    }

    @GameTest
    public void fishingBiteTimeExpressionExecutesRealScript(GameTestHelper helper) {
        FishingHook hook = createFishingHook(helper);
        PrivateFishingHookAccess.setTimeUntilHooked(hook, 20);
        assertFishingScriptSetsMarker(
                helper,
                "skript/gametest/expression/fishing_bite_time_marks_block.sk",
                hook,
                new BlockPos(4, 1, 0),
                Blocks.IRON_BLOCK,
                () -> helper.assertTrue(
                        PrivateFishingHookAccess.timeUntilHooked(hook) == 7,
                        Component.literal("Expected fishing bite time script to set the bite time to 7 ticks.")
                )
        );
    }

    @GameTest
    public void fishingApproachAngleExpressionExecutesRealScript(GameTestHelper helper) {
        FishingHook hook = createFishingHook(helper);
        FabricFishingState.minLureAngle(hook, 0.0F);
        FabricFishingState.maxLureAngle(hook, 360.0F);
        assertFishingScriptSetsMarker(
                helper,
                "skript/gametest/expression/fishing_approach_angle_marks_block.sk",
                hook,
                new BlockPos(5, 1, 0),
                Blocks.DIAMOND_BLOCK,
                () -> {
                    float minimumAngle = FabricFishingState.minLureAngle(hook);
                    float maximumAngle = FabricFishingState.maxLureAngle(hook);
                    helper.assertTrue(
                            Float.compare(minimumAngle, 30.0F) == 0,
                            Component.literal("Expected fishing approach angle script to set the minimum angle to 30 degrees, got " + minimumAngle + ".")
                    );
                    helper.assertTrue(
                            Float.compare(maximumAngle, 300.0F) == 0,
                            Component.literal("Expected fishing approach angle script to set the maximum angle to 300 degrees, got " + maximumAngle + ".")
                    );
                }
        );
    }

    @GameTest
    public void currentInputKeysExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/current_input_keys_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(8, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricPlayerInputHandle(
                            helper.getLevel(),
                            player,
                            Input.EMPTY,
                            new Input(true, false, false, false, false, false, false)
                    ),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected current input keys script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GREEN_WOOL),
                    Component.literal("Expected current input keys expression script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void pastCurrentInputKeysExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/past_current_input_keys_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(9, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricPlayerInputHandle(
                            helper.getLevel(),
                            player,
                            new Input(true, false, false, false, false, false, false),
                            Input.EMPTY
                    ),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected past current input keys script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.BLUE_CONCRETE),
                    Component.literal("Expected past current input keys expression to expose the previous input state.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void causingEntityExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/causing_entity_names_target.sk");

            Cow victim = createCow(helper, false);
            Cow attacker = createCow(helper, false);
            attacker.setCustomName(Component.literal("named attacker"));

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().mobAttack(attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected causing entity expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "caused target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected causing entity expression script to rename the damaged entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void directEntityExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/direct_entity_names_target.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            ArmorStand projectile = new ArmorStand(helper.getLevel(), 1.5D, 1.0D, 0.5D);
            projectile.setCustomName(Component.literal("named projectile"));
            helper.getLevel().addFreshEntity(projectile);

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().thrown(projectile, attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected direct entity expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "direct target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected direct entity expression script to rename the damaged entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void sourceLocationExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/source_location_sets_block.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            BlockPos markerRelative = new BlockPos(4, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            ArmorStand projectile = new ArmorStand(
                    helper.getLevel(),
                    markerAbsolute.getX() + 0.5D,
                    markerAbsolute.getY() + 0.5D,
                    markerAbsolute.getZ() + 0.5D
            );
            helper.getLevel().addFreshEntity(projectile);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().thrown(projectile, attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected source location expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertBlockPresent(Blocks.RED_WOOL, markerRelative);
            runtime.clearScripts();
        });
    }

    @GameTest
    public void damageLocationExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/damage_location_sets_block.sk");

            Cow victim = createCow(helper, false);
            BlockPos markerRelative = new BlockPos(5, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().badRespawnPointExplosion(Vec3.atCenterOf(markerAbsolute)),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage location expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertBlockPresent(Blocks.BLUE_WOOL, markerRelative);
            runtime.clearScripts();
        });
    }

    @GameTest
    public void foodExhaustionExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            DamageSource damageSource = helper.getLevel().damageSources().playerAttack(attacker);
            Path scriptPath = writeTempScript(
                    "food_exhaustion_expression",
                    """
                    on damage:
                        food exhaustion of event-damage source is "%s"
                        set test name of entity event-entity to "food exhaustion target"
                    """.formatted(Float.toString(damageSource.getFoodExhaustion()))
            );
            try {
                runtime.loadFromPath(scriptPath);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load temporary food exhaustion script.", exception);
            }

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    damageSource,
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected food exhaustion expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "food exhaustion target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected food exhaustion expression script to rename the damaged entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingFuelLevelExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/brewing_fuel_level_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(9, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for fuel level expression test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }
            PrivateBlockEntityAccess.setBrewingFuel(brewingStand, 20);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(brewingAbsolute.getX() + 0.5D, brewingAbsolute.getY() + 1.0D, brewingAbsolute.getZ() + 0.5D);

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected brewing fuel level expression script to keep Fabric callback flow in PASS state.")
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(9, 2, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingTimeExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/brewing_time_sets_block.sk");

            BlockPos brewingRelative = new BlockPos(10, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing time expression test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }
            PrivateBlockEntityAccess.setBrewingTime(brewingStand, 5);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(brewingAbsolute.getX() + 0.5D, brewingAbsolute.getY() + 1.0D, brewingAbsolute.getZ() + 0.5D);

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected brewing time expression script to keep Fabric callback flow in PASS state.")
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(10, 2, 0));
            helper.assertTrue(
                    PrivateBlockEntityAccess.brewingTime(brewingStand) == 6,
                    Component.literal("Expected brewing time expression script to change the brewing time to 6 ticks.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void textDisplayAlignmentExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_ALIGN_LEFT);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/text_display_alignment_names_entity.sk",
                textDisplay,
                "left aligned text"
        );
    }

    @GameTest
    public void textDisplayLineWidthExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/text_display_line_width_names_entity.sk",
                textDisplay,
                "wide text"
        );
    }

    @GameTest
    public void textDisplayOpacityExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/text_display_opacity_names_entity.sk",
                textDisplay,
                "semi transparent text"
        );
    }

    @GameTest
    public void displayBillboardExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayBillboardConstraints(textDisplay, Display.BillboardConstraints.CENTER);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_billboard_names_entity.sk",
                textDisplay,
                "center billboard"
        );
    }

    @GameTest
    public void displayBrightnessExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(4, 7));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_brightness_names_entity.sk",
                textDisplay,
                "bright text"
        );
    }

    @GameTest
    public void displayHeightExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayHeight(textDisplay, 2.5F);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_height_names_entity.sk",
                textDisplay,
                "tall text"
        );
    }

    @GameTest
    public void displayShadowExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayShadowRadius(textDisplay, 1.75F);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_shadow_names_entity.sk",
                textDisplay,
                "shadow radius text"
        );
    }

    @GameTest
    public void displayViewRangeExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_view_range_names_entity.sk",
                textDisplay,
                "long range text"
        );
    }

    @GameTest
    public void displayInterpolationDelayExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/expression/display_interpolation_delay_names_entity.sk",
                textDisplay,
                new BlockPos(9, 1, 0),
                Blocks.RED_WOOL,
                () -> helper.assertTrue(
                        PrivateEntityAccess.displayTransformationInterpolationDelay(textDisplay) == 3,
                        Component.literal("Expected interpolation delay script to update Mojang display interpolation delay.")
                )
        );
    }

    @GameTest
    public void displayInterpolationDurationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/expression/display_interpolation_duration_names_entity.sk",
                textDisplay,
                new BlockPos(10, 1, 0),
                Blocks.BLUE_WOOL,
                () -> helper.assertTrue(
                        PrivateEntityAccess.displayTransformationInterpolationDuration(textDisplay) == 4,
                        Component.literal("Expected interpolation duration script to update Mojang display interpolation duration.")
                )
        );
    }

    @GameTest
    public void displayTeleportDurationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/expression/display_teleport_duration_names_entity.sk",
                textDisplay,
                new BlockPos(11, 1, 0),
                Blocks.LIME_WOOL,
                () -> helper.assertTrue(
                        PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 2,
                        Component.literal("Expected teleport duration script to update Mojang display position interpolation duration.")
                )
        );
    }

    @GameTest
    public void displayTransformationTranslationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(1.25F, 2.5F, 3.75F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_translation_names_entity.sk",
                textDisplay,
                "translated text"
        );
    }

    @GameTest
    public void displayTransformationScaleExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(2.0F, 3.0F, 4.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_scale_names_entity.sk",
                textDisplay,
                "scaled text"
        );
    }

    @GameTest
    public void displayLeftRotationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.1F, 0.2F, 0.3F, 0.4F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_left_rotation_names_entity.sk",
                textDisplay,
                "left rotated text"
        );
    }

    @GameTest
    public void displayRightRotationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.5F, 0.6F, 0.7F, 0.8F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/display_right_rotation_names_entity.sk",
                textDisplay,
                "right rotated text"
        );
    }

    @GameTest
    public void itemDisplayTransformExpressionExecutesRealScript(GameTestHelper helper) {
        Display.ItemDisplay itemDisplay = createItemDisplay(helper, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/item_display_transform_names_entity.sk",
                itemDisplay,
                "left hand transform"
        );
    }

    @GameTest
    public void loveTimeExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/expression/love_time_marks_block.sk",
                cow,
                new BlockPos(12, 1, 0),
                Blocks.BLUE_WOOL,
                () -> helper.assertTrue(
                        cow.getInLoveTime() == 30 * 20,
                        Component.literal("Expected love time expression script to set the animal love time to 30 seconds.")
                )
        );
    }

    @GameTest
    public void interactionDimensionsExpressionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/expression/interaction_dimensions_marks_block.sk",
                interaction,
                new BlockPos(13, 1, 0),
                Blocks.RED_WOOL,
                () -> {
                    helper.assertTrue(
                            Float.compare(PrivateEntityAccess.interactionWidth(interaction), 2.0F) == 0,
                            Component.literal("Expected interaction width expression script to set the interaction width to 2.")
                    );
                    helper.assertTrue(
                            Float.compare(PrivateEntityAccess.interactionHeight(interaction), 3.0F) == 0,
                            Component.literal("Expected interaction height expression script to set the interaction height to 3.")
                    );
                }
        );
    }

    @GameTest
    public void lastClickedInteractionPlayerExpressionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/last_click_player_names_entity.sk",
                interaction,
                "clicked interaction"
        );
    }

    @GameTest
    public void lastAttackedInteractionPlayerExpressionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertAttackEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/last_attack_player_names_entity.sk",
                interaction,
                "attacked interaction"
        );
    }

    @GameTest
    public void lastInteractedInteractionPlayerExpressionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/last_interact_player_names_entity.sk",
                interaction,
                "interacted interaction"
        );
    }

    @GameTest
    public void lastInteractedInteractionDateExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/last_interact_time_names_entity.sk");

            Interaction interaction = createInteraction(helper, true);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(interaction.getX(), interaction.getY() + 1.0D, interaction.getZ());

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    interaction,
                    new EntityHitResult(interaction)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected interaction date expression test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    interaction.getCustomName() != null && "interaction date matched".equals(interaction.getCustomName().getString()),
                    Component.literal("Expected original interaction date expressions to resolve to the same timestamp.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void lastAttackedInteractionDateExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/last_attack_time_names_entity.sk");

            Interaction interaction = createInteraction(helper, true);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(interaction.getX(), interaction.getY() + 1.0D, interaction.getZ());

            InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    interaction,
                    new EntityHitResult(interaction)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected attack date expression test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    interaction.getCustomName() != null && "attack date matched".equals(interaction.getCustomName().getString()),
                    Component.literal("Expected original attack date expressions to resolve to the same timestamp.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void lootTableExpressionExecutesRealScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/loot_table_expression_names_entity.sk",
                chestMinecart,
                "loot table expression"
        );
    }

    @GameTest
    public void lootTableSetterExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/loot_table_setter_names_entity.sk");

            MinecartChest chestMinecart = createChestMinecart(helper, false);
            Trigger trigger = getOnlyLoadedTrigger(runtime);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(chestMinecart.getX(), chestMinecart.getY() + 1.0D, chestMinecart.getZ());
            EntityHitResult hitResult = new EntityHitResult(chestMinecart);
            var skriptEvent = new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, chestMinecart, hitResult),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    chestMinecart,
                    hitResult
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected loot table setter script to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    chestMinecart.getCustomName() != null && "loot table set".equals(chestMinecart.getCustomName().getString()),
                    Component.literal("Expected loot table setter script to name the chest minecart. Items: "
                            + describeTriggerItems(trigger)
                            + ", fields0: " + describeExpressionFields(getTriggerItem(trigger, 0), skriptEvent)
                            + ", fields1: " + describeExpressionFields(getTriggerItem(trigger, 1), skriptEvent)
                            + ", fields2: " + describeExpressionFields(getTriggerItem(trigger, 2), skriptEvent))
            );
            helper.assertTrue(
                    BuiltInLootTables.SIMPLE_DUNGEON.equals(chestMinecart.getContainerLootTable()),
                    Component.literal("Expected loot table setter script to assign the simple dungeon loot table to the chest minecart. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    chestMinecart.getContainerLootTableSeed() == 23L,
                    Component.literal("Expected loot table setter script to assign the configured seed to the chest minecart. Items: "
                            + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void lootTableBlockExpressionsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/loot_table_block_sets_marker.sk");
            Trigger trigger = getOnlyLoadedTrigger(runtime);

            BlockPos chestRelative = new BlockPos(14, 1, 0);
            BlockPos chestAbsolute = helper.absolutePos(chestRelative);
            helper.getLevel().setBlockAndUpdate(chestAbsolute, Blocks.CHEST.defaultBlockState());

            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestAbsolute);
            helper.assertTrue(
                    chest != null,
                    Component.literal("Expected chest block entity to exist for loot table block expression test.")
            );
            if (chest == null) {
                throw new IllegalStateException("Chest block entity was not created.");
            }

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(chestAbsolute.getX() + 0.5D, chestAbsolute.getY() + 1.0D, chestAbsolute.getZ() + 0.5D);
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(chestAbsolute), Direction.UP, chestAbsolute, false);
            var skriptEvent = new org.skriptlang.skript.lang.event.SkriptEvent(
                    new org.skriptlang.skript.fabric.runtime.FabricUseBlockHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, hitResult),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    hitResult
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected loot table block expression script to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(14, 2, 0))).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected loot table block expression script to mark the block above the chest. Items: "
                            + describeTriggerItems(trigger)
                            + ", fields0: " + describeExpressionFields(getTriggerItem(trigger, 0), skriptEvent)
                            + ", fields1: " + describeExpressionFields(getTriggerItem(trigger, 1), skriptEvent)
                            + ", fields2: " + describeExpressionFields(getTriggerItem(trigger, 2), skriptEvent))
            );
            helper.assertTrue(
                    BuiltInLootTables.SIMPLE_DUNGEON.equals(chest.getLootTable()),
                    Component.literal("Expected loot table block expression script to assign the simple dungeon loot table to the chest block entity. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    chest.getLootTableSeed() == 29L,
                    Component.literal("Expected loot table block expression script to assign the configured seed to the chest block entity. Items: "
                            + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectsExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/potion_effects_names_entity.sk",
                cow,
                "active potion effects"
        );
    }

    @GameTest
    public void specificPotionEffectExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/potion_effect_names_entity.sk",
                cow,
                "specific potion effect"
        );
    }

    @GameTest
    public void potionDurationExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/potion_duration_names_entity.sk",
                cow,
                "potion duration"
        );
    }

    @GameTest
    public void potionAmplifierExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/potion_amplifier_names_entity.sk",
                cow,
                "potion amplifier"
        );
    }

    @GameTest
    public void potionCategoryExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/potion_category_names_entity.sk",
                cow,
                "potion category"
        );
    }

    @GameTest
    public void blankEquippableComponentSectionPropagatesLocalVariables(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/blank_equippable_component_section_propagates_locals_names_entity.sk",
                cow,
                "blank equippable locals"
        );
    }

    @GameTest
    public void plainEffectArgumentParsesInsideOuterExpressionSection(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/plain_effect_argument_inside_outer_section_expression_names_entity.sk",
                cow,
                "nested effect arg"
        );
    }

    @GameTest
    public void customDamageSourceSectionPropagatesLocalVariables(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/custom_damage_source_section_propagates_locals_names_entity.sk",
                cow,
                "custom damage source locals"
        );
    }

    @GameTest
    public void potionEffectSectionPropagatesLocalVariables(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/potion_effect_section_propagates_locals_names_entity.sk",
                cow,
                "potion effect locals"
        );
    }

    @GameTest
    public void lootContextSectionPropagatesLocalVariables(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/loot_context_section_propagates_locals_names_entity.sk",
                cow,
                "loot context locals"
        );
    }

    @GameTest
    public void eventPayloadExpressionsParseAndResolve(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 0));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        @SuppressWarnings("unchecked")
        Expression<? extends ServerPlayer> eventPlayerExpression = new SkriptParser(
                "event-player",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{ServerPlayer.class});
        helper.assertTrue(
                eventPlayerExpression != null,
                Component.literal("Expected event-player expression to parse from registry.")
        );
        if (eventPlayerExpression == null) {
            throw new IllegalStateException("event-player expression did not parse");
        }

        ServerPlayer resolvedPlayer = eventPlayerExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                null,
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedPlayer == player,
                Component.literal("event-player expression should resolve the event player.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends FabricBlock> eventBlockExpression = new SkriptParser(
                "event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{FabricBlock.class});
        helper.assertTrue(
                eventBlockExpression != null,
                Component.literal("Expected event-block expression to parse from registry.")
        );
        if (eventBlockExpression == null) {
            throw new IllegalStateException("event-block expression did not parse");
        }

        FabricBlockBreakHandle handle = new FabricBlockBreakHandle(
                helper.getLevel(),
                player,
                pos,
                helper.getLevel().getBlockState(pos),
                helper.getLevel().getBlockEntity(pos)
        );
        FabricBlock resolvedBlock = eventBlockExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedBlock != null && resolvedBlock.position().equals(pos),
                Component.literal("event-block expression should resolve the broken block position.")
        );

        ArmorStand armorStand = new ArmorStand(helper.getLevel(), pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(armorStand);

        @SuppressWarnings("unchecked")
        Expression<? extends net.minecraft.world.entity.Entity> eventEntityExpression = new SkriptParser(
                "event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{net.minecraft.world.entity.Entity.class});
        helper.assertTrue(
                eventEntityExpression != null,
                Component.literal("Expected event-entity expression to parse from registry.")
        );
        if (eventEntityExpression == null) {
            throw new IllegalStateException("event-entity expression did not parse");
        }

        net.minecraft.world.entity.Entity resolvedEntity = eventEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        armorStand,
                        new EntityHitResult(armorStand)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedEntity == armorStand,
                Component.literal("event-entity expression should resolve the interacted entity.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends ItemStack> eventItemExpression = new SkriptParser(
                "event-item",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{ItemStack.class});
        helper.assertTrue(
                eventItemExpression != null,
                Component.literal("Expected event-item expression to parse from registry.")
        );
        if (eventItemExpression == null) {
            throw new IllegalStateException("event-item expression did not parse");
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
        ItemStack resolvedItem = eventItemExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseItemHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedItem != null && resolvedItem.is(Items.APPLE),
                Component.literal("event-item expression should resolve the used item stack.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends ItemStack> futureEventItemExpression = parseExpressionInEvent(
                "future event-item",
                new Class[]{ItemStack.class},
                FabricBucketCatchEventHandle.class
        );
        helper.assertTrue(
                futureEventItemExpression != null,
                Component.literal("Expected future event-item expression to parse inside bucket catch events.")
        );
        if (futureEventItemExpression == null) {
            throw new IllegalStateException("future event-item expression did not parse");
        }

        ItemStack resolvedFutureItem = futureEventItemExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBucketCatchHandle(
                        helper.getLevel(),
                        player,
                        armorStand,
                        new ItemStack(Items.WATER_BUCKET),
                        new ItemStack(Items.PUFFERFISH_BUCKET)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedFutureItem != null && resolvedFutureItem.is(Items.PUFFERFISH_BUCKET),
                Component.literal("future event-item expression should resolve the future bucketed item.")
        );

        net.minecraft.world.entity.Entity attackedEntity = eventEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricAttackEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        armorStand,
                        new EntityHitResult(armorStand)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                attackedEntity == armorStand,
                Component.literal("event-entity expression should also resolve attacked entities.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends DamageSource> eventDamageSourceExpression = new SkriptParser(
                "event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{DamageSource.class});
        helper.assertTrue(
                eventDamageSourceExpression != null,
                Component.literal("Expected event-damage source expression to parse from registry.")
        );
        if (eventDamageSourceExpression == null) {
            throw new IllegalStateException("event-damage source expression did not parse");
        }

        DamageSource expectedDamageSource = helper.getLevel().damageSources().playerAttack(player);
        DamageSource resolvedDamageSource = eventDamageSourceExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        expectedDamageSource,
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedDamageSource == expectedDamageSource,
                Component.literal("event-damage source expression should resolve the current Mojang damage source.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends net.minecraft.world.entity.Entity> causingEntityExpression = new SkriptParser(
                "causing entity of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{net.minecraft.world.entity.Entity.class});
        helper.assertTrue(
                causingEntityExpression != null,
                Component.literal("Expected causing entity expression to parse from registry.")
        );
        if (causingEntityExpression == null) {
            throw new IllegalStateException("causing entity expression did not parse");
        }
        Cow causingAttacker = createCow(helper, false);
        net.minecraft.world.entity.Entity resolvedCausingEntity = causingEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().mobAttack(causingAttacker),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedCausingEntity == causingAttacker,
                Component.literal("Expected causing entity expression to resolve the current damage source attacker.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends net.minecraft.world.entity.Entity> directEntityExpression = new SkriptParser(
                "direct entity of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{net.minecraft.world.entity.Entity.class});
        helper.assertTrue(
                directEntityExpression != null,
                Component.literal("Expected direct entity expression to parse from registry.")
        );
        if (directEntityExpression == null) {
            throw new IllegalStateException("direct entity expression did not parse");
        }
        ArmorStand damageProjectile = new ArmorStand(helper.getLevel(), 2.5D, 1.0D, 0.5D);
        helper.getLevel().addFreshEntity(damageProjectile);
        net.minecraft.world.entity.Entity resolvedDirectEntity = directEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().thrown(damageProjectile, player),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedDirectEntity == damageProjectile,
                Component.literal("Expected direct entity expression to resolve the current damage projectile.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends FabricLocation> sourceLocationExpression = new SkriptParser(
                "source location of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{FabricLocation.class});
        helper.assertTrue(
                sourceLocationExpression != null,
                Component.literal("Expected source location expression to parse from registry.")
        );
        if (sourceLocationExpression == null) {
            throw new IllegalStateException("source location expression did not parse");
        }
        FabricLocation resolvedSourceLocation = sourceLocationExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().thrown(damageProjectile, player),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedSourceLocation != null
                        && BlockPos.containing(resolvedSourceLocation.position()).equals(BlockPos.containing(damageProjectile.position())),
                Component.literal("Expected source location expression to resolve the direct entity position.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends FabricLocation> damageLocationExpression = new SkriptParser(
                "damage location of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{FabricLocation.class});
        helper.assertTrue(
                damageLocationExpression != null,
                Component.literal("Expected damage location expression to parse from registry.")
        );
        if (damageLocationExpression == null) {
            throw new IllegalStateException("damage location expression did not parse");
        }
        Vec3 explicitDamagePosition = Vec3.atCenterOf(helper.absolutePos(new BlockPos(14, 1, 0)));
        FabricLocation resolvedDamageLocation = damageLocationExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().badRespawnPointExplosion(explicitDamagePosition),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedDamageLocation != null
                        && BlockPos.containing(resolvedDamageLocation.position()).equals(BlockPos.containing(explicitDamagePosition)),
                Component.literal("Expected damage location expression to resolve the explicit damage source position.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> foodExhaustionExpression = new SkriptParser(
                "food exhaustion of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(
                foodExhaustionExpression != null,
                Component.literal("Expected food exhaustion expression to parse from registry.")
        );
        if (foodExhaustionExpression == null) {
            throw new IllegalStateException("food exhaustion expression did not parse");
        }
        DamageSource foodExhaustionSource = helper.getLevel().damageSources().playerAttack(player);
        Float resolvedFoodExhaustion = foodExhaustionExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        foodExhaustionSource,
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedFoodExhaustion != null && Float.compare(resolvedFoodExhaustion, foodExhaustionSource.getFoodExhaustion()) == 0,
                Component.literal("Expected food exhaustion expression to resolve the current damage source food exhaustion.")
        );
        helper.succeed();
    }

    @GameTest
    public void textDisplayExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_ALIGN_LEFT);
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Display.TextDisplay.Align> alignmentExpression = new SkriptParser(
                "text alignment of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Display.TextDisplay.Align.class});
        helper.assertTrue(
                alignmentExpression != null,
                Component.literal("Expected text alignment expression to parse from registry.")
        );
        if (alignmentExpression == null) {
            throw new IllegalStateException("text alignment expression did not parse");
        }
        helper.assertTrue(
                alignmentExpression.getSingle(event) == Display.TextDisplay.Align.LEFT,
                Component.literal("Expected text alignment expression to resolve the current text display alignment.")
        );
        alignmentExpression.change(event, new Object[]{Display.TextDisplay.Align.RIGHT}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayAlignment(textDisplay) == Display.TextDisplay.Align.RIGHT,
                Component.literal("Expected text alignment expression to apply SET changes.")
        );
        alignmentExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayAlignment(textDisplay) == Display.TextDisplay.Align.CENTER,
                Component.literal("Expected text alignment expression to reset to center.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> lineWidthExpression = new SkriptParser(
                "line width of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(
                lineWidthExpression != null,
                Component.literal("Expected line width expression to parse from registry.")
        );
        if (lineWidthExpression == null) {
            throw new IllegalStateException("line width expression did not parse");
        }
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        helper.assertTrue(
                Integer.valueOf(300).equals(lineWidthExpression.getSingle(event)),
                Component.literal("Expected line width expression to resolve the current line width.")
        );
        lineWidthExpression.change(event, new Object[]{20}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayLineWidth(textDisplay) == 320,
                Component.literal("Expected line width expression to apply ADD changes.")
        );
        lineWidthExpression.change(event, new Object[]{50}, ChangeMode.REMOVE);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayLineWidth(textDisplay) == 270,
                Component.literal("Expected line width expression to apply REMOVE changes.")
        );
        lineWidthExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayLineWidth(textDisplay) == 200,
                Component.literal("Expected line width expression to reset to the vanilla default.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> opacityExpression = new SkriptParser(
                "text opacity of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(
                opacityExpression != null,
                Component.literal("Expected text opacity expression to parse from registry.")
        );
        if (opacityExpression == null) {
            throw new IllegalStateException("text opacity expression did not parse");
        }
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);
        helper.assertTrue(
                Integer.valueOf(128).equals(opacityExpression.getSingle(event)),
                Component.literal("Expected text opacity expression to resolve the current unsigned opacity.")
        );
        opacityExpression.change(event, new Object[]{4}, ChangeMode.SET);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 4,
                Component.literal("Expected text opacity expression to apply SET changes.")
        );
        opacityExpression.change(event, new Object[]{20}, ChangeMode.ADD);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 24,
                Component.literal("Expected text opacity expression to apply ADD changes.")
        );
        opacityExpression.change(event, new Object[]{30}, ChangeMode.REMOVE);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 0,
                Component.literal("Expected text opacity expression to clamp REMOVE changes at zero.")
        );
        opacityExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 255,
                Component.literal("Expected text opacity expression to reset to fully opaque.")
        );

        Condition alignmentCondition = Condition.parse("text alignment of event-entity is \"left aligned\"", null);
        helper.assertTrue(
                alignmentCondition != null,
                Component.literal("Expected text alignment comparison condition to parse.")
        );
        if (alignmentCondition == null) {
            throw new IllegalStateException("text alignment comparison condition did not parse");
        }
        PrivateEntityAccess.setTextDisplayAlignment(textDisplay, Display.TextDisplay.Align.LEFT);
        helper.assertTrue(
                alignmentCondition.check(event),
                Component.literal("Expected text alignment comparison condition to evaluate true.")
        );

        Condition lineWidthCondition = Condition.parse("line width of event-entity is \"300\"", null);
        helper.assertTrue(
                lineWidthCondition != null,
                Component.literal("Expected line width comparison condition to parse.")
        );
        if (lineWidthCondition == null) {
            throw new IllegalStateException("line width comparison condition did not parse");
        }
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        helper.assertTrue(
                lineWidthCondition.check(event),
                Component.literal("Expected line width comparison condition to evaluate true.")
        );

        Condition opacityCondition = Condition.parse("text opacity of event-entity is \"128\"", null);
        helper.assertTrue(
                opacityCondition != null,
                Component.literal("Expected text opacity comparison condition to parse.")
        );
        if (opacityCondition == null) {
            throw new IllegalStateException("text opacity comparison condition did not parse");
        }
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);
        helper.assertTrue(
                opacityCondition.check(event),
                Component.literal("Expected text opacity comparison condition to evaluate true.")
        );
        helper.succeed();
    }

    @GameTest
    public void genericDisplayExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayBillboardConstraints(textDisplay, Display.BillboardConstraints.CENTER);
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(4, 7));
        PrivateEntityAccess.setDisplayHeight(textDisplay, 2.5F);
        PrivateEntityAccess.setDisplayWidth(textDisplay, 1.5F);
        PrivateEntityAccess.setDisplayShadowRadius(textDisplay, 1.75F);
        PrivateEntityAccess.setDisplayShadowStrength(textDisplay, 0.5F);
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Display.BillboardConstraints> billboardExpression = new SkriptParser(
                "billboard of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Display.BillboardConstraints.class});
        helper.assertTrue(billboardExpression != null, Component.literal("Expected billboard expression to parse from registry."));
        if (billboardExpression == null) {
            throw new IllegalStateException("billboard expression did not parse");
        }
        helper.assertTrue(
                billboardExpression.getSingle(event) == Display.BillboardConstraints.CENTER,
                Component.literal("Expected billboard expression to resolve the current billboard setting.")
        );
        billboardExpression.change(event, new Object[]{Display.BillboardConstraints.VERTICAL}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayBillboardConstraints(textDisplay) == Display.BillboardConstraints.VERTICAL,
                Component.literal("Expected billboard expression to apply SET changes.")
        );
        billboardExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayBillboardConstraints(textDisplay) == Display.BillboardConstraints.FIXED,
                Component.literal("Expected billboard expression to reset to fixed.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> viewRangeExpression = new SkriptParser(
                "view range of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(viewRangeExpression != null, Component.literal("Expected view range expression to parse from registry."));
        if (viewRangeExpression == null) {
            throw new IllegalStateException("view range expression did not parse");
        }
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);
        helper.assertTrue(
                Math.abs(viewRangeExpression.getSingle(event) - 2.5F) < 0.0001F,
                Component.literal("Expected view range expression to resolve the current view range.")
        );
        viewRangeExpression.change(event, new Object[]{0.5F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayViewRange(textDisplay) - 3.0F) < 0.0001F,
                Component.literal("Expected view range expression to apply ADD changes.")
        );
        viewRangeExpression.change(event, new Object[]{1.0F}, ChangeMode.REMOVE);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayViewRange(textDisplay) - 2.0F) < 0.0001F,
                Component.literal("Expected view range expression to apply REMOVE changes.")
        );
        viewRangeExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayViewRange(textDisplay) - 1.0F) < 0.0001F,
                Component.literal("Expected view range expression to reset to the vanilla default.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> heightExpression = new SkriptParser(
                "display height of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(heightExpression != null, Component.literal("Expected display height expression to parse from registry."));
        if (heightExpression == null) {
            throw new IllegalStateException("display height expression did not parse");
        }
        PrivateEntityAccess.setDisplayHeight(textDisplay, 2.5F);
        helper.assertTrue(
                Math.abs(heightExpression.getSingle(event) - 2.5F) < 0.0001F,
                Component.literal("Expected display height expression to resolve the current display height.")
        );
        heightExpression.change(event, new Object[]{1.0F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayHeight(textDisplay) - 3.5F) < 0.0001F,
                Component.literal("Expected display height expression to apply ADD changes.")
        );
        heightExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayHeight(textDisplay)) < 0.0001F,
                Component.literal("Expected display height expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> widthExpression = new SkriptParser(
                "display width of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(widthExpression != null, Component.literal("Expected display width expression to parse from registry."));
        if (widthExpression == null) {
            throw new IllegalStateException("display width expression did not parse");
        }
        PrivateEntityAccess.setDisplayWidth(textDisplay, 1.5F);
        helper.assertTrue(
                Math.abs(widthExpression.getSingle(event) - 1.5F) < 0.0001F,
                Component.literal("Expected display width expression to resolve the current display width.")
        );
        widthExpression.change(event, new Object[]{0.5F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayWidth(textDisplay) - 2.0F) < 0.0001F,
                Component.literal("Expected display width expression to apply ADD changes.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> shadowRadiusExpression = new SkriptParser(
                "shadow radius of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(shadowRadiusExpression != null, Component.literal("Expected shadow radius expression to parse from registry."));
        if (shadowRadiusExpression == null) {
            throw new IllegalStateException("shadow radius expression did not parse");
        }
        PrivateEntityAccess.setDisplayShadowRadius(textDisplay, 1.75F);
        helper.assertTrue(
                Math.abs(shadowRadiusExpression.getSingle(event) - 1.75F) < 0.0001F,
                Component.literal("Expected shadow radius expression to resolve the current shadow radius.")
        );
        shadowRadiusExpression.change(event, new Object[]{0.25F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowRadius(textDisplay) - 2.0F) < 0.0001F,
                Component.literal("Expected shadow radius expression to apply ADD changes.")
        );
        shadowRadiusExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowRadius(textDisplay)) < 0.0001F,
                Component.literal("Expected shadow radius expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> shadowStrengthExpression = new SkriptParser(
                "shadow strength of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(shadowStrengthExpression != null, Component.literal("Expected shadow strength expression to parse from registry."));
        if (shadowStrengthExpression == null) {
            throw new IllegalStateException("shadow strength expression did not parse");
        }
        PrivateEntityAccess.setDisplayShadowStrength(textDisplay, 0.5F);
        helper.assertTrue(
                Math.abs(shadowStrengthExpression.getSingle(event) - 0.5F) < 0.0001F,
                Component.literal("Expected shadow strength expression to resolve the current shadow strength.")
        );
        shadowStrengthExpression.change(event, new Object[]{0.25F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowStrength(textDisplay) - 0.75F) < 0.0001F,
                Component.literal("Expected shadow strength expression to apply ADD changes.")
        );
        shadowStrengthExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowStrength(textDisplay) - 1.0F) < 0.0001F,
                Component.literal("Expected shadow strength expression to reset to one.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> skyLightExpression = new SkriptParser(
                "sky light override of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(skyLightExpression != null, Component.literal("Expected sky light override expression to parse from registry."));
        if (skyLightExpression == null) {
            throw new IllegalStateException("sky light override expression did not parse");
        }
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(4, 7));
        helper.assertTrue(
                Integer.valueOf(7).equals(skyLightExpression.getSingle(event)),
                Component.literal("Expected sky light override expression to resolve the current sky light value.")
        );
        skyLightExpression.change(event, new Object[]{2}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay).sky() == 9,
                Component.literal("Expected sky light override expression to apply ADD changes.")
        );
        skyLightExpression.change(event, new Object[]{20}, ChangeMode.REMOVE);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay).sky() == 0,
                Component.literal("Expected sky light override expression to clamp REMOVE changes at zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> brightnessExpression = new SkriptParser(
                "brightness override of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(brightnessExpression != null, Component.literal("Expected brightness override expression to parse from registry."));
        if (brightnessExpression == null) {
            throw new IllegalStateException("brightness override expression did not parse");
        }
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(3, 6));
        Integer[] brightnessValues = brightnessExpression.getAll(event);
        helper.assertTrue(
                brightnessValues.length == 2 && brightnessValues[0] == 3 && brightnessValues[1] == 6,
                Component.literal("Expected brightness override expression to resolve both block and sky light values.")
        );
        brightnessExpression.change(event, new Object[]{9}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay).block() == 9
                        && PrivateEntityAccess.displayBrightnessOverride(textDisplay).sky() == 9,
                Component.literal("Expected brightness override expression to apply SET changes to both light channels.")
        );
        brightnessExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay) == null,
                Component.literal("Expected brightness override expression to clear the override on RESET.")
        );

        Condition billboardCondition = Condition.parse("billboard of event-entity is \"center\"", null);
        helper.assertTrue(billboardCondition != null, Component.literal("Expected billboard comparison condition to parse."));
        if (billboardCondition == null) {
            throw new IllegalStateException("billboard comparison condition did not parse");
        }
        PrivateEntityAccess.setDisplayBillboardConstraints(textDisplay, Display.BillboardConstraints.CENTER);
        helper.assertTrue(
                billboardCondition.check(event),
                Component.literal("Expected billboard comparison condition to evaluate true.")
        );

        Condition viewRangeCondition = Condition.parse("view range of event-entity is \"2.5\"", null);
        helper.assertTrue(viewRangeCondition != null, Component.literal("Expected view range comparison condition to parse."));
        if (viewRangeCondition == null) {
            throw new IllegalStateException("view range comparison condition did not parse");
        }
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);
        helper.assertTrue(
                viewRangeCondition.check(event),
                Component.literal("Expected view range comparison condition to evaluate true.")
        );
        helper.succeed();
    }

    @GameTest
    public void displayTimeExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformationInterpolationDelay(textDisplay, 3);
        PrivateEntityAccess.setDisplayTransformationInterpolationDuration(textDisplay, 4);
        PrivateEntityAccess.setDisplayPosRotInterpolationDuration(textDisplay, 2);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> interpolationDelayExpression = new SkriptParser(
                "interpolation delay of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(interpolationDelayExpression != null, Component.literal("Expected interpolation delay expression to parse from registry."));
        if (interpolationDelayExpression == null) {
            throw new IllegalStateException("interpolation delay expression did not parse");
        }
        helper.assertTrue(
                interpolationDelayExpression.getSingle(event).getAs(TimePeriod.TICK) == 3,
                Component.literal("Expected interpolation delay expression to resolve the current delay.")
        );
        interpolationDelayExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 2)}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.displayTransformationInterpolationDelay(textDisplay) == 5,
                Component.literal("Expected interpolation delay expression to apply ADD changes.")
        );
        interpolationDelayExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayTransformationInterpolationDelay(textDisplay) == 0,
                Component.literal("Expected interpolation delay expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> interpolationDurationExpression = new SkriptParser(
                "interpolation duration of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(interpolationDurationExpression != null, Component.literal("Expected interpolation duration expression to parse from registry."));
        if (interpolationDurationExpression == null) {
            throw new IllegalStateException("interpolation duration expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformationInterpolationDuration(textDisplay, 4);
        helper.assertTrue(
                interpolationDurationExpression.getSingle(event).getAs(TimePeriod.TICK) == 4,
                Component.literal("Expected interpolation duration expression to resolve the current duration.")
        );
        interpolationDurationExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 3)}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayTransformationInterpolationDuration(textDisplay) == 3,
                Component.literal("Expected interpolation duration expression to apply SET changes.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> teleportDurationExpression = new SkriptParser(
                "teleport duration of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(teleportDurationExpression != null, Component.literal("Expected teleport duration expression to parse from registry."));
        if (teleportDurationExpression == null) {
            throw new IllegalStateException("teleport duration expression did not parse");
        }
        PrivateEntityAccess.setDisplayPosRotInterpolationDuration(textDisplay, 2);
        helper.assertTrue(
                teleportDurationExpression.getSingle(event).getAs(TimePeriod.TICK) == 2,
                Component.literal("Expected teleport duration expression to resolve the current teleport duration.")
        );
        teleportDurationExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 10)}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 12,
                Component.literal("Expected teleport duration expression to apply ADD changes.")
        );
        teleportDurationExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 99)}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 59,
                Component.literal("Expected teleport duration expression to clamp SET changes to the vanilla maximum.")
        );
        teleportDurationExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 0,
                Component.literal("Expected teleport duration expression to reset to zero.")
        );
        helper.succeed();
    }

    @GameTest
    public void displayTransformationExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(1.25F, 2.5F, 3.75F),
                        new Quaternionf(0.1F, 0.2F, 0.3F, 0.4F),
                        new Vector3f(2.0F, 3.0F, 4.0F),
                        new Quaternionf(0.5F, 0.6F, 0.7F, 0.8F)
                )
        );

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Vec3> translationExpression = new SkriptParser(
                "transformation translation of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Vec3.class});
        helper.assertTrue(translationExpression != null, Component.literal("Expected transformation translation expression to parse from registry."));
        if (translationExpression == null) {
            throw new IllegalStateException("transformation translation expression did not parse");
        }
        Vec3 translation = translationExpression.getSingle(event);
        helper.assertTrue(
                translation != null && Math.abs(translation.x - 1.25D) < 0.0001D && Math.abs(translation.y - 2.5D) < 0.0001D
                        && Math.abs(translation.z - 3.75D) < 0.0001D,
                Component.literal("Expected transformation translation expression to resolve the current translation.")
        );
        translationExpression.change(event, new Object[]{new Vec3(5.0D, 6.0D, 7.0D)}, ChangeMode.SET);
        Transformation translationChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(translationChange.translation().x() - 5.0F) < 0.0001F
                        && Math.abs(translationChange.translation().y() - 6.0F) < 0.0001F
                        && Math.abs(translationChange.translation().z() - 7.0F) < 0.0001F,
                Component.literal("Expected transformation translation expression to apply SET changes.")
        );
        translationExpression.change(event, null, ChangeMode.RESET);
        Transformation translationReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(translationReset.translation().x()) < 0.0001F
                        && Math.abs(translationReset.translation().y()) < 0.0001F
                        && Math.abs(translationReset.translation().z()) < 0.0001F,
                Component.literal("Expected transformation translation expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Vec3> scaleExpression = new SkriptParser(
                "transformation scale of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Vec3.class});
        helper.assertTrue(scaleExpression != null, Component.literal("Expected transformation scale expression to parse from registry."));
        if (scaleExpression == null) {
            throw new IllegalStateException("transformation scale expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(2.0F, 3.0F, 4.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        Vec3 scale = scaleExpression.getSingle(event);
        helper.assertTrue(
                scale != null && Math.abs(scale.x - 2.0D) < 0.0001D && Math.abs(scale.y - 3.0D) < 0.0001D
                        && Math.abs(scale.z - 4.0D) < 0.0001D,
                Component.literal("Expected transformation scale expression to resolve the current scale.")
        );
        scaleExpression.change(event, new Object[]{new Vec3(8.0D, 9.0D, 10.0D)}, ChangeMode.SET);
        Transformation scaleChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(scaleChange.scale().x() - 8.0F) < 0.0001F
                        && Math.abs(scaleChange.scale().y() - 9.0F) < 0.0001F
                        && Math.abs(scaleChange.scale().z() - 10.0F) < 0.0001F,
                Component.literal("Expected transformation scale expression to apply SET changes.")
        );
        scaleExpression.change(event, null, ChangeMode.RESET);
        Transformation scaleReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(scaleReset.scale().x() - 1.0F) < 0.0001F
                        && Math.abs(scaleReset.scale().y() - 1.0F) < 0.0001F
                        && Math.abs(scaleReset.scale().z() - 1.0F) < 0.0001F,
                Component.literal("Expected transformation scale expression to reset to one.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Quaternionf> leftRotationExpression = new SkriptParser(
                "left rotation of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Quaternionf.class});
        helper.assertTrue(leftRotationExpression != null, Component.literal("Expected left rotation expression to parse from registry."));
        if (leftRotationExpression == null) {
            throw new IllegalStateException("left rotation expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.1F, 0.2F, 0.3F, 0.4F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        Quaternionf leftRotation = leftRotationExpression.getSingle(event);
        helper.assertTrue(
                leftRotation != null
                        && Math.abs(leftRotation.x - 0.1F) < 0.0001F
                        && Math.abs(leftRotation.y - 0.2F) < 0.0001F
                        && Math.abs(leftRotation.z - 0.3F) < 0.0001F
                        && Math.abs(leftRotation.w - 0.4F) < 0.0001F,
                Component.literal("Expected left rotation expression to resolve the current quaternion.")
        );
        leftRotationExpression.change(event, new Object[]{new Quaternionf(0.11F, 0.22F, 0.33F, 0.44F)}, ChangeMode.SET);
        Transformation leftRotationChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(leftRotationChange.leftRotation().x() - 0.11F) < 0.0001F
                        && Math.abs(leftRotationChange.leftRotation().w() - 0.44F) < 0.0001F,
                Component.literal("Expected left rotation expression to apply SET changes.")
        );
        leftRotationExpression.change(event, null, ChangeMode.RESET);
        Transformation leftRotationReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(leftRotationReset.leftRotation().x()) < 0.0001F
                        && Math.abs(leftRotationReset.leftRotation().y()) < 0.0001F
                        && Math.abs(leftRotationReset.leftRotation().z()) < 0.0001F
                        && Math.abs(leftRotationReset.leftRotation().w() - 1.0F) < 0.0001F,
                Component.literal("Expected left rotation expression to reset to identity.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Quaternionf> rightRotationExpression = new SkriptParser(
                "right rotation of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Quaternionf.class});
        helper.assertTrue(rightRotationExpression != null, Component.literal("Expected right rotation expression to parse from registry."));
        if (rightRotationExpression == null) {
            throw new IllegalStateException("right rotation expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.5F, 0.6F, 0.7F, 0.8F)
                )
        );
        Quaternionf rightRotation = rightRotationExpression.getSingle(event);
        helper.assertTrue(
                rightRotation != null
                        && Math.abs(rightRotation.x - 0.5F) < 0.0001F
                        && Math.abs(rightRotation.y - 0.6F) < 0.0001F
                        && Math.abs(rightRotation.z - 0.7F) < 0.0001F
                        && Math.abs(rightRotation.w - 0.8F) < 0.0001F,
                Component.literal("Expected right rotation expression to resolve the current quaternion.")
        );
        rightRotationExpression.change(event, new Object[]{new Quaternionf(0.9F, 1.0F, 1.1F, 1.2F)}, ChangeMode.SET);
        Transformation rightRotationChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(rightRotationChange.rightRotation().x() - 0.9F) < 0.0001F
                        && Math.abs(rightRotationChange.rightRotation().w() - 1.2F) < 0.0001F,
                Component.literal("Expected right rotation expression to apply SET changes.")
        );
        rightRotationExpression.change(event, null, ChangeMode.RESET);
        Transformation rightRotationReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(rightRotationReset.rightRotation().x()) < 0.0001F
                        && Math.abs(rightRotationReset.rightRotation().y()) < 0.0001F
                        && Math.abs(rightRotationReset.rightRotation().z()) < 0.0001F
                        && Math.abs(rightRotationReset.rightRotation().w() - 1.0F) < 0.0001F,
                Component.literal("Expected right rotation expression to reset to identity.")
        );
        helper.succeed();
    }

    @GameTest
    public void itemDisplayTransformExpressionParsesResolvesAndChanges(GameTestHelper helper) {
        Display.ItemDisplay itemDisplay = createItemDisplay(helper, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        itemDisplay,
                        new EntityHitResult(itemDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends ItemDisplayContext> transformExpression = new SkriptParser(
                "item transform of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{ItemDisplayContext.class});
        helper.assertTrue(transformExpression != null, Component.literal("Expected item transform expression to parse from registry."));
        if (transformExpression == null) {
            throw new IllegalStateException("item transform expression did not parse");
        }
        helper.assertTrue(
                transformExpression.getSingle(event) == ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                Component.literal("Expected item transform expression to resolve the current item display transform.")
        );
        transformExpression.change(event, new Object[]{ItemDisplayContext.GUI}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.itemDisplayTransform(itemDisplay) == ItemDisplayContext.GUI,
                Component.literal("Expected item transform expression to apply SET changes.")
        );
        transformExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.itemDisplayTransform(itemDisplay) == ItemDisplayContext.NONE,
                Component.literal("Expected item transform expression to reset to no transform.")
        );

        Condition transformCondition = Condition.parse("item transform of event-entity is \"first person left hand\"", null);
        helper.assertTrue(transformCondition != null, Component.literal("Expected item transform comparison condition to parse."));
        if (transformCondition == null) {
            throw new IllegalStateException("item transform comparison condition did not parse");
        }
        PrivateEntityAccess.setItemDisplayTransform(itemDisplay, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        helper.assertTrue(
                transformCondition.check(event),
                Component.literal("Expected item transform comparison condition to evaluate true.")
        );
        helper.succeed();
    }

    @GameTest
    public void potionExpressionsResolveFromRegistry(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));

        var useEntityEvent = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        cow,
                        new EntityHitResult(cow)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends SkriptPotionEffect> potionEffectsExpression = parseExpressionInEvent(
                "active potion effects of event-entity",
                new Class[]{SkriptPotionEffect.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionEffectsExpression != null,
                Component.literal("Expected active potion effects expression to parse from registry.")
        );
        if (potionEffectsExpression == null) {
            throw new IllegalStateException("active potion effects expression did not parse");
        }
        SkriptPotionEffect resolvedPotionEffects = potionEffectsExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedPotionEffects != null && resolvedPotionEffects.type().is(MobEffects.POISON),
                Component.literal("Expected active potion effects expression to resolve the current poison effect.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends SkriptPotionEffect> potionEffectExpression = parseExpressionInEvent(
                "poison effect of event-entity",
                new Class[]{SkriptPotionEffect.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionEffectExpression != null,
                Component.literal("Expected specific potion effect expression to parse from registry.")
        );
        if (potionEffectExpression == null) {
            throw new IllegalStateException("specific potion effect expression did not parse");
        }
        SkriptPotionEffect resolvedPotionEffect = potionEffectExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedPotionEffect != null && resolvedPotionEffect.type().is(MobEffects.POISON),
                Component.literal("Expected specific potion effect expression to resolve the requested poison effect. "
                        + "Fields: " + describeExpressionFields(potionEffectExpression, useEntityEvent)
                        + ", value: " + resolvedPotionEffect)
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> potionDurationExpression = parseExpressionInEvent(
                "duration of poison effect of event-entity",
                new Class[]{Timespan.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionDurationExpression != null,
                Component.literal("Expected potion duration expression to parse from registry.")
        );
        if (potionDurationExpression == null) {
            throw new IllegalStateException("potion duration expression did not parse");
        }
        Timespan resolvedDuration = potionDurationExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedDuration != null && resolvedDuration.getAs(TimePeriod.TICK) == 200,
                Component.literal("Expected potion duration expression to resolve the poison duration in ticks.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> potionAmplifierExpression = parseExpressionInEvent(
                "potion amplifier of poison effect of event-entity",
                new Class[]{Integer.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionAmplifierExpression != null,
                Component.literal("Expected potion amplifier expression to parse from registry.")
        );
        if (potionAmplifierExpression == null) {
            throw new IllegalStateException("potion amplifier expression did not parse");
        }
        Integer resolvedAmplifier = potionAmplifierExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedAmplifier != null && resolvedAmplifier == 2,
                Component.literal("Expected potion amplifier expression to resolve the effect level plus one.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends MobEffectCategory> potionCategoryExpression = new SkriptParser(
                "potion effect type category of poison",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{MobEffectCategory.class});
        helper.assertTrue(
                potionCategoryExpression != null,
                Component.literal("Expected potion effect type category expression to parse from registry.")
        );
        if (potionCategoryExpression == null) {
            throw new IllegalStateException("potion effect type category expression did not parse");
        }
        MobEffectCategory resolvedCategory = potionCategoryExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedCategory == MobEffectCategory.HARMFUL,
                Component.literal("Expected potion effect type category expression to resolve poison as harmful.")
        );

        helper.succeed();
    }

    @GameTest
    public void potionEventPayloadExpressionsResolveFromHandle(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect currentEffect =
                org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.fromInstance(
                        new MobEffectInstance(MobEffects.POISON, 200, 1),
                        cow
                );
        org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect previousEffect =
                org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.fromInstance(
                        new MobEffectInstance(MobEffects.POISON, 200, 0)
                );

        var changedEvent = new org.skriptlang.skript.lang.event.SkriptEvent(
                new org.skriptlang.skript.fabric.runtime.FabricPotionEffectHandle(
                        helper.getLevel(),
                        cow,
                        currentEffect,
                        previousEffect,
                        org.skriptlang.skript.fabric.runtime.FabricPotionEffectAction.CHANGED,
                        FabricPotionEffectCause.UNKNOWN
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                null
        );

        @SuppressWarnings("unchecked")
        Expression<? extends org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect> eventPotionEffectExpression = parseExpressionInEvent(
                "event-potion effect",
                new Class[]{org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.class},
                org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle.class
        );
        helper.assertTrue(
                eventPotionEffectExpression != null,
                Component.literal("Expected event-potion effect expression to parse in potion events.")
        );
        if (eventPotionEffectExpression == null) {
            throw new IllegalStateException("event-potion effect expression did not parse");
        }
        org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect resolvedCurrentEffect = eventPotionEffectExpression.getSingle(changedEvent);
        helper.assertTrue(
                resolvedCurrentEffect != null && resolvedCurrentEffect.amplifier() == 1,
                Component.literal("Expected event-potion effect to resolve the current changed effect. Fields: "
                        + describeExpressionFields(eventPotionEffectExpression, changedEvent))
        );

        @SuppressWarnings("unchecked")
        Expression<? extends org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect> pastEventPotionEffectExpression = parseExpressionInEvent(
                "past event-potion effect",
                new Class[]{org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.class},
                org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle.class
        );
        helper.assertTrue(
                pastEventPotionEffectExpression != null,
                Component.literal("Expected past event-potion effect expression to parse in potion events.")
        );
        if (pastEventPotionEffectExpression == null) {
            throw new IllegalStateException("past event-potion effect expression did not parse");
        }
        org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect resolvedPastEffect = pastEventPotionEffectExpression.getSingle(changedEvent);
        helper.assertTrue(
                resolvedPastEffect != null && resolvedPastEffect.amplifier() == 0,
                Component.literal("Expected past event-potion effect to resolve the previous changed effect. Fields: "
                        + describeExpressionFields(pastEventPotionEffectExpression, changedEvent))
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> currentAmplifierExpression = parseExpressionInEvent(
                "potion amplifier of event-potion effect",
                new Class[]{Integer.class},
                org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle.class
        );
        helper.assertTrue(
                currentAmplifierExpression != null,
                Component.literal("Expected current event-potion amplifier expression to parse in potion events.")
        );
        if (currentAmplifierExpression == null) {
            throw new IllegalStateException("current event-potion amplifier expression did not parse");
        }
        Integer currentAmplifier = currentAmplifierExpression.getSingle(changedEvent);
        helper.assertTrue(
                Integer.valueOf(2).equals(currentAmplifier),
                Component.literal("Expected potion amplifier of event-potion effect to resolve the changed amplifier tier. Fields: "
                        + describeExpressionFields(currentAmplifierExpression, changedEvent)
                        + ", time=" + currentAmplifierExpression.getTime()
                        + ", value=" + currentAmplifier)
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> pastAmplifierExpression = parseExpressionInEvent(
                "potion amplifier of past event-potion effect",
                new Class[]{Integer.class},
                org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle.class
        );
        helper.assertTrue(
                pastAmplifierExpression != null,
                Component.literal("Expected past event-potion amplifier expression to parse in potion events.")
        );
        if (pastAmplifierExpression == null) {
            throw new IllegalStateException("past event-potion amplifier expression did not parse");
        }
        Integer pastAmplifier = pastAmplifierExpression.getSingle(changedEvent);
        helper.assertTrue(
                Integer.valueOf(1).equals(pastAmplifier),
                Component.literal("Expected potion amplifier of past event-potion effect to resolve the previous amplifier tier. Fields: "
                        + describeExpressionFields(pastAmplifierExpression, changedEvent)
                        + ", time=" + pastAmplifierExpression.getTime()
                        + ", value=" + pastAmplifier)
        );

        @SuppressWarnings("unchecked")
        Expression<? extends String> actionExpression = parseExpressionInEvent(
                "event-potion effect action",
                new Class[]{String.class},
                org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle.class
        );
        helper.assertTrue(
                actionExpression != null,
                Component.literal("Expected event-potion effect action expression to parse in potion events.")
        );
        if (actionExpression == null) {
            throw new IllegalStateException("event-potion effect action expression did not parse");
        }
        String action = actionExpression.getSingle(changedEvent);
        helper.assertTrue(
                "changed".equals(action),
                Component.literal("Expected event-potion effect action to resolve the changed action.")
        );

        helper.succeed();
    }

    @GameTest
    public void lootTableExpressionsResolveFromRegistry(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        @SuppressWarnings("unchecked")
        Expression<? extends LootTable> lootTableFromStringExpression = new SkriptParser(
                "loot table \"chests/simple_dungeon\"",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{LootTable.class});
        helper.assertTrue(
                lootTableFromStringExpression != null,
                Component.literal("Expected loot table from string expression to parse from registry.")
        );
        if (lootTableFromStringExpression == null) {
            throw new IllegalStateException("loot table from string expression did not parse");
        }
        LootTable parsed = lootTableFromStringExpression.getSingle(
                new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)
        );
        helper.assertTrue(
                parsed != null && BuiltInLootTables.SIMPLE_DUNGEON.equals(parsed.key()),
                Component.literal("Expected loot table from string expression to resolve the simple dungeon loot table.")
        );

        MinecartChest chestMinecart = createChestMinecart(helper, true);
        @SuppressWarnings("unchecked")
        Expression<? extends LootTable> eventLootTableExpression = new SkriptParser(
                "loot table of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{LootTable.class});
        helper.assertTrue(
                eventLootTableExpression != null,
                Component.literal("Expected loot table property expression to parse from registry.")
        );
        if (eventLootTableExpression == null) {
            throw new IllegalStateException("loot table property expression did not parse");
        }
        LootTable resolvedLootTable = eventLootTableExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        chestMinecart,
                        new EntityHitResult(chestMinecart)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedLootTable != null && BuiltInLootTables.SIMPLE_DUNGEON.equals(resolvedLootTable.key()),
                Component.literal("Expected loot table property expression to resolve the chest minecart loot table.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Long> lootTableSeedExpression = new SkriptParser(
                "loot table seed of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Long.class});
        helper.assertTrue(
                lootTableSeedExpression != null,
                Component.literal("Expected loot table seed expression to parse from registry.")
        );
        if (lootTableSeedExpression == null) {
            throw new IllegalStateException("loot table seed expression did not parse");
        }
        Long resolvedSeed = lootTableSeedExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        chestMinecart,
                        new EntityHitResult(chestMinecart)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedSeed != null && resolvedSeed == 1L,
                Component.literal("Expected loot table seed expression to resolve the chest minecart loot table seed.")
        );

        helper.succeed();
    }

    @GameTest
    public void healthSetExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/health_set_names_entity.sk",
                cow,
                "health set",
                () -> helper.assertTrue(
                        cow.getHealth() <= 10.0F,
                        Component.literal("Expected health expression to set entity health to 5 hearts (10 HP).")
                )
        );
    }

    @GameTest
    public void foodLevelExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/food_level_names_player.sk",
                cow,
                "hungry"
        );
    }

    @GameTest
    public void velocitySetExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/expression/velocity_set_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.MAGENTA_WOOL,
                () -> helper.assertTrue(
                        cow.getDeltaMovement().y > 0,
                        Component.literal("Expected velocity expression to set upward velocity on entity.")
                )
        );
    }

    @GameTest
    public void fireTicksExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/expression/fire_ticks_names_entity.sk",
                cow,
                "on fire",
                () -> helper.assertTrue(
                        cow.getRemainingFireTicks() > 0,
                        Component.literal("Expected fire time expression to set fire ticks on entity.")
                )
        );
    }

    @GameTest
    public void customNameGetExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/expression/custom_name_get_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.LIME_WOOL,
                () -> helper.assertTrue(
                        cow.getCustomName() != null && "test name".equals(cow.getCustomName().getString()),
                        Component.literal("Expected name expression to set custom name on entity.")
                )
        );
    }
}
