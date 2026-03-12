package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.CondContains;
import ch.njol.skript.conditions.CondItemInHand;
import ch.njol.skript.conditions.CondIsWearing;
import ch.njol.skript.conditions.CondPermission;
import ch.njol.skript.expressions.ExprAI;
import ch.njol.skript.expressions.ExprAttackCooldown;
import ch.njol.skript.expressions.ExprChestInventory;
import ch.njol.skript.expressions.ExprEnderChest;
import ch.njol.skript.expressions.ExprExhaustion;
import ch.njol.skript.expressions.ExprFallDistance;
import ch.njol.skript.expressions.ExprFireTicks;
import ch.njol.skript.expressions.ExprFirstEmptySlot;
import ch.njol.skript.expressions.ExprFlightMode;
import ch.njol.skript.expressions.ExprFreezeTicks;
import ch.njol.skript.expressions.ExprGravity;
import ch.njol.skript.expressions.ExprInventory;
import ch.njol.skript.expressions.ExprInventoryInfo;
import ch.njol.skript.expressions.ExprInventorySlot;
import ch.njol.skript.expressions.ExprItemsIn;
import ch.njol.skript.expressions.ExprLastDamage;
import ch.njol.skript.expressions.ExprLevelProgress;
import ch.njol.skript.expressions.ExprMaxFreezeTicks;
import ch.njol.skript.expressions.ExprRandomCharacter;
import ch.njol.skript.expressions.ExprTimes;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecIf;
import ch.njol.skript.structures.StructOptions;
import org.skriptlang.skript.lang.properties.PropertyRegistry;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.bukkit.base.types.BlockClassInfo;
import org.skriptlang.skript.bukkit.base.conditions.CondAI;
import org.skriptlang.skript.bukkit.base.conditions.CondIsEmpty;
import org.skriptlang.skript.bukkit.base.conditions.CondIsAlive;
import org.skriptlang.skript.bukkit.base.conditions.CondIsBurning;
import org.skriptlang.skript.bukkit.base.conditions.CondCompare;
import org.skriptlang.skript.bukkit.base.conditions.CondIsInvisible;
import org.skriptlang.skript.bukkit.base.conditions.CondIsInvulnerable;
import org.skriptlang.skript.bukkit.base.conditions.CondIsNamed;
import org.skriptlang.skript.bukkit.base.conditions.CondIsSilent;
import org.skriptlang.skript.bukkit.base.conditions.CondIsSprinting;
import org.skriptlang.skript.bukkit.base.types.DamageSourceClassInfo;
import org.skriptlang.skript.bukkit.brewing.elements.CondBrewingConsume;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingFuelLevel;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingSlot;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingTime;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanAge;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanBreed;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsAdult;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsBaby;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsInLove;
import org.skriptlang.skript.bukkit.breeding.elements.ExprLoveTime;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprCausingEntity;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprDamageLocation;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprDirectEntity;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprFoodExhaustion;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSourceLocation;
import org.skriptlang.skript.bukkit.damagesource.elements.CondScalesWithDifficulty;
import org.skriptlang.skript.bukkit.damagesource.elements.CondWasIndirect;
import org.skriptlang.skript.bukkit.displays.generic.DisplayBillboardConstraintsClassInfo;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayBillboard;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayBrightness;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayHeightWidth;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayInterpolation;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayShadow;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayTeleportDuration;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayTransformationRotation;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayTransformationScaleTranslation;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayViewRange;
import org.skriptlang.skript.bukkit.displays.item.ExprItemDisplayTransform;
import org.skriptlang.skript.bukkit.displays.item.ItemDisplayTransformClassInfo;
import org.skriptlang.skript.bukkit.displays.text.CondTextDisplayHasDropShadow;
import org.skriptlang.skript.bukkit.displays.text.CondTextDisplaySeeThroughBlocks;
import org.skriptlang.skript.bukkit.displays.text.ExprTextDisplayAlignment;
import org.skriptlang.skript.bukkit.displays.text.ExprTextDisplayLineWidth;
import org.skriptlang.skript.bukkit.displays.text.ExprTextDisplayOpacity;
import org.skriptlang.skript.bukkit.displays.text.TextDisplayAlignClassInfo;
import org.skriptlang.skript.bukkit.fishing.elements.ExprFishingHook;
import org.skriptlang.skript.bukkit.fishing.elements.ExprFishingHookEntity;
import org.skriptlang.skript.bukkit.fishing.elements.ExprFishingApproachAngle;
import org.skriptlang.skript.bukkit.fishing.elements.ExprFishingBiteTime;
import org.skriptlang.skript.bukkit.fishing.elements.ExprFishingWaitTime;
import org.skriptlang.skript.bukkit.fishing.elements.EffSetFishingApproachAngle;
import org.skriptlang.skript.bukkit.fishing.elements.CondFishingLure;
import org.skriptlang.skript.bukkit.fishing.elements.CondIsInOpenWater;
import org.skriptlang.skript.bukkit.input.InputKeyClassInfo;
import org.skriptlang.skript.bukkit.input.elements.expressions.ExprCurrentInputKeys;
import org.skriptlang.skript.bukkit.input.elements.conditions.CondIsPressingKey;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprInteractionDimensions;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprLastInteractionPlayer;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableComponentClassInfo;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDamage;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDispensable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompInteract;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompShearable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompSwapEquipment;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.loottables.LootTableClassInfo;
import org.skriptlang.skript.bukkit.loottables.elements.conditions.CondHasLootTable;
import org.skriptlang.skript.bukkit.loottables.elements.conditions.CondIsLootable;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootTable;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootTableFromString;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootTableSeed;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondHasPotion;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPoisoned;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionAmbient;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionInstant;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasIcon;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasParticles;
import org.skriptlang.skript.bukkit.potion.PotionCauseClassInfo;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionAmplifier;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionDuration;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffectTypeCategory;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffects;
import org.skriptlang.skript.bukkit.tags.elements.CondIsTagged;
import org.skriptlang.skript.bukkit.base.types.EntityClassInfo;
import org.skriptlang.skript.bukkit.base.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemStackClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemTypeClassInfo;
import org.skriptlang.skript.bukkit.base.types.LocationClassInfo;
import org.skriptlang.skript.bukkit.base.types.NameableClassInfo;
import org.skriptlang.skript.bukkit.base.types.OfflinePlayerClassInfo;
import org.skriptlang.skript.bukkit.base.types.PlayerClassInfo;
import org.skriptlang.skript.bukkit.base.types.QuaternionClassInfo;
import org.skriptlang.skript.bukkit.base.types.SlotClassInfo;
import org.skriptlang.skript.bukkit.base.types.TimespanClassInfo;
import org.skriptlang.skript.bukkit.base.types.VectorClassInfo;
import org.skriptlang.skript.bukkit.base.types.WorldClassInfo;
import org.skriptlang.skript.fabric.SkriptFabric;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.bukkit.base.effects.EffInvisible;
import org.skriptlang.skript.bukkit.base.effects.EffFeed;
import org.skriptlang.skript.bukkit.base.effects.EffInvulnerability;
import org.skriptlang.skript.bukkit.base.effects.EffKill;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestBlockAtLocation;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestBlockAtBlock;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestBlockAboveBlock;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestBlock;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestEntityName;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestItemName;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestBlockUnderPlayer;
import org.skriptlang.skript.bukkit.base.effects.EffSilence;
import org.skriptlang.skript.bukkit.base.effects.EffSprinting;
import org.skriptlang.skript.fabric.syntax.event.EvtAttackEntity;
import org.skriptlang.skript.fabric.syntax.event.EvtBrewingFuel;
import org.skriptlang.skript.fabric.syntax.event.EvtDamage;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricBlockBreak;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricGameTest;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricServerTick;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricUseBlock;
import org.skriptlang.skript.fabric.syntax.event.EvtFishing;
import org.skriptlang.skript.fabric.syntax.event.EvtPlayerInput;
import org.skriptlang.skript.fabric.syntax.event.EvtUseEntity;
import org.skriptlang.skript.fabric.syntax.event.EvtUseItem;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventBlock;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventDamageSource;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventEntity;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventItem;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventPlayer;
import org.skriptlang.skript.lang.properties.Property;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class SkriptFabricBootstrap {

    private static volatile boolean bootstrapped;

    private SkriptFabricBootstrap() {
    }

    public static void bootstrap() {
        if (bootstrapped && hasCoreBootstrapState()) {
            return;
        }
        synchronized (SkriptFabricBootstrap.class) {
            if (bootstrapped && hasCoreBootstrapState()) {
                return;
            }

            Skript.setAcceptRegistrations(true);
            try {
                StructOptions.register();
                SecIf.register();
                forceInitialize(ch.njol.skript.sections.SecLoop.class);
                if (!hasCoreProperties()) {
                    Property.registerDefaultProperties();
                }
                registerCoreClassInfos();
                SkriptFabricEventBridge.register();
                Skript.registerCondition(
                        CondIsEmpty.class,
                        "%itemstack% is empty",
                        "%itemstack% is not empty",
                        "%slot% is empty",
                        "%slot% is not empty",
                        "%inventory% is empty",
                        "%inventory% is not empty"
                );
                Skript.registerCondition(
                        CondIsNamed.class,
                        "%entity% is named",
                        "%entity% is not named",
                        "%itemstack% is named",
                        "%itemstack% is not named"
                );
                Skript.registerCondition(
                        CondIsAlive.class,
                        "%entities% (is|are) (alive|1¦dead)",
                        "%entities% (isn't|is not|aren't|are not) (alive|1¦dead)"
                );
                Skript.registerCondition(
                        CondIsBurning.class,
                        "%entities% (is|are) (burning|ignited|on fire)",
                        "%entities% (isn't|is not|aren't|are not) (burning|ignited|on fire)"
                );
                Skript.registerCondition(
                        CondIsSilent.class,
                        "%entities% (is|are) silent",
                        "%entities% (isn't|is not|aren't|are not) silent"
                );
                Skript.registerCondition(
                        CondIsInvisible.class,
                        "%livingentities% (is|are) (invisible|:visible)",
                        "%livingentities% (isn't|is not|aren't|are not) (invisible|:visible)"
                );
                Skript.registerCondition(
                        CondIsInvulnerable.class,
                        "%entities% (is|are) (invulnerable|invincible)",
                        "%entities% (isn't|is not|aren't|are not) (invulnerable|invincible)"
                );
                Skript.registerCondition(
                        CondAI.class,
                        "%livingentities% (has|have) (ai|artificial intelligence)",
                        "%livingentities% (doesn't|does not|do not|don't) have (ai|artificial intelligence)"
                );
                Skript.registerCondition(
                        CondIsSprinting.class,
                        "%players% (is|are) sprinting",
                        "%players% (isn't|is not|aren't|are not) sprinting"
                );
                Skript.registerCondition(
                        CondContains.class,
                        "%inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
                        "%inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
                        "%inventories/strings/objects% contain[(1¦s)] %itemtypes/strings/objects%",
                        "%inventories/strings/objects% (doesn't|does not|do not|don't) contain %itemtypes/strings/objects%"
                );
                Skript.registerCondition(
                        CondItemInHand.class,
                        "[%livingentities%] ha(s|ve) %itemtypes% in [main] hand",
                        "[%livingentities%] (is|are) holding %itemtypes% [in main hand]",
                        "[%livingentities%] ha(s|ve) %itemtypes% in off[(-| )]hand",
                        "[%livingentities%] (is|are) holding %itemtypes% in off[(-| )]hand",
                        "[%livingentities%] (ha(s|ve) not|do[es]n't have) %itemtypes% in [main] hand",
                        "[%livingentities%] (is not|isn't) holding %itemtypes% [in main hand]",
                        "[%livingentities%] (ha(s|ve) not|do[es]n't have) %itemtypes% in off[(-| )]hand",
                        "[%livingentities%] (is not|isn't) holding %itemtypes% in off[(-| )]hand"
                );
                Skript.registerCondition(
                        CondIsWearing.class,
                        "%livingentities% (is|are) wearing %itemtypes%",
                        "%livingentities% (isn't|is not|aren't|are not) wearing %itemtypes%"
                );
                CondPermission.register();
                initializeRecoveredConditionBundle();
                Skript.registerCondition(
                        CondBrewingConsume.class,
                        "[the] brewing stand will consume [the] fuel",
                        "[the] brewing stand (will not|won't) consume [the] fuel"
                );
                Skript.registerCondition(
                        CondCanAge.class,
                        "%entity% can (age|grow (up|old[er]))",
                        "%entity% can(n't| not) (age|grow (up|old[er]))"
                );
                Skript.registerCondition(
                        CondCanBreed.class,
                        "%entity% can (breed|be bred)",
                        "%entity% can(n't| not) (breed|be bred)"
                );
                Skript.registerCondition(
                        CondIsAdult.class,
                        "%entity% is [an] adult",
                        "%entity% is(n't| not) [an] adult"
                );
                Skript.registerCondition(
                        CondIsBaby.class,
                        "%entity% is a (child|baby)",
                        "%entity% is(n't| not) a (child|baby)"
                );
                Skript.registerCondition(
                        CondIsInLove.class,
                        "%entity% is in lov(e|ing) [state|mode]",
                        "%entity% is(n't| not) in lov(e|ing) [state|mode]"
                );
                Skript.registerCondition(
                        CondScalesWithDifficulty.class,
                        "%damagesources% ((does|do) scale|scales) damage with difficulty",
                        "%damagesources% (do not|don't|does not|doesn't) scale damage with difficulty",
                        "%damagesources%'[s] damage ((does|do) scale|scales) with difficulty",
                        "%damagesources%'[s] damage (do not|don't|does not|doesn't) scale with difficulty"
                );
                Skript.registerCondition(
                        CondWasIndirect.class,
                        "%damagesources% (was|were) (indirectly caused|caused indirectly)",
                        "%damagesources% (was not|wasn't|were not|weren't) (indirectly caused|caused indirectly)",
                        "%damagesources% (was|were) (directly caused|caused directly)",
                        "%damagesources% (was not|wasn't|were not|weren't) (directly caused|caused directly)"
                );
                Skript.registerCondition(
                        CondFishingLure.class,
                        "lure enchantment bonus is (applied|active)",
                        "lure enchantment bonus is(n't| not) (applied|active)"
                );
                Skript.registerCondition(
                        CondIsInOpenWater.class,
                        "%entity% (is|are) in open water[s]",
                        "%entity% (isn't|is not|aren't|are not) in open water[s]"
                );
                Skript.registerCondition(
                        CondIsPressingKey.class,
                        "%players% (is|are) pressing %inputkeys%",
                        "%players% (isn't|is not|aren't|are not) pressing %inputkeys%",
                        "%players% (was|were) pressing %inputkeys%",
                        "%players% (wasn't|was not|weren't|were not) pressing %inputkeys%"
                );
                Skript.registerCondition(
                        CondTextDisplayHasDropShadow.class,
                        "[[the] text of] %entities% (has|have) [a] (drop|text) shadow",
                        "%entities%'[s] text (has|have) [a] (drop|text) shadow",
                        "[[the] text of] %entities% (doesn't|does not|do not|don't) have [a] (drop|text) shadow",
                        "%entities%'[s] text (doesn't|does not|do not|don't) have [a] (drop|text) shadow"
                );
                Skript.registerCondition(
                        CondTextDisplaySeeThroughBlocks.class,
                        "%entities% (is|are) visible through (blocks|walls)",
                        "%entities% (isn't|is not|aren't|are not) visible through (blocks|walls)"
                );
                Skript.registerCondition(
                        CondIsResponsive.class,
                        "%entities% (is|are) (responsive|:unresponsive)",
                        "%entities% (isn't|is not|aren't|are not) (responsive|:unresponsive)"
                );
                Skript.registerCondition(
                        CondHasLootTable.class,
                        "%blocks/entities% has [a] loot[ ]table",
                        "%blocks/entities% does(n't| not) have [a] loot[ ]table"
                );
                Skript.registerCondition(
                        CondIsLootable.class,
                        "%blocks/entities% (is|are) lootable",
                        "%blocks/entities% (isn't|is not|aren't|are not) lootable"
                );
                Skript.registerCondition(
                        CondHasPotion.class,
                        "%livingentities% (has|have) ([any|a[n]] [active] potion effect[s]|[any|a] potion effect[s] active)",
                        "%livingentities% (doesn't|does not|do not|don't) have ([any|a[n]] [active] potion effect[s]|[any|a] potion effect[s] active)",
                        "%livingentities% (has|have) %objects% [active]",
                        "%livingentities% (doesn't|does not|do not|don't) have %objects% [active]"
                );
                Skript.registerCondition(
                        CondIsPoisoned.class,
                        "%livingentities% (is|are) poisoned",
                        "%livingentities% (isn't|is not|aren't|are not) poisoned"
                );
                Skript.registerCondition(
                        CondIsPotionAmbient.class,
                        "%objects% (is|are) ambient",
                        "%objects% (isn't|is not|aren't|are not) ambient"
                );
                Skript.registerCondition(
                        CondIsPotionInstant.class,
                        "%objects% (is|are) instant",
                        "%objects% (isn't|is not|aren't|are not) instant"
                );
                Skript.registerCondition(
                        CondPotionHasIcon.class,
                        "%objects% (has|have) ([an] icon|icons)",
                        "%objects% (doesn't|does not|do not|don't) have ([an] icon|icons)"
                );
                Skript.registerCondition(
                        CondPotionHasParticles.class,
                        "%objects% (has|have) particles",
                        "%objects% (doesn't|does not|do not|don't) have particles"
                );
                Skript.registerCondition(
                        CondIsTagged.class,
                        "%objects% (is|are) tagged (as|with) %objects%",
                        "%objects% (isn't|is not|aren't|are not) tagged (as|with) %objects%"
                );
                Skript.registerCondition(
                        CondEquipCompDamage.class,
                        "%objects% will (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))",
                        "%objects% (will not|won't) (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))"
                );
                Skript.registerCondition(
                        CondEquipCompDispensable.class,
                        "%objects% can be dispensed",
                        "%objects% (can not|can't) be dispensed",
                        "%objects% (is|are) (able to be dispensed|dispensable)",
                        "%objects% (isn't|is not|aren't|are not) (able to be dispensed|dispensable)"
                );
                Skript.registerCondition(
                        CondEquipCompInteract.class,
                        "%objects% can be (equipped|put) on[to] entities",
                        "%objects% (can not|can't) be (equipped|put) on[to] entities"
                );
                Skript.registerCondition(
                        CondEquipCompShearable.class,
                        "%objects% can be sheared off [of entities]",
                        "%objects% (can not|can't) be sheared off [of entities]"
                );
                Skript.registerCondition(
                        CondEquipCompSwapEquipment.class,
                        "%objects% can swap equipment [on right click|when right clicked]",
                        "%objects% (can not|can't) swap equipment [on right click|when right clicked]"
                );
                Skript.registerCondition(
                        CondCompare.class,
                        "%objects% (is|are) %objects%",
                        "%objects% (isn't|is not|aren't|are not) %objects%"
                );
                Skript.registerEvent(EvtFabricBlockBreak.class, "on block break");
                Skript.registerEvent(EvtAttackEntity.class, "on attack entity");
                Skript.registerEvent(EvtBrewingFuel.class, EvtBrewingFuel.patterns());
                Skript.registerEvent(EvtDamage.class, "on damage");
                Skript.registerEvent(EvtFabricGameTest.class, "on gametest");
                Skript.registerEvent(EvtFishing.class, EvtFishing.patterns());
                Skript.registerEvent(EvtPlayerInput.class, EvtPlayerInput.patterns());
                Skript.registerEvent(EvtFabricServerTick.class, "on server tick");
                Skript.registerEvent(EvtFabricUseBlock.class, "on use block");
                Skript.registerEvent(EvtUseEntity.class, "on use entity");
                Skript.registerEvent(EvtUseItem.class, "on use item");
                registerRecoveredEventActivationBundle();
                Skript.registerExpression(
                        ExprChestInventory.class,
                        org.skriptlang.skript.fabric.compat.FabricInventory.class,
                        "[a] [new] chest inventory (named|with name) %string% [with %-number% row[s]]",
                        "[a] [new] chest inventory with %number% row[s] [(named|with name) %-string%]"
                );
                Skript.registerExpression(
                        ExprEnderChest.class,
                        org.skriptlang.skript.fabric.compat.FabricInventory.class,
                        "[the] ender[ ]chest[s] of %players%",
                        "%players%'[s] ender[ ]chest[s]"
                );
                Skript.registerExpression(
                        ExprInventory.class,
                        org.skriptlang.skript.fabric.compat.FabricInventory.class,
                        "[the] inventor(y|ies) of %players%",
                        "%players%'[s] inventor(y|ies)"
                );
                Skript.registerExpression(
                        ExprInventoryInfo.class,
                        Object.class,
                        "(1¦holder[s]|2¦[amount of] rows|3¦[amount of] slots) of %inventories%",
                        "%inventories%'[s] (1¦holder[s]|2¦[amount of] rows|3¦[amount of] slots)"
                );
                Skript.registerExpression(
                        ExprInventorySlot.class,
                        net.minecraft.world.inventory.Slot.class,
                        "[the] slot[s] %numbers% of %inventory%",
                        "%inventory%'[s] slot[s] %numbers%"
                );
                Skript.registerExpression(
                        ExprItemsIn.class,
                        net.minecraft.world.inventory.Slot.class,
                        "[all [[of] the]] items ([with]in|of|contained in|out of) [1:inventor(y|ies)] %inventories%",
                        "all [[of] the] %itemtypes% ([with]in|of|contained in|out of) [1:inventor(y|ies)] %inventories%"
                );
                Skript.registerExpression(
                        ExprFirstEmptySlot.class,
                        net.minecraft.world.inventory.Slot.class,
                        "[the] first empty slot[s] of %inventories%",
                        "%inventories%'[s] first empty slot[s]",
                        "[the] first empty slot[s] in %inventories%"
                );
                Skript.registerExpression(
                        ExprLoveTime.class,
                        ch.njol.skript.util.Timespan.class,
                        "[the] love time of %entities%",
                        "%entities%'[s] love time"
                );
                Skript.registerExpression(
                        ExprBrewingFuelLevel.class,
                        Integer.class,
                        "[the] brewing [stand] fuel (level|amount) of %blocks%",
                        "%blocks%'s brewing [stand] fuel (level|amount)"
                );
                Skript.registerExpression(
                        ExprBrewingSlot.class,
                        net.minecraft.world.inventory.Slot.class,
                        ExprBrewingSlot.patterns()
                );
                Skript.registerExpression(
                        ExprBrewingTime.class,
                        ch.njol.skript.util.Timespan.class,
                        "[current|remaining] brewing time of %blocks%",
                        "%blocks%'[s] [current|remaining] brewing time"
                );
                Skript.registerExpression(
                        ExprCausingEntity.class,
                        Entity.class,
                        "[the] (causing|responsible) entity of %damagesources%",
                        "%damagesources%'s (causing|responsible) entity"
                );
                Skript.registerExpression(
                        ExprDirectEntity.class,
                        Entity.class,
                        "[the] direct entity of %damagesources%",
                        "%damagesources%'s direct entity"
                );
                Skript.registerExpression(
                        ExprDamageLocation.class,
                        org.skriptlang.skript.fabric.compat.FabricLocation.class,
                        "[the] damage location of %damagesources%",
                        "%damagesources%'s damage location"
                );
                Skript.registerExpression(
                        ExprSourceLocation.class,
                        org.skriptlang.skript.fabric.compat.FabricLocation.class,
                        "[the] source location of %damagesources%",
                        "%damagesources%'s source location"
                );
                Skript.registerExpression(
                        ExprFoodExhaustion.class,
                        Float.class,
                        "[the] food exhaustion of %damagesources%",
                        "%damagesources%'s food exhaustion"
                );
                Skript.registerExpression(
                        ExprDisplayBillboard.class,
                        Display.BillboardConstraints.class,
                        "billboard of %entities%",
                        "billboarding of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayBrightness.class,
                        Integer.class,
                        "block light override of %entities%",
                        "sky light override of %entities%",
                        "brightness override of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayHeightWidth.class,
                        Float.class,
                        "display height of %entities%",
                        "display width of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayShadow.class,
                        Float.class,
                        "shadow radius of %entities%",
                        "shadow strength of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayInterpolation.class,
                        ch.njol.skript.util.Timespan.class,
                        "interpolation delay of %entities%",
                        "interpolation duration of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayTransformationRotation.class,
                        Quaternionf.class,
                        "left [transformation] rotation of %entities%",
                        "right [transformation] rotation of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayTransformationScaleTranslation.class,
                        Vec3.class,
                        "[display] transformation scale of %entities%",
                        "[display] transformation translation of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayTeleportDuration.class,
                        ch.njol.skript.util.Timespan.class,
                        "teleport duration of %entities%",
                        "teleportation duration of %entities%"
                );
                Skript.registerExpression(
                        ExprDisplayViewRange.class,
                        Float.class,
                        "view range of %entities%",
                        "view radius of %entities%"
                );
                Skript.registerExpression(
                        ExprItemDisplayTransform.class,
                        ItemDisplayContext.class,
                        "[the] item [display] transform of %entities%",
                        "%entities%'[s] item [display] transform"
                );
                Skript.registerExpression(
                        ExprLootTable.class,
                        LootTable.class,
                        "[the] (loot[ ]table|loot[ ]tables) of event-entity",
                        "[the] (loot[ ]table|loot[ ]tables) of event-block",
                        "[the] (loot[ ]table|loot[ ]tables) of %entities%",
                        "%entities%'[s] (loot[ ]table|loot[ ]tables)",
                        "[the] (loot[ ]table|loot[ ]tables) of %blocks%",
                        "%blocks%'[s] (loot[ ]table|loot[ ]tables)"
                );
                Skript.registerExpression(
                        ExprLootTableSeed.class,
                        Long.class,
                        "[the] (loot[[ ]table] seed|loot[[ ]table] seeds) of event-entity",
                        "[the] (loot[[ ]table] seed|loot[[ ]table] seeds) of event-block",
                        "[the] (loot[[ ]table] seed|loot[[ ]table] seeds) of %entities%",
                        "%entities%'[s] (loot[[ ]table] seed|loot[[ ]table] seeds)",
                        "[the] (loot[[ ]table] seed|loot[[ ]table] seeds) of %blocks%",
                        "%blocks%'[s] (loot[[ ]table] seed|loot[[ ]table] seeds)"
                );
                Skript.registerExpression(
                        ExprLootTableFromString.class,
                        LootTable.class,
                        "[the] (loot[ ]table|loot[ ]tables) %strings%"
                );
                Skript.registerExpression(
                        ExprPotionEffects.class,
                        org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.class,
                        "[the] [active] (potion effect|potion effects) of %entities%",
                        "%entities%'[s] [active] (potion effect|potion effects)"
                );
                Skript.registerExpression(
                        ExprPotionDuration.class,
                        ch.njol.skript.util.Timespan.class,
                        "[the] [potion] duration of %objects%",
                        "%objects%'[s] [potion] duration",
                        "[the] [potion] length of %objects%",
                        "%objects%'[s] [potion] length"
                );
                Skript.registerExpression(
                        ExprPotionAmplifier.class,
                        Integer.class,
                        "[the] [potion] amplifier of %objects%",
                        "%objects%'[s] [potion] amplifier",
                        "[the] potion tier of %objects%",
                        "%objects%'[s] potion tier",
                        "[the] potion level of %objects%",
                        "%objects%'[s] potion level"
                );
                Skript.registerExpression(
                        ExprPotionEffect.class,
                        org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.class,
                        "[the] %strings% [active] effect of %entities%",
                        "[the] %strings% [active] effects of %entities%",
                        "%entities%'[s] %strings% [active] effect",
                        "%entities%'[s] %strings% [active] effects",
                        "[the] %objects% [active] effect of %entities%",
                        "[the] %objects% [active] effects of %entities%",
                        "%entities%'[s] %objects% [active] effect",
                        "%entities%'[s] %objects% [active] effects"
                );
                Skript.registerExpression(
                        ExprPotionEffectTypeCategory.class,
                        net.minecraft.world.effect.MobEffectCategory.class,
                        "[the] potion [effect [type]] category of %strings%",
                        "%strings%'[s] potion [effect [type]] category",
                        "[the] potion [effect [type]] category of %objects%",
                        "%objects%'[s] potion [effect [type]] category"
                );
                Skript.registerExpression(
                        ExprTextDisplayAlignment.class,
                        Display.TextDisplay.Align.class,
                        "[the] text alignment of %entities%",
                        "%entities%'[s] text alignment[s]"
                );
                Skript.registerExpression(
                        ExprTextDisplayLineWidth.class,
                        Integer.class,
                        "[the] line width of %entities%",
                        "%entities%'[s] line width"
                );
                Skript.registerExpression(
                        ExprTextDisplayOpacity.class,
                        Integer.class,
                        "[the] [display] [text] opacity of %entities%",
                        "%entities%'[s] [display] [text] opacity"
                );
                Skript.registerExpression(
                        ExprFishingHook.class,
                        Entity.class,
                        "fish[ing] (hook|bobber)"
                );
                Skript.registerExpression(
                        ExprFishingApproachAngle.class,
                        Float.class,
                        "min[imum] fishing approach angle",
                        "min[imum] fishing approaching angle",
                        "max[imum] fishing approach angle",
                        "max[imum] fishing approaching angle"
                );
                Skript.registerExpression(
                        ExprFishingBiteTime.class,
                        ch.njol.skript.util.Timespan.class,
                        "fish[ing] bit(e|ing) [wait] time"
                );
                Skript.registerExpression(
                        ExprFishingHookEntity.class,
                        Entity.class,
                        "hook[ed] entity"
                );
                Skript.registerExpression(
                        ExprFishingWaitTime.class,
                        ch.njol.skript.util.Timespan.class,
                        "min[imum] fishing wait time",
                        "min[imum] fishing waiting time",
                        "max[imum] fishing wait time",
                        "max[imum] fishing waiting time"
                );
                Skript.registerExpression(
                        ExprCurrentInputKeys.class,
                        org.skriptlang.skript.bukkit.input.InputKey.class,
                        "[current] (inputs|input keys) of %players%",
                        "%players%'[s] [current] (inputs|input keys)"
                );
                Skript.registerExpression(
                        ExprInteractionDimensions.class,
                        Float.class,
                        "[the] interaction width of %entities%",
                        "%entities%'[s] interaction width",
                        "[the] interaction height of %entities%",
                        "%entities%'[s] interaction height"
                );
                Skript.registerExpression(
                        ExprLastInteractionPlayer.class,
                        com.mojang.authlib.GameProfile.class,
                        "[the] last player to attack %entities%",
                        "[the] last player who attacked %entities%",
                        "[the] last player to interact with %entities%",
                        "[the] last player who interacted with %entities%",
                        "[the] last player to click on %entities%",
                        "[the] last player who clicked on %entities%"
                );
                Skript.registerExpression(
                        ExprEventBlock.class,
                        FabricBlock.class,
                        "event-block",
                        "event block",
                        "the event-block",
                        "the event block"
                );
                Skript.registerExpression(
                        ExprEventPlayer.class,
                        ServerPlayer.class,
                        "event-player",
                        "event player",
                        "the event-player",
                        "the event player"
                );
                Skript.registerExpression(
                        ExprEventEntity.class,
                        Entity.class,
                        "event-entity",
                        "event entity",
                        "the event-entity",
                        "the event entity"
                );
                Skript.registerExpression(
                        ExprEventItem.class,
                        ItemStack.class,
                        "event-item",
                        "event item",
                        "the event-item",
                        "the event item"
                );
                Skript.registerExpression(
                        ExprEventDamageSource.class,
                        DamageSource.class,
                        "event-damage source",
                        "event damage source",
                        "the event-damage source",
                        "the event damage source"
                );
                Skript.registerExpression(
                        ExprRandomCharacter.class,
                        String.class,
                        "[a|%-integer%] random [:alphanumeric] character[s] (from|between) %string% (to|and) %string%"
                );
                Skript.registerExpression(
                        ExprTimes.class,
                        Long.class,
                        "%number% time[s]",
                        "once",
                        "twice",
                        "thrice"
                );
                initializeRecoveredSyntaxCoreExpressionBundle();
                initializeRecoveredExpressionBundle();
                SkriptFabricAdditionalSyntax.register();
                SkriptFabricAdditionalEffects.register();
                ch.njol.skript.effects.EffActionBar.register();
                ch.njol.skript.effects.EffBroadcast.register();
                ch.njol.skript.effects.EffKick.register();
                ch.njol.skript.effects.EffMessage.register();
                ch.njol.skript.effects.EffOp.register();
                ch.njol.skript.effects.EffPlaySound.register();
                ch.njol.skript.effects.EffResetTitle.register();
                ch.njol.skript.effects.EffSendResourcePack.register();
                ch.njol.skript.effects.EffSendTitle.register();
                ch.njol.skript.effects.EffStopSound.register();
                ch.njol.skript.effects.EffBan.register();
                ch.njol.skript.effects.EffCancelItemUse.register();
                ch.njol.skript.effects.EffCommand.register();
                ch.njol.skript.effects.EffLidState.register();
                ch.njol.skript.effects.EffLook.register();
                ch.njol.skript.effects.EffOpenBook.register();
                ch.njol.skript.effects.EffOpenInventory.register();
                ch.njol.skript.effects.EffPvP.register();
                ch.njol.skript.effects.EffSendBlockChange.register();
                ch.njol.skript.effects.EffTooltip.register();
                ch.njol.skript.effects.EffWardenDisturbance.register();
                ch.njol.skript.effects.EffWorldLoad.register();
                ch.njol.skript.effects.EffWorldSave.register();
                ch.njol.skript.effects.EffStopServer.register();
                ch.njol.skript.effects.EffBlockUpdate.register();
                ch.njol.skript.effects.EffBreakNaturally.register();
                ch.njol.skript.effects.EffCancelCooldown.register();
                ch.njol.skript.effects.EffCancelDrops.register();
                ch.njol.skript.effects.EffCancelEvent.register();
                ch.njol.skript.effects.EffHidePlayerFromServerList.register();
                ch.njol.skript.effects.EffLoadServerIcon.register();
                ch.njol.skript.effects.EffPlayerInfoVisibility.register();
                ch.njol.skript.effects.EffRing.register();
                Skript.registerEffect(
                        EffFeed.class,
                        "feed [the] %players% [by %-number% [beef[s]]]"
                );
                Skript.registerEffect(
                        EffKill.class,
                        "kill %entities%"
                );
                Skript.registerEffect(
                        EffSilence.class,
                        "silence %entities%",
                        "unsilence %entities%",
                        "make %entities% silent",
                        "make %entities% not silent"
                );
                Skript.registerEffect(
                        EffInvisible.class,
                        "make %livingentities% not visible",
                        "make %livingentities% not invisible",
                        "make %livingentities% invisible",
                        "make %livingentities% visible"
                );
                Skript.registerEffect(
                        EffInvulnerability.class,
                        "make %entities% (invulnerable|invincible)",
                        "make %entities% (not (invulnerable|invincible)|vulnerable|vincible)"
                );
                Skript.registerEffect(
                        EffSprinting.class,
                        "make %players% (start sprinting|sprint)",
                        "force %players% to (start sprinting|sprint)",
                        "make %players% (stop sprinting|not sprint)",
                        "force %players% to (stop sprinting|not sprint)"
                );
                Skript.registerEffect(
                        EffSetTestBlock.class,
                        "set test block at %integer% %integer% %integer% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestBlockAtBlock.class,
                        "set test block for %block% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestBlockAboveBlock.class,
                        "set test block above %block% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestBlockAtLocation.class,
                        "set test block at %location% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestBlockUnderPlayer.class,
                        "set test block under player %player% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestEntityName.class,
                        "set test name of entity %entity% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestItemName.class,
                        "set test name of item %itemstack% to %string%"
                );
                Skript.registerEffect(
                        EffSetFishingApproachAngle.class,
                        "set min[imum] fishing approach angle to %object%",
                        "set min[imum] fishing approaching angle to %object%",
                        "set max[imum] fishing approach angle to %object%",
                        "set max[imum] fishing approaching angle to %object%"
                );
                Skript.registerEffect(
                        EffChange.class,
                        "set %object% to %object%",
                        "add %object% to %object%",
                        "remove %object% from %object%",
                        "reset %object%",
                        "delete %object%"
                );
                registerRecoveredEffectBundle();
            } finally {
                Skript.setAcceptRegistrations(false);
            }

            bootstrapped = true;
            SkriptFabric.LOGGER.info("Initialized minimal Skript Fabric runtime bootstrap.");
        }
    }

    private static boolean hasCoreBootstrapState() {
        return hasCoreClassInfos() && hasCoreSyntax();
    }

    private static void registerCoreClassInfos() {
        registerClassInfoIfMissing("player", PlayerClassInfo::register);
        registerClassInfoIfMissing("inventory", InventoryClassInfo::register);
        registerClassInfoIfMissing("itemstack", ItemStackClassInfo::register);
        registerClassInfoIfMissing("itemtype", ItemTypeClassInfo::register);
        registerClassInfoIfMissing("inputkey", InputKeyClassInfo::register);
        registerClassInfoIfMissing("location", LocationClassInfo::register);
        registerClassInfoIfMissing("loottable", LootTableClassInfo::register);
        registerClassInfoIfMissing("nameable", NameableClassInfo::register);
        registerClassInfoIfMissing("billboardconstraints", DisplayBillboardConstraintsClassInfo::register);
        registerClassInfoIfMissing("itemdisplaytransform", ItemDisplayTransformClassInfo::register);
        registerClassInfoIfMissing("offlineplayer", OfflinePlayerClassInfo::register);
        registerClassInfoIfMissing("potioneffectcause", PotionCauseClassInfo::register);
        registerClassInfoIfMissing("quaternion", QuaternionClassInfo::register);
        registerClassInfoIfMissing("textdisplayalign", TextDisplayAlignClassInfo::register);
        registerClassInfoIfMissing("timespan", TimespanClassInfo::register);
        registerClassInfoIfMissing("world", WorldClassInfo::register);
        registerClassInfoIfMissing("entity", EntityClassInfo::register);
        registerClassInfoIfMissing("damagesource", DamageSourceClassInfo::register);
        registerClassInfoIfMissing("block", BlockClassInfo::register);
        registerClassInfoIfMissing("slot", SlotClassInfo::register);
        registerClassInfoIfMissing("vector", VectorClassInfo::register);
        registerClassInfoIfMissing("equippablecomponent", EquippableComponentClassInfo::register);
    }

    private static void registerClassInfoIfMissing(String codeName, Runnable registrar) {
        if (Classes.getClassInfoNoError(codeName) == null) {
            registrar.run();
        }
    }

    private static void initializeRecoveredConditionBundle() {
        forceInitialize(ch.njol.skript.conditions.CondEntityStorageIsFull.class);
        forceInitialize(ch.njol.skript.conditions.CondIsFuel.class);
        forceInitialize(ch.njol.skript.conditions.CondIsOfType.class);
        forceInitialize(ch.njol.skript.conditions.CondIsResonating.class);
        forceInitialize(ch.njol.skript.conditions.CondItemEnchantmentGlint.class);
        forceInitialize(ch.njol.skript.conditions.CondWillHatch.class);
        forceInitialize(ch.njol.skript.conditions.CondCancelled.class);
        forceInitialize(ch.njol.skript.conditions.CondDamageCause.class);
        forceInitialize(ch.njol.skript.conditions.CondEntityUnload.class);
        forceInitialize(ch.njol.skript.conditions.CondIncendiary.class);
        forceInitialize(ch.njol.skript.conditions.CondItemDespawn.class);
        forceInitialize(ch.njol.skript.conditions.CondIsPreferredTool.class);
        forceInitialize(ch.njol.skript.conditions.CondIsSedated.class);
        forceInitialize(ch.njol.skript.conditions.CondLeashWillDrop.class);
        forceInitialize(ch.njol.skript.conditions.CondRespawnLocation.class);
        forceInitialize(ch.njol.skript.conditions.CondScriptLoaded.class);
    }

    private static void initializeRecoveredSyntaxCoreExpressionBundle() {
        forceInitialize(ch.njol.skript.expressions.ExprAttacked.class);
        forceInitialize(ch.njol.skript.expressions.ExprAttacker.class);
        forceInitialize(ch.njol.skript.expressions.ExprAltitude.class);
        forceInitialize(ch.njol.skript.expressions.ExprBlock.class);
        forceInitialize(ch.njol.skript.expressions.ExprBlockData.class);
        forceInitialize(ch.njol.skript.expressions.ExprCarryingBlockData.class);
        forceInitialize(ch.njol.skript.expressions.ExprChunk.class);
        forceInitialize(ch.njol.skript.expressions.ExprChunkX.class);
        forceInitialize(ch.njol.skript.expressions.ExprChunkZ.class);
        forceInitialize(ch.njol.skript.expressions.ExprClientViewDistance.class);
        forceInitialize(ch.njol.skript.expressions.ExprCommandBlockCommand.class);
        forceInitialize(ch.njol.skript.expressions.ExprCommand.class);
        forceInitialize(ch.njol.skript.expressions.ExprCommandSender.class);
        forceInitialize(ch.njol.skript.expressions.ExprDamage.class);
        forceInitialize(ch.njol.skript.expressions.ExprDamageCause.class);
        forceInitialize(ch.njol.skript.expressions.ExprDifficulty.class);
        forceInitialize(ch.njol.skript.expressions.ExprDistance.class);
        forceInitialize(ch.njol.skript.expressions.ExprExperience.class);
        forceInitialize(ch.njol.skript.expressions.ExprFinalDamage.class);
        forceInitialize(ch.njol.skript.expressions.ExprHealReason.class);
        forceInitialize(ch.njol.skript.expressions.ExprHumidity.class);
        forceInitialize(ch.njol.skript.expressions.ExprIP.class);
        forceInitialize(ch.njol.skript.expressions.ExprItemCooldown.class);
        forceInitialize(ch.njol.skript.expressions.ExprLastDamageCause.class);
        forceInitialize(ch.njol.skript.expressions.ExprLanguage.class);
        forceInitialize(ch.njol.skript.expressions.ExprLightLevel.class);
        forceInitialize(ch.njol.skript.expressions.ExprLocation.class);
        forceInitialize(ch.njol.skript.expressions.ExprLocationAt.class);
        forceInitialize(ch.njol.skript.expressions.ExprLocationFromVector.class);
        forceInitialize(ch.njol.skript.expressions.ExprLocationOf.class);
        forceInitialize(ch.njol.skript.expressions.ExprLocationVectorOffset.class);
        forceInitialize(ch.njol.skript.expressions.ExprMOTD.class);
        forceInitialize(ch.njol.skript.expressions.ExprMidpoint.class);
        forceInitialize(ch.njol.skript.expressions.ExprAffectedEntities.class);
        forceInitialize(ch.njol.skript.expressions.ExprBarterInput.class);
        forceInitialize(ch.njol.skript.expressions.ExprConfig.class);
        forceInitialize(ch.njol.skript.expressions.ExprConsumedItem.class);
        forceInitialize(ch.njol.skript.expressions.ExprExperienceCooldownChangeReason.class);
        forceInitialize(ch.njol.skript.expressions.ExprExplodedBlocks.class);
        forceInitialize(ch.njol.skript.expressions.ExprHatchingNumber.class);
        forceInitialize(ch.njol.skript.expressions.ExprHatchingType.class);
        forceInitialize(ch.njol.skript.expressions.ExprHealAmount.class);
        forceInitialize(ch.njol.skript.expressions.ExprHostname.class);
        forceInitialize(ch.njol.skript.expressions.ExprLastAttacker.class);
        forceInitialize(ch.njol.skript.expressions.ExprLeashHolder.class);
        forceInitialize(ch.njol.skript.expressions.ExprLevel.class);
        forceInitialize(ch.njol.skript.expressions.ExprMaxDurability.class);
        forceInitialize(ch.njol.skript.expressions.ExprMaxHealth.class);
        forceInitialize(ch.njol.skript.expressions.ExprMaxItemUseTime.class);
        forceInitialize(ch.njol.skript.expressions.ExprMaxPlayers.class);
        forceInitialize(ch.njol.skript.expressions.ExprMaxStack.class);
        forceInitialize(ch.njol.skript.expressions.ExprMods.class);
        forceInitialize(ch.njol.skript.expressions.ExprNamed.class);
        forceInitialize(ch.njol.skript.expressions.ExprNoDamageTicks.class);
        forceInitialize(ch.njol.skript.expressions.ExprNode.class);
        forceInitialize(ch.njol.skript.expressions.ExprNumbers.class);
        forceInitialize(ch.njol.skript.expressions.ExprOfflinePlayers.class);
        forceInitialize(ch.njol.skript.expressions.ExprOnlinePlayersCount.class);
        forceInitialize(ch.njol.skript.expressions.ExprOps.class);
        forceInitialize(ch.njol.skript.expressions.ExprPandaGene.class);
        forceInitialize(ch.njol.skript.expressions.ExprItemOwner.class);
        forceInitialize(ch.njol.skript.expressions.ExprItemThrower.class);
        forceInitialize(ch.njol.skript.expressions.ExprPing.class);
        forceInitialize(ch.njol.skript.expressions.ExprPlain.class);
        forceInitialize(ch.njol.skript.expressions.ExprPermissions.class);
        forceInitialize(ch.njol.skript.expressions.ExprPlayerProtocolVersion.class);
        forceInitialize(ch.njol.skript.expressions.ExprProtocolVersion.class);
        forceInitialize(ch.njol.skript.expressions.ExprQuitReason.class);
        forceInitialize(ch.njol.skript.expressions.ExprTPS.class);
        forceInitialize(ch.njol.skript.expressions.ExprRawName.class);
        forceInitialize(ch.njol.skript.expressions.ExprRedstoneBlockPower.class);
        forceInitialize(ch.njol.skript.expressions.ExprSeaLevel.class);
        forceInitialize(ch.njol.skript.expressions.ExprSeaPickles.class);
        forceInitialize(ch.njol.skript.expressions.ExprSeed.class);
        forceInitialize(ch.njol.skript.expressions.ExprSimulationDistance.class);
        forceInitialize(ch.njol.skript.expressions.ExprSlotIndex.class);
        forceInitialize(ch.njol.skript.expressions.ExprSourceBlock.class);
        forceInitialize(ch.njol.skript.expressions.ExprSpeed.class);
        forceInitialize(ch.njol.skript.expressions.ExprSpawn.class);
        forceInitialize(ch.njol.skript.expressions.ExprScripts.class);
        forceInitialize(ch.njol.skript.expressions.ExprTamer.class);
        forceInitialize(ch.njol.skript.expressions.ExprTimeState.class);
        forceInitialize(ch.njol.skript.expressions.ExprTool.class);
        forceInitialize(ch.njol.skript.expressions.ExprVersion.class);
        forceInitialize(ch.njol.skript.expressions.ExprVectorBetweenLocations.class);
        forceInitialize(ch.njol.skript.expressions.ExprVectorCrossProduct.class);
        forceInitialize(ch.njol.skript.expressions.ExprVectorDotProduct.class);
        forceInitialize(ch.njol.skript.expressions.ExprVectorLength.class);
        forceInitialize(ch.njol.skript.expressions.ExprVectorNormalize.class);
        forceInitialize(ch.njol.skript.expressions.ExprViewDistance.class);
        forceInitialize(ch.njol.skript.expressions.ExprWardenAngryAt.class);
        forceInitialize(ch.njol.skript.expressions.ExprWardenEntityAnger.class);
        forceInitialize(ch.njol.skript.expressions.ExprWhitelist.class);
        forceInitialize(ch.njol.skript.expressions.ExprWithFireResistance.class);
        forceInitialize(ch.njol.skript.expressions.ExprWithItemFlags.class);
        forceInitialize(ch.njol.skript.expressions.ExprXYZComponent.class);
        forceInitialize(ch.njol.skript.expressions.ExprYawPitch.class);
    }

    private static void initializeRecoveredExpressionBundle() {
        forceInitialize(ExprAI.class);
        forceInitialize(ExprAttackCooldown.class);
        forceInitialize(ExprExhaustion.class);
        forceInitialize(ExprFallDistance.class);
        forceInitialize(ExprFireTicks.class);
        forceInitialize(ExprFlightMode.class);
        forceInitialize(ExprFreezeTicks.class);
        forceInitialize(ExprGravity.class);
        forceInitialize(ExprLastDamage.class);
        forceInitialize(ExprLevelProgress.class);
        forceInitialize(ExprMaxFreezeTicks.class);
    }

    private static void registerRecoveredEffectBundle() {
        ch.njol.skript.effects.EffApplyBoneMeal.register();
        ch.njol.skript.effects.EffEntityUnload.register();
        ch.njol.skript.effects.EffForceEnchantmentGlint.register();
        ch.njol.skript.effects.EffMakeEggHatch.register();
        ch.njol.skript.effects.EffReplace.register();
        ch.njol.skript.effects.EffDetonate.register();
        ch.njol.skript.effects.EffLog.register();
        ch.njol.skript.effects.EffRun.register();
        ch.njol.skript.effects.EffSuppressWarnings.register();
        ch.njol.skript.effects.EffSuppressTypeHints.register();
        ch.njol.skript.effects.EffWorldBorderExpand.register();
        ch.njol.skript.effects.EffCopy.register();
        ch.njol.skript.effects.EffSort.register();
        ch.njol.skript.effects.EffToggle.register();
        ch.njol.skript.effects.EffExceptionDebug.register();
    }

    private static void registerRecoveredEventActivationBundle() {
        ch.njol.skript.events.EvtAtTime.register();
        ch.njol.skript.events.EvtAreaCloudEffect.register();
        ch.njol.skript.events.EvtBeaconEffect.register();
        ch.njol.skript.events.EvtBeaconToggle.register();
        ch.njol.skript.events.EvtBlockFertilize.register();
        ch.njol.skript.events.EvtBlock.register();
        ch.njol.skript.events.EvtBookEdit.register();
        ch.njol.skript.events.EvtBookSign.register();
        ch.njol.skript.events.EvtClick.register();
        ch.njol.skript.events.EvtEntity.register();
        ch.njol.skript.events.EvtExplode.register();
        ch.njol.skript.events.EvtFirework.register();
        ch.njol.skript.events.EvtEntityShootBow.register();
        ch.njol.skript.events.EvtEntityTransform.register();
        ch.njol.skript.events.EvtExplosionPrime.register();
        ch.njol.skript.events.EvtExperienceChange.register();
        ch.njol.skript.events.EvtExperienceCooldownChange.register();
        ch.njol.skript.events.EvtExperienceSpawn.register();
        ch.njol.skript.events.EvtFirstJoin.register();
        ch.njol.skript.events.EvtGameMode.register();
        ch.njol.skript.events.EvtGrow.register();
        ch.njol.skript.events.EvtHarvestBlock.register();
        ch.njol.skript.events.EvtHealing.register();
        ch.njol.skript.events.EvtItem.register();
        ch.njol.skript.events.EvtLeash.register();
        ch.njol.skript.events.EvtMove.register();
        ch.njol.skript.events.EvtMoveOn.register();
        ch.njol.skript.events.EvtPiglinBarter.register();
        ch.njol.skript.events.EvtPlayerArmorChange.register();
        ch.njol.skript.events.EvtPlayerChunkEnter.register();
        ch.njol.skript.events.EvtPlayerCommandSend.register();
        ch.njol.skript.events.EvtPlayerEggThrow.register();
        ch.njol.skript.events.EvtPeriodical.register();
        ch.njol.skript.events.EvtPortal.register();
        ch.njol.skript.events.EvtPressurePlate.register();
        ch.njol.skript.events.EvtResourcePackResponse.register();
        ch.njol.skript.events.EvtRespawn.register();
        ch.njol.skript.events.EvtScript.register();
        ch.njol.skript.events.EvtSkript.register();
        ch.njol.skript.events.EvtSpectate.register();
        ch.njol.skript.events.EvtTeleport.register();
        ch.njol.skript.events.EvtVehicleCollision.register();
        ch.njol.skript.events.EvtWeatherChange.register();
        ch.njol.skript.events.EvtWorld.register();
    }

    private static void forceInitialize(Class<?> type) {
        try {
            Class.forName(type.getName(), true, type.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to initialize syntax class " + type.getName(), e);
        }
    }

    private static boolean hasCoreClassInfos() {
        return Classes.getClassInfoNoError("player") != null
                && Classes.getClassInfoNoError("inventory") != null
                && Classes.getClassInfoNoError("itemstack") != null
                && Classes.getClassInfoNoError("itemtype") != null
                && Classes.getClassInfoNoError("inputkey") != null
                && Classes.getClassInfoNoError("location") != null
                && Classes.getClassInfoNoError("loottable") != null
                && Classes.getClassInfoNoError("nameable") != null
                && Classes.getClassInfoNoError("billboardconstraints") != null
                && Classes.getClassInfoNoError("itemdisplaytransform") != null
                && Classes.getClassInfoNoError("offlineplayer") != null
                && Classes.getClassInfoNoError("potioneffectcause") != null
                && Classes.getClassInfoNoError("quaternion") != null
                && Classes.getClassInfoNoError("textdisplayalign") != null
                && Classes.getClassInfoNoError("timespan") != null
                && Classes.getClassInfoNoError("world") != null
                && Classes.getClassInfoNoError("entity") != null
                && Classes.getClassInfoNoError("damagesource") != null
                && Classes.getClassInfoNoError("block") != null
                && Classes.getClassInfoNoError("slot") != null
                && Classes.getClassInfoNoError("vector") != null
                && Classes.getClassInfoNoError("equippablecomponent") != null;
    }

    private static boolean hasCoreProperties() {
        return Skript.instance().registry(PropertyRegistry.class).isRegistered("name");
    }

    private static boolean hasCoreSyntax() {
        return hasSyntax(SyntaxRegistry.SECTION, SecIf.class)
                && hasSyntax(SyntaxRegistry.EVENT, EvtFabricGameTest.class)
                && hasSyntax(SyntaxRegistry.EVENT, EvtDamage.class)
                && hasSyntax(SyntaxRegistry.CONDITION, CondAI.class)
                && hasSyntax(SyntaxRegistry.CONDITION, CondIsBurning.class)
                && hasSyntax(SyntaxRegistry.EFFECT, ch.njol.skript.effects.EffActionBar.class)
                && hasSyntax(SyntaxRegistry.EFFECT, org.skriptlang.skript.bukkit.base.effects.EffSilence.class)
                && hasSyntax(SyntaxRegistry.EXPRESSION, ExprLoveTime.class)
                && hasSyntax(SyntaxRegistry.EXPRESSION, ExprEventPlayer.class)
                && hasSyntax(SyntaxRegistry.EXPRESSION, ExprAI.class);
    }

    private static boolean hasSyntax(String key, Class<?> type) {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(key)) {
            if (info.type() == type) {
                return true;
            }
        }
        return false;
    }
}
