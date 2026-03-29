package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.bukkit.damagesource.elements.CondScalesWithDifficulty;
import org.skriptlang.skript.bukkit.damagesource.elements.CondWasIndirect;
import org.skriptlang.skript.bukkit.displays.text.CondTextDisplayHasDropShadow;
import org.skriptlang.skript.bukkit.fishing.elements.CondIsInOpenWater;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.bukkit.input.elements.conditions.CondIsPressingKey;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDamage;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDispensable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompInteract;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompShearable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompSwapEquipment;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.bukkit.loottables.elements.conditions.CondHasLootTable;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanAge;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanBreed;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsAdult;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsBaby;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsInLove;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondHasPotion;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPoisoned;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionAmbient;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionInstant;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasIcon;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasParticles;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.tags.elements.CondIsTagged;
import org.skriptlang.skript.fabric.compat.FabricBreedingState;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.PrivateBlockEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateFishingHookAccess;
import org.skriptlang.skript.fabric.runtime.FabricBucketCatchEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricBucketCatchHandle;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingHandle;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseItemHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import ch.njol.util.Kleenean;

import java.util.List;

public final class SkriptFabricConditionGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void adultEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/condition/adult_entity_marks_block.sk",
                cow,
                new BlockPos(7, 1, 0),
                Blocks.LIME_WOOL
        );
    }

    @GameTest
    public void babyEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, true);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/condition/baby_entity_marks_block.sk",
                cow,
                new BlockPos(8, 1, 0),
                Blocks.PINK_WOOL
        );
    }

    @GameTest
    public void canBreedConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/condition/breedable_entity_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.ORANGE_WOOL
        );
    }

    @GameTest
    public void canAgeConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/condition/can_age_entity_marks_block.sk",
                cow,
                new BlockPos(10, 1, 0),
                Blocks.WHITE_WOOL
        );
    }

    @GameTest
    public void inLoveConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/in_love_entity_marks_block.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(11, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            Cow cow = createCow(helper, false);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);
            cow.setInLove(player);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected in-love condition bridge test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.RED_CONCRETE),
                    Component.literal("Expected in-love condition to execute a real .sk file for an in-love cow.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingConsumeConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/brewing_consume_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(0, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing condition test.")
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
                    Component.literal("Expected brewing consume script to execute exactly one trigger.")
            );

            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 2, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingLureConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/fishing_lure_names_hook.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 3);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, true),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected fishing lure script to execute exactly one trigger.")
            );

            helper.assertTrue(
                    hook.getCustomName() != null && "lured hook".equals(hook.getCustomName().getString()),
                    Component.literal("Expected fishing lure condition script to name the created hook.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingOpenWaterConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/fishing_open_water_names_hook.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
            setFishingHookOpenWater(hook, true);

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected fishing open water script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    hook.getCustomName() != null && "open water hook".equals(hook.getCustomName().getString()),
                    Component.literal("Expected open water condition script to name the hook.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playerInputConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/player_input_forward_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
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
                    Component.literal("Expected player input script to execute exactly one trigger.")
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.LIME_WOOL),
                    Component.literal("Expected player input condition script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playerInputPastConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/player_input_past_forward_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(7, 1, 0));
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
                    Component.literal("Expected past player input script to execute exactly one trigger.")
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.ORANGE_WOOL),
                    Component.literal("Expected past player input condition script to observe the previous input state.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void namedEntityConditionBlocksEffectsWhenFalse(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/named_entity_blocks_effects.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    armorStand,
                    new EntityHitResult(armorStand)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected named condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.AIR),
                    Component.literal("Expected false named condition to stop later effects in the trigger.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void scaledDamageSourceConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/scaled_damage_source_names_entity.sk");

            Cow victim = createCow(helper, false);
            Cow attacker = createCow(helper, false);

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().mobAttack(attacker),
                    2.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage bridge to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "scaled target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected scaled damage source condition to rename the damaged entity from a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void indirectDamageSourceConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/indirect_damage_source_names_entity.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            ArmorStand projectile = new ArmorStand(helper.getLevel(), 1.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(projectile);

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().thrown(projectile, attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage bridge to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "indirect target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected indirect damage source condition to rename the damaged entity from a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void directDamageSourceDoesNotExecuteIndirectScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/indirect_damage_source_names_entity.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().playerAttack(attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage bridge to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() == null,
                    Component.literal("Expected direct damage source to fail the indirect condition and stop later effects.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void textDisplayDropShadowConditionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_SHADOW);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/text_display_shadow_names_entity.sk",
                textDisplay,
                "shadowed text"
        );
    }

    @GameTest
    public void textDisplaySeeThroughConditionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_SEE_THROUGH);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/text_display_see_through_names_entity.sk",
                textDisplay,
                "see through text"
        );
    }

    @GameTest
    public void responsiveInteractionConditionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/responsive_interaction_names_entity.sk",
                interaction,
                "responsive interaction"
        );
    }

    @GameTest
    public void lootableEntityConditionExecutesRealScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/lootable_entity_names_entity.sk",
                chestMinecart,
                "lootable entity"
        );
    }

    @GameTest
    public void lootableBlockConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/lootable_block_marks_block.sk");

            BlockPos chestRelative = new BlockPos(16, 1, 0);
            BlockPos chestAbsolute = helper.absolutePos(chestRelative);
            helper.getLevel().setBlockAndUpdate(chestAbsolute, Blocks.CHEST.defaultBlockState());

            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestAbsolute);
            helper.assertTrue(
                    chest != null,
                    Component.literal("Expected chest block entity to exist for the lootable block condition test.")
            );
            if (chest == null) {
                throw new IllegalStateException("Chest block entity was not created.");
            }

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(16, 2, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(chestAbsolute.getX() + 0.5D, chestAbsolute.getY() + 1.0D, chestAbsolute.getZ() + 0.5D);

            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(chestAbsolute), Direction.UP, chestAbsolute, false);
            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    hitResult
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected lootable block condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected lootable block condition script to mark the block above the clicked container.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void lootTableConditionExecutesRealScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/loot_table_entity_names_entity.sk",
                chestMinecart,
                "loot table entity"
        );
    }

    @GameTest
    public void missingLootTableDoesNotExecuteLootTableScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, false);
        assertUseEntityScriptDoesNotNameEntity(
                helper,
                "skript/gametest/condition/loot_table_entity_names_entity.sk",
                chestMinecart
        );
    }

    @GameTest
    public void poisonedEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/poisoned_entity_names_entity.sk",
                cow,
                "poisoned entity"
        );
    }

    @GameTest
    public void hasPotionConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/entity_with_poison_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(cow.getX(), cow.getY() + 1.0D, cow.getZ());
            helper.assertTrue(
                    skriptPatternMatches("event-entity has \"poison\" active", "%livingentities% (has|have) %objects% [active]"),
                    Component.literal("Expected has-potion syntax pattern to regex-match the script line.")
            );

            var event = new org.skriptlang.skript.lang.event.SkriptEvent(
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
            Trigger trigger = getOnlyLoadedTrigger(runtime);
            helper.assertTrue(
                    getFirstTriggerItem(trigger) instanceof CondHasPotion,
                    Component.literal("Expected has-potion script to load CondHasPotion as the first trigger item. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    trigger.execute(event),
                    Component.literal("Expected loaded has-potion trigger to execute. Items: " + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "poison effect entity".equals(cow.getCustomName().getString()),
                    Component.literal("Expected direct trigger execution for has-potion script to rename the entity. Items: "
                            + describeTriggerItems(trigger) + ", fields: " + describeExpressionFields(getFirstTriggerItem(trigger), event))
            );

            cow.setCustomName(null);
            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "poison effect entity".equals(cow.getCustomName().getString()),
                    Component.literal("Expected condition script to name the interacted entity. Items: " + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void taggedItemConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/tagged_item_renames_item.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
            helper.assertTrue(
                    skriptPatternMatches("event-item is tagged with \"piglin_loved\"", "%objects% (is|are) tagged (as|with) %objects%"),
                    Component.literal("Expected tagged-item syntax pattern to regex-match the script line.")
            );

            var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricUseItemHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );
            Trigger trigger = getOnlyLoadedTrigger(runtime);
            helper.assertTrue(
                    getFirstTriggerItem(trigger) instanceof CondIsTagged,
                    Component.literal("Expected tagged-item script to load CondIsTagged as the first trigger item. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    trigger.execute(event),
                    Component.literal("Expected loaded tagged-item trigger to execute. Items: " + describeTriggerItems(trigger))
            );
            ItemStack directHeld = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    directHeld.getCustomName() != null && "tagged item".equals(directHeld.getCustomName().getString()),
                    Component.literal("Expected direct trigger execution for tagged-item script to rename the held item. Items: "
                            + describeTriggerItems(trigger) + ", fields: " + describeExpressionFields(getFirstTriggerItem(trigger), event))
            );

            player.getItemInHand(InteractionHand.MAIN_HAND).remove(DataComponents.CUSTOM_NAME);
            InteractionResult result = UseItemCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use item condition test to keep Fabric callback flow in PASS state.")
            );
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    held.getCustomName() != null && "tagged item".equals(held.getCustomName().getString()),
                    Component.literal("Expected condition script to rename the held item. Items: " + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void equippableDamageConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/condition/equippable_damage_renames_item.sk",
                createEquippableTestItem(true, false, false, false, false),
                "damage equippable"
        );
    }

    @GameTest
    public void equippableDispensableConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/condition/equippable_dispensable_renames_item.sk",
                createEquippableTestItem(false, true, false, false, false),
                "dispensable equippable"
        );
    }

    @GameTest
    public void equippableInteractConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/condition/equippable_interact_renames_item.sk",
                createEquippableTestItem(false, false, true, false, false),
                "interactable equippable"
        );
    }

    @GameTest
    public void equippableShearableConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/condition/equippable_shearable_renames_item.sk",
                createEquippableTestItem(false, false, false, true, false),
                "shearable equippable"
        );
    }

    @GameTest
    public void equippableSwappableConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/condition/equippable_swappable_renames_item.sk",
                createEquippableTestItem(false, false, false, false, true),
                "swappable equippable"
        );
    }

    @GameTest
    public void baseConditionsWorkOnMappedTypes(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));

        Condition eventItemNotEmpty = Condition.parse("event-item is not empty", null);
        helper.assertTrue(
                eventItemNotEmpty != null,
                Component.literal("Expected event-item emptiness condition to parse.")
        );
        if (eventItemNotEmpty == null) {
            throw new IllegalStateException("event-item is not empty did not parse");
        }
        helper.assertTrue(
                eventItemNotEmpty.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricUseItemHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected event-item emptiness condition to evaluate true for a held apple.")
        );

        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        Condition futureBucketItemNotEmpty;
        try {
            parser.setCurrentEvent("bucket catch", FabricBucketCatchEventHandle.class);
            futureBucketItemNotEmpty = Condition.parse("future event-item is not empty", null);
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
        helper.assertTrue(
                futureBucketItemNotEmpty != null,
                Component.literal("Expected future event-item emptiness condition to parse inside bucket catch events.")
        );
        if (futureBucketItemNotEmpty == null) {
            throw new IllegalStateException("future event-item is not empty did not parse");
        }
        helper.assertTrue(
                futureBucketItemNotEmpty.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricBucketCatchHandle(
                                helper.getLevel(),
                                player,
                                new ArmorStand(helper.getLevel(), 1.0D, 1.0D, 1.0D),
                                new ItemStack(Items.WATER_BUCKET),
                                new ItemStack(Items.PUFFERFISH_BUCKET)
                        ),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected future event-item emptiness condition to evaluate true for bucket catch future items.")
        );

        Condition unnamedEntityCondition = Condition.parse("event-entity is not named", null);
        helper.assertTrue(
                unnamedEntityCondition != null,
                Component.literal("Expected event-entity named condition to parse.")
        );
        if (unnamedEntityCondition == null) {
            throw new IllegalStateException("event-entity is not named did not parse");
        }

        ArmorStand unnamedStand = new ArmorStand(helper.getLevel(), 1.0D, 1.0D, 1.0D);
        helper.assertTrue(
                unnamedEntityCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricUseEntityHandle(
                                helper.getLevel(),
                                player,
                                InteractionHand.MAIN_HAND,
                                unnamedStand,
                                new EntityHitResult(unnamedStand)
                        ),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected unnamed entity condition to evaluate true for an unnamed armor stand.")
        );

        var emptySlotCondition = new org.skriptlang.skript.bukkit.base.conditions.CondIsEmpty();
        helper.assertTrue(
                emptySlotCondition.init(
                        new Expression[]{new SimpleLiteral<>(new Slot(new SimpleContainer(1), 0, 0, 0), false)},
                        2,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected slot emptiness condition to initialize with a literal slot.")
        );
        helper.assertTrue(
                emptySlotCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), null)),
                Component.literal("Expected empty slot condition to evaluate true for an empty slot.")
        );

        var emptyInventoryCondition = new org.skriptlang.skript.bukkit.base.conditions.CondIsEmpty();
        helper.assertTrue(
                emptyInventoryCondition.init(
                        new Expression[]{new SimpleLiteral<>(new FabricInventory(new SimpleContainer(2)), false)},
                        4,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected inventory emptiness condition to initialize with a literal inventory.")
        );
        helper.assertTrue(
                emptyInventoryCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), null)),
                Component.literal("Expected empty inventory condition to evaluate true for an empty container.")
        );

        var namedItemCondition = new org.skriptlang.skript.bukkit.base.conditions.CondIsNamed();
        ItemStack namedStack = new ItemStack(Items.STICK);
        namedStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("named stick"));
        helper.assertTrue(
                namedItemCondition.init(
                        new Expression[]{new SimpleLiteral<>(namedStack, false)},
                        2,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected item named condition to initialize with a literal item stack.")
        );
        helper.assertTrue(
                namedItemCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), null)),
                Component.literal("Expected named item condition to evaluate true for a custom-named item.")
        );

        Cow adultCow = createCow(helper, false);
        Cow babyCow = createCow(helper, true);
        Cow inLoveCow = createCow(helper, false);
        inLoveCow.setInLove(player);
        FabricBreedingState.setAgeLocked(adultCow, true);

        var adultCondition = new CondIsAdult();
        helper.assertTrue(
                adultCondition.init(
                        new Expression[]{new SimpleLiteral<>(adultCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected adult condition to initialize with a cow literal.")
        );
        helper.assertTrue(
                adultCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected adult condition to evaluate true for an adult cow.")
        );

        var babyCondition = new CondIsBaby();
        helper.assertTrue(
                babyCondition.init(
                        new Expression[]{new SimpleLiteral<>(babyCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected baby condition to initialize with a baby cow literal.")
        );
        helper.assertTrue(
                babyCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected baby condition to evaluate true for a baby cow.")
        );

        var canBreedCondition = new CondCanBreed();
        helper.assertTrue(
                canBreedCondition.init(
                        new Expression[]{new SimpleLiteral<>(adultCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected can-breed condition to initialize with an adult cow literal.")
        );
        helper.assertTrue(
                canBreedCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected can-breed condition to evaluate true for a breed-ready cow.")
        );

        var canAgeCondition = new CondCanAge();
        helper.assertTrue(
                canAgeCondition.init(
                        new Expression[]{new SimpleLiteral<>(adultCow, false)},
                        1,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected can-age condition to initialize with an adult cow literal.")
        );
        helper.assertTrue(
                canAgeCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected negated can-age condition to evaluate true after age lock emulation.")
        );

        var inLoveCondition = new CondIsInLove();
        helper.assertTrue(
                inLoveCondition.init(
                        new Expression[]{new SimpleLiteral<>(inLoveCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected in-love condition to initialize with a cow literal.")
        );
        helper.assertTrue(
                inLoveCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected in-love condition to evaluate true for a cow in love mode.")
        );

        DamageSource scaledDamageSource = helper.getLevel().damageSources().mobAttack(adultCow);
        var scalesWithDifficultyCondition = new CondScalesWithDifficulty();
        helper.assertTrue(
                scalesWithDifficultyCondition.init(
                        new Expression[]{new SimpleLiteral<>(scaledDamageSource, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected scales-with-difficulty condition to initialize with a damage source literal.")
        );
        helper.assertTrue(
                scalesWithDifficultyCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected mob damage source to scale with difficulty.")
        );

        ArmorStand projectile = new ArmorStand(helper.getLevel(), 4.0D, 1.0D, 4.0D);
        helper.getLevel().addFreshEntity(projectile);
        DamageSource indirectDamageSource = helper.getLevel().damageSources().thrown(projectile, player);
        var indirectCondition = new CondWasIndirect();
        helper.assertTrue(
                indirectCondition.init(
                        new Expression[]{new SimpleLiteral<>(indirectDamageSource, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected indirect-damage condition to initialize with a damage source literal.")
        );
        helper.assertTrue(
                indirectCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected thrown damage source to be treated as indirectly caused.")
        );

        Display.TextDisplay shadowedTextDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_SHADOW);
        var shadowCondition = new CondTextDisplayHasDropShadow();
        helper.assertTrue(
                shadowCondition.init(
                        new Expression[]{new SimpleLiteral<>(shadowedTextDisplay, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected text display shadow condition to initialize with a TextDisplay literal.")
        );
        helper.assertTrue(
                shadowCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected text display shadow condition to evaluate true for a flagged TextDisplay.")
        );

        Interaction responsiveInteraction = createInteraction(helper, true);
        var responsiveCondition = new CondIsResponsive();
        helper.assertTrue(
                responsiveCondition.init(
                        new Expression[]{new SimpleLiteral<>(responsiveInteraction, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected responsive interaction condition to initialize with an interaction literal.")
        );
        helper.assertTrue(
                responsiveCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected responsive interaction condition to evaluate true for a responsive interaction entity.")
        );

        MinecartChest chestMinecart = createChestMinecart(helper, true);
        var lootTableCondition = new CondHasLootTable();
        helper.assertTrue(
                lootTableCondition.init(
                        new Expression[]{new SimpleLiteral<>(chestMinecart, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected loot table condition to initialize with a lootable entity literal.")
        );
        helper.assertTrue(
                lootTableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected loot table condition to evaluate true for a chest minecart with a loot table.")
        );

        Cow poisonedCow = createCow(helper, false);
        poisonedCow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        var poisonedCondition = new CondIsPoisoned();
        helper.assertTrue(
                poisonedCondition.init(
                        new Expression[]{new SimpleLiteral<>(poisonedCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected poisoned condition to initialize with a living entity literal.")
        );
        helper.assertTrue(
                poisonedCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected poisoned condition to evaluate true for a poisoned cow.")
        );

        var hasPotionCondition = new CondHasPotion();
        helper.assertTrue(
                hasPotionCondition.init(
                        new Expression[]{
                                new SimpleLiteral<>(poisonedCow, false),
                                new SimpleLiteral<>("poison", false)
                        },
                        2,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected has-potion condition to initialize with string effect literals.")
        );
        helper.assertTrue(
                hasPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected has-potion condition to match a poisoned cow.")
        );

        SkriptPotionEffect ambientPotion = SkriptPotionEffect.fromInstance(new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
        var ambientPotionCondition = new CondIsPotionAmbient();
        helper.assertTrue(
                ambientPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>(ambientPotion, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected potion ambient condition to initialize with a potion wrapper.")
        );
        helper.assertTrue(
                ambientPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected potion ambient condition to evaluate true for an ambient effect.")
        );

        var instantPotionCondition = new CondIsPotionInstant();
        helper.assertTrue(
                instantPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>("instant_health", false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected instant potion condition to initialize with a string literal.")
        );
        helper.assertTrue(
                instantPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected instant potion condition to evaluate true for instant health.")
        );

        var iconPotionCondition = new CondPotionHasIcon();
        helper.assertTrue(
                iconPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>(SkriptPotionEffect.fromInstance(new MobEffectInstance(MobEffects.SPEED, 200, 0, false, true, true)), false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected potion icon condition to initialize with a potion wrapper.")
        );
        helper.assertTrue(
                iconPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected potion icon condition to evaluate true for an icon-visible effect.")
        );

        var particlesPotionCondition = new CondPotionHasParticles();
        helper.assertTrue(
                particlesPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>(SkriptPotionEffect.fromInstance(new MobEffectInstance(MobEffects.SPEED, 200, 0, false, true, false)), false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected potion particles condition to initialize with a potion wrapper.")
        );
        helper.assertTrue(
                particlesPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected potion particles condition to evaluate true for a visible effect.")
        );

        var taggedCondition = new CondIsTagged();
        helper.assertTrue(
                taggedCondition.init(
                        new Expression[]{
                                new SimpleLiteral<>(new ItemStack(Items.GOLD_INGOT), false),
                                new SimpleLiteral<>("piglin_loved", false)
                        },
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected tagged condition to initialize with item and tag literals.")
        );
        helper.assertTrue(
                taggedCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected tagged condition to evaluate true for gold ingots in piglin_loved.")
        );

        ItemStack equippableStack = createEquippableTestItem(true, true, true, true, true);

        var damageEquippableCondition = new CondEquipCompDamage();
        helper.assertTrue(
                damageEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable damage condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                damageEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable damage condition to evaluate true for a damage-on-hurt component.")
        );

        var dispensableEquippableCondition = new CondEquipCompDispensable();
        helper.assertTrue(
                dispensableEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable dispensable condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                dispensableEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable dispensable condition to evaluate true for a dispensable component.")
        );

        var interactEquippableCondition = new CondEquipCompInteract();
        helper.assertTrue(
                interactEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable interact condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                interactEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable interact condition to evaluate true for an interactable component.")
        );

        var shearableEquippableCondition = new CondEquipCompShearable();
        helper.assertTrue(
                shearableEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable shearable condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                shearableEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable shearable condition to evaluate true for a shearable component.")
        );

        var swappableEquippableCondition = new CondEquipCompSwapEquipment();
        helper.assertTrue(
                swappableEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable swappable condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                swappableEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable swappable condition to evaluate true for a swappable component.")
        );

        FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
        setFishingHookOpenWater(hook, true);

        var openWaterCondition = new CondIsInOpenWater();
        helper.assertTrue(
                openWaterCondition.init(
                        new Expression[]{new SimpleLiteral<>(hook, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected open water condition to initialize with a fishing hook literal.")
        );
        helper.assertTrue(
                openWaterCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected open water condition to evaluate true for a fishing hook in open water.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Slot> brewingSlotExpression = new SkriptParser(
                "brewing stand fuel slot of event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Slot.class});
        helper.assertTrue(
                brewingSlotExpression != null,
                Component.literal("Expected brewing stand fuel slot expression to parse from registry.")
        );
        if (brewingSlotExpression == null) {
            throw new IllegalStateException("brewing stand fuel slot expression did not parse");
        }

        BlockPos brewingAbsolute = helper.absolutePos(new BlockPos(12, 1, 0));
        helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());
        BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
        helper.assertTrue(
                brewingStand != null,
                Component.literal("Expected brewing stand block entity for expression resolution test.")
        );
        if (brewingStand == null) {
            throw new IllegalStateException("Brewing stand block entity missing for expression resolution test.");
        }
        brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        Slot brewingSlot = brewingSlotExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBrewingFuelHandle(helper.getLevel(), brewingAbsolute, brewingStand, true),
                helper.getLevel().getServer(),
                helper.getLevel(),
                null
        ));
        helper.assertTrue(
                brewingSlot != null && brewingSlot.getItem().is(Items.BLAZE_POWDER),
                Component.literal("Expected brewing stand fuel slot expression to resolve the fuel slot item.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> brewingFuelLevelExpression = new SkriptParser(
                "brewing fuel amount of event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(
                brewingFuelLevelExpression != null,
                Component.literal("Expected brewing fuel amount expression to parse from registry.")
        );
        if (brewingFuelLevelExpression == null) {
            throw new IllegalStateException("brewing fuel amount expression did not parse");
        }
        PrivateBlockEntityAccess.setBrewingFuel(brewingStand, 20);
        Integer resolvedFuelLevel = brewingFuelLevelExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new org.skriptlang.skript.fabric.runtime.FabricUseBlockHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedFuelLevel != null && resolvedFuelLevel == 20,
                Component.literal("Expected brewing fuel amount expression to resolve the brewing stand fuel level.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> brewingTimeExpression = new SkriptParser(
                "brewing time of event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(
                brewingTimeExpression != null,
                Component.literal("Expected brewing time expression to parse from registry.")
        );
        if (brewingTimeExpression == null) {
            throw new IllegalStateException("brewing time expression did not parse");
        }
        PrivateBlockEntityAccess.setBrewingTime(brewingStand, 6);
        Timespan resolvedBrewingTime = brewingTimeExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new org.skriptlang.skript.fabric.runtime.FabricUseBlockHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedBrewingTime != null && resolvedBrewingTime.getAs(TimePeriod.TICK) == 6,
                Component.literal("Expected brewing time expression to resolve the brewing time in ticks.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> minimumFishingWaitTimeExpression = parseExpressionInEvent(
                "minimum fishing wait time",
                new Class[]{Timespan.class},
                FabricFishingEventHandle.class
        );
        helper.assertTrue(
                minimumFishingWaitTimeExpression != null,
                Component.literal("Expected minimum fishing wait time expression to parse from registry.")
        );
        if (minimumFishingWaitTimeExpression == null) {
            throw new IllegalStateException("minimum fishing wait time expression did not parse");
        }

        FishingHook fishingExpressionHook = new FishingHook(player, helper.getLevel(), 0, 0);
        FabricFishingState.minWaitTime(fishingExpressionHook, 12);
        FabricFishingState.maxWaitTime(fishingExpressionHook, 30);
        PrivateFishingHookAccess.setTimeUntilHooked(fishingExpressionHook, 9);
        FabricFishingState.minLureAngle(fishingExpressionHook, 15.0F);
        FabricFishingState.maxLureAngle(fishingExpressionHook, 270.0F);

        Timespan resolvedMinimumFishingWaitTime = minimumFishingWaitTimeExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(helper.getLevel(), player, fishingExpressionHook, false),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedMinimumFishingWaitTime != null && resolvedMinimumFishingWaitTime.getAs(TimePeriod.TICK) == 12,
                Component.literal("Expected minimum fishing wait time expression to resolve the minimum wait time.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> fishingBiteTimeExpression = parseExpressionInEvent(
                "fishing bite time",
                new Class[]{Timespan.class},
                FabricFishingEventHandle.class
        );
        helper.assertTrue(
                fishingBiteTimeExpression != null,
                Component.literal("Expected fishing bite time expression to parse from registry.")
        );
        if (fishingBiteTimeExpression == null) {
            throw new IllegalStateException("fishing bite time expression did not parse");
        }
        Timespan resolvedFishingBiteTime = fishingBiteTimeExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(helper.getLevel(), player, fishingExpressionHook, false),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedFishingBiteTime != null && resolvedFishingBiteTime.getAs(TimePeriod.TICK) == 9,
                Component.literal("Expected fishing bite time expression to resolve the bite time.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> maximumFishingApproachAngleExpression = parseExpressionInEvent(
                "maximum fishing approach angle",
                new Class[]{Float.class},
                FabricFishingEventHandle.class
        );
        helper.assertTrue(
                maximumFishingApproachAngleExpression != null,
                Component.literal("Expected maximum fishing approach angle expression to parse from registry.")
        );
        if (maximumFishingApproachAngleExpression == null) {
            throw new IllegalStateException("maximum fishing approach angle expression did not parse");
        }
        Float resolvedMaximumFishingApproachAngle = maximumFishingApproachAngleExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(helper.getLevel(), player, fishingExpressionHook, false),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedMaximumFishingApproachAngle != null && Float.compare(resolvedMaximumFishingApproachAngle, 270.0F) == 0,
                Component.literal("Expected maximum fishing approach angle expression to resolve the lure angle.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends InputKey> currentInputKeysExpression = new SkriptParser(
                "current input keys of event-player",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{InputKey.class});
        helper.assertTrue(
                currentInputKeysExpression != null,
                Component.literal("Expected current input keys expression to parse from registry.")
        );
        if (currentInputKeysExpression == null) {
            throw new IllegalStateException("current input keys expression did not parse");
        }
        InputKey[] resolvedInputKeys = currentInputKeysExpression.getAll(new org.skriptlang.skript.lang.event.SkriptEvent(
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
                List.of(resolvedInputKeys).contains(InputKey.FORWARD),
                Component.literal("Expected current input keys expression to resolve the forward input key.")
        );

        var pressingKeyCondition = new CondIsPressingKey();
        helper.assertTrue(
                pressingKeyCondition.init(
                        new Expression[]{
                                new SimpleLiteral<>(player, false),
                                new SimpleLiteral<>(InputKey.FORWARD, false)
                        },
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected pressing key condition to initialize with player and input key literals.")
        );
        helper.assertTrue(
                pressingKeyCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricPlayerInputHandle(
                                helper.getLevel(),
                                player,
                                Input.EMPTY,
                                new Input(true, false, false, false, false, false, false)
                        ),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected pressing key condition to evaluate true for a forward input event.")
        );

        helper.succeed();
    }

    @GameTest
    public void wetEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        BlockPos cowAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
        helper.getLevel().setBlockAndUpdate(cowAbsolute, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
        cow.setPos(cowAbsolute.getX() + 0.5D, cowAbsolute.getY(), cowAbsolute.getZ() + 0.5D);
        cow.tick();
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/wet_entity_names_entity.sk",
                cow,
                "wet"
        );
    }

    @GameTest
    public void canPickUpItemsConditionExecutesRealScript(GameTestHelper helper) {
        net.minecraft.world.entity.monster.zombie.Zombie zombie =
                (net.minecraft.world.entity.monster.zombie.Zombie) helper.spawnWithNoFreeWill(
                        EntityType.ZOMBIE, 0.5F, 1.0F, 0.5F);
        zombie.setCanPickUpLoot(true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/can_pick_up_items_names_entity.sk",
                zombie,
                "can pick up"
        );
    }

    @GameTest
    public void glowingEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/glowing_entity_names_entity.sk",
                cow,
                "glowing",
                () -> helper.assertTrue(
                        cow.hasGlowingTag(),
                        Component.literal("Expected glowing script to make entity glow.")
                )
        );
    }

    @GameTest
    public void hasGravityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/has_gravity_entity_names_entity.sk",
                cow,
                "has gravity"
        );
    }

    @GameTest
    public void frozenEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/frozen_entity_names_entity.sk",
                cow,
                "frozen",
                () -> helper.assertTrue(
                        cow.getTicksFrozen() >= 200,
                        Component.literal("Expected frozen condition script to set freeze ticks on entity.")
                )
        );
    }
}
