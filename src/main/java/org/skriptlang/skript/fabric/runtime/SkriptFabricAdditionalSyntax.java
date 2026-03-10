package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.ExprArrowKnockbackStrength;
import ch.njol.skript.expressions.ExprArrowPierceLevel;
import ch.njol.skript.expressions.ExprBarterDrops;
import ch.njol.skript.expressions.ExprClicked;
import ch.njol.skript.expressions.ExprDrops;
import ch.njol.skript.expressions.ExprExplosionBlockYield;
import ch.njol.skript.expressions.ExprExplosionYield;
import ch.njol.skript.expressions.ExprExplosiveYield;
import ch.njol.skript.expressions.ExprFertilizedBlocks;
import ch.njol.skript.expressions.ExprHanging;
import ch.njol.skript.expressions.ExprLastSpawnedEntity;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.skriptlang.skript.bukkit.breeding.elements.ExprBreedingFamily;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingResults;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprCreatedDamageSource;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprDamageType;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSecDamageSource;
import org.skriptlang.skript.bukkit.displays.generic.ExprDisplayGlowOverride;
import org.skriptlang.skript.bukkit.furnace.elements.ExprFurnaceEventItems;
import org.skriptlang.skript.bukkit.furnace.elements.ExprFurnaceSlot;
import org.skriptlang.skript.bukkit.furnace.elements.ExprFurnaceTime;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprInteractionDimensions;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprLastInteractionDate;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprEquipCompCameraOverlay;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprEquipCompEntities;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprEquipCompEquipSound;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprEquipCompModel;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprEquipCompShearSound;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprEquipCompSlot;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprEquippableComponent;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprSecBlankEquipComp;
import org.skriptlang.skript.bukkit.itemcomponents.generic.ExprItemCompCopy;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLoot;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContext;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextEntity;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLocation;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLooter;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLuck;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootItems;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContext;
import org.skriptlang.skript.bukkit.misc.expressions.ExprItemOfEntity;
import org.skriptlang.skript.bukkit.misc.expressions.ExprQuaternionAxisAngle;
import org.skriptlang.skript.bukkit.misc.expressions.ExprRotate;
import org.skriptlang.skript.bukkit.misc.expressions.ExprTextOf;
import org.skriptlang.skript.bukkit.particles.GameEffect;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprGameEffectWithData;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleCount;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleDistribution;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleOffset;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleSpeed;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleWithData;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleWithOffset;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleWithSpeed;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprEventPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprEventPotionEffectAction;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprSecPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprSkriptPotionEffect;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.tags.MinecraftTag;
import org.skriptlang.skript.bukkit.tags.elements.ExprTag;
import org.skriptlang.skript.bukkit.tags.elements.ExprTagContents;
import org.skriptlang.skript.bukkit.tags.elements.ExprTagKey;
import org.skriptlang.skript.bukkit.tags.elements.ExprTagsOf;
import org.skriptlang.skript.bukkit.tags.elements.ExprTagsOfType;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.syntax.event.EvtBucketCatch;
import org.skriptlang.skript.fabric.syntax.event.EvtBreeding;
import org.skriptlang.skript.fabric.syntax.event.EvtBrewingComplete;
import org.skriptlang.skript.fabric.syntax.event.EvtBrewingStart;
import org.skriptlang.skript.fabric.syntax.event.EvtEntityPotion;
import org.skriptlang.skript.fabric.syntax.event.EvtFurnace;
import org.skriptlang.skript.fabric.syntax.event.EvtLoveModeEnter;
import org.skriptlang.skript.fabric.syntax.event.EvtLootGenerate;

final class SkriptFabricAdditionalSyntax {

    private SkriptFabricAdditionalSyntax() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void register() {
        forceInitialize(ch.njol.skript.conditions.CondChatColors.class);
        forceInitialize(ch.njol.skript.conditions.CondChatFiltering.class);
        forceInitialize(ch.njol.skript.conditions.CondChatVisibility.class);
        forceInitialize(ch.njol.skript.conditions.CondElytraBoostConsume.class);
        forceInitialize(ch.njol.skript.conditions.CondFromMobSpawner.class);
        forceInitialize(ch.njol.skript.conditions.CondHasClientWeather.class);
        forceInitialize(ch.njol.skript.conditions.CondHasMetadata.class);
        forceInitialize(ch.njol.skript.conditions.CondHasResourcePack.class);
        forceInitialize(ch.njol.skript.conditions.CondIsEnchanted.class);
        forceInitialize(ch.njol.skript.conditions.CondIsPluginEnabled.class);
        forceInitialize(ch.njol.skript.conditions.CondIsSkriptCommand.class);
        forceInitialize(ch.njol.skript.conditions.CondIsSlimeChunk.class);
        forceInitialize(ch.njol.skript.conditions.CondIsSpawnable.class);
        forceInitialize(ch.njol.skript.conditions.CondLeashed.class);
        forceInitialize(ch.njol.skript.conditions.CondResourcePack.class);
        ch.njol.skript.events.EvtEntityBlockChange.register();
        ch.njol.skript.events.EvtGrow.register();
        ch.njol.skript.events.EvtPlantGrowth.register();
        ch.njol.skript.events.EvtPressurePlate.register();
        ch.njol.skript.events.EvtResourcePackResponse.register();
        ch.njol.skript.events.EvtVehicleCollision.register();
        Skript.registerEvent(EvtBucketCatch.class, EvtBucketCatch.patterns());
        Skript.registerEvent(EvtBreeding.class, EvtBreeding.patterns());
        Skript.registerEvent(EvtBrewingComplete.class, EvtBrewingComplete.patterns());
        Skript.registerEvent(EvtBrewingStart.class, EvtBrewingStart.patterns());
        Skript.registerEvent(EvtEntityPotion.class, EvtEntityPotion.patterns());
        Skript.registerEvent(EvtLoveModeEnter.class, EvtLoveModeEnter.patterns());
        Skript.registerEvent(EvtLootGenerate.class, "on loot generate");
        Skript.registerEvent(
                EvtFurnace.class,
                EvtFurnace.patterns()
        );

        Skript.registerExpression(
                ExprArrowKnockbackStrength.class,
                Long.class,
                "arrow knockback strength of %entities%",
                "%entities%'[s] arrow knockback strength"
        );
        Skript.registerExpression(
                ExprArrowPierceLevel.class,
                Long.class,
                "arrow pierce level of %entities%",
                "%entities%'[s] arrow pierce level"
        );
        Skript.registerExpression(
                ExprBarterDrops.class,
                (Class) FabricItemType.class,
                "[the] [piglin] barter[ing] drops"
        );
        Skript.registerExpression(
                ExprClicked.class,
                Object.class,
                "[the] clicked (block|%-*itemtype/entitydata%)"
        );
        Skript.registerExpression(
                ExprDrops.class,
                (Class) FabricItemType.class,
                "[the] drops"
        );
        Skript.registerExpression(
                ExprExplosionBlockYield.class,
                Number.class,
                "[the] [explosion['s]] block (yield|amount)",
                "[the] percentage of blocks dropped"
        );
        Skript.registerExpression(
                ExprExplosionYield.class,
                Number.class,
                "[the] explosion (yield|radius|size)",
                "[the] (yield|radius|size) of [the] explosion"
        );
        Skript.registerExpression(
                ExprExplosiveYield.class,
                Number.class,
                "explosive (yield|radius|size|power) of %entities%",
                "%entities%'[s] explosive (yield|radius|size|power)"
        );
        Skript.registerExpression(
                ExprFertilizedBlocks.class,
                (Class) FabricBlock.class,
                "[all] [the] fertilized blocks"
        );
        Skript.registerExpression(
                ExprHanging.class,
                Entity.class,
                "[the] hanging (entity|:remover)"
        );
        Skript.registerExpression(
                ExprLastSpawnedEntity.class,
                Entity.class,
                "[the] [last[ly]] (0:spawned|1:shot) %*entitydata%",
                "[the] [last[ly]] dropped (2:item)",
                "[the] [last[ly]] (created|struck) (3:lightning)",
                "[the] [last[ly]] (launched|deployed) (4:firework)"
        );

        Skript.registerExpression(
                ExprBreedingFamily.class,
                LivingEntity.class,
                "breeding mother",
                "breeding father",
                "bred offspring",
                "breeder"
        );
        Skript.registerExpression(
                ExprBrewingResults.class,
                ItemStack.class,
                "[the] brewing results"
        );
        Skript.registerExpression(
                ExprCreatedDamageSource.class,
                DamageSource.class,
                "[the] created damage source"
        );
        Skript.registerExpression(
                ExprDamageType.class,
                String.class,
                "[the] damage type",
                "[the] damage type of %damagesources%",
                "%damagesources%'[s] damage type"
        );
        Skript.registerExpression(
                ExprSecDamageSource.class,
                DamageSource.class,
                "[a] [custom] damage source [of [damage] type %-objects%]"
        );
        Skript.registerExpression(
                ExprDisplayGlowOverride.class,
                Color.class,
                "[the] glow [color] override of %entities%",
                "%entities%'[s] glow [color] override"
        );
        Skript.registerExpression(
                org.skriptlang.skript.bukkit.base.expressions.ExprGlowing.class,
                Boolean.class,
                "[the] glowing of %entities%",
                "%entities%'[s] glowing"
        );
        Skript.registerExpression(
                ExprEventPotionEffect.class,
                (Class) SkriptPotionEffect.class,
                "[the] event-potion effect",
                "[the] event potion effect"
        );
        Skript.registerExpression(
                ExprEventPotionEffectAction.class,
                String.class,
                "[the] event-potion effect action",
                "[the] event potion effect action"
        );
        Skript.registerExpression(
                ExprFurnaceEventItems.class,
                ItemStack.class,
                "smelted item",
                "extracted item",
                "smelting item",
                "burned fuel"
        );
        Skript.registerExpression(
                ExprFurnaceSlot.class,
                Slot.class,
                ExprFurnaceSlot.patterns()
        );
        Skript.registerExpression(
                ExprFurnaceTime.class,
                Timespan.class,
                "[the] cook time",
                "[the] cook time of %blocks%",
                "[the] cooking time",
                "[the] cooking time of %blocks%",
                "[the] total cook time",
                "[the] total cook time of %blocks%",
                "[the] total cooking time",
                "[the] total cooking time of %blocks%",
                "[the] fuel burn time",
                "[the] fuel burn time of %blocks%"
        );
        Skript.registerExpression(
                ExprLastInteractionDate.class,
                Date.class,
                "[the] last attack (date|time) of %entities%",
                "[the] last interaction (date|time) of %entities%",
                "[the] last click (date|time) of %entities%",
                "[the] last (date|time)[s] [that|when] %entities% (were|was) attacked",
                "[the] last (date|time)[s] [that|when] %entities% (were|was) interacted with",
                "[the] last (date|time)[s] [that|when] %entities% (were|was) clicked [on]"
        );
        Skript.registerExpression(
                ExprInteractionDimensions.class,
                Float.class,
                "[the] interaction widths of %entities%",
                "%entities%'[s] interaction widths",
                "[the] interaction heights of %entities%",
                "%entities%'[s] interaction heights"
        );
        Skript.registerExpression(
                ExprEquippableComponent.class,
                (Class) EquippableWrapper.class,
                "[the] equippable component[s] of %objects%",
                "%objects%'[s] equippable component[s]"
        );
        Skript.registerExpression(
                ExprSecBlankEquipComp.class,
                (Class) EquippableWrapper.class,
                "a (blank|empty) equippable component"
        );
        Skript.registerExpression(
                ExprEquipCompCameraOverlay.class,
                String.class,
                "[the] camera overlay of %objects%",
                "%objects%'[s] camera overlay"
        );
        Skript.registerExpression(
                ExprEquipCompEntities.class,
                (Class) EntityType.class,
                "[the] allowed entities of %objects%",
                "%objects%'[s] allowed entities"
        );
        Skript.registerExpression(
                ExprEquipCompEquipSound.class,
                String.class,
                "[the] equip sound of %objects%",
                "%objects%'[s] equip sound"
        );
        Skript.registerExpression(
                ExprEquipCompModel.class,
                String.class,
                "[the] equipped (model|asset) (key|id) of %objects%",
                "%objects%'[s] equipped (model|asset) (key|id)"
        );
        Skript.registerExpression(
                ExprEquipCompShearSound.class,
                String.class,
                "[the] shear[ed [off]] sound of %objects%",
                "%objects%'[s] shear[ed [off]] sound"
        );
        Skript.registerExpression(
                ExprEquipCompSlot.class,
                EquipmentSlot.class,
                "[the] equipment slot of %objects%",
                "%objects%'[s] equipment slot"
        );
        Skript.registerExpression(
                ExprItemCompCopy.class,
                (Class) ComponentWrapper.class,
                "[the|a[n]] [item] component copy of %objects%",
                "[the] [item] component copies of %objects%"
        );
        Skript.registerExpression(
                ExprSecCreateLootContext.class,
                (Class) LootContextWrapper.class,
                "[a] loot[ ]context at %locations%"
        );
        Skript.registerExpression(
                ExprLoot.class,
                ItemStack.class,
                "[the] loot"
        );
        Skript.registerExpression(
                ExprLootContext.class,
                (Class) LootContextWrapper.class,
                "loot[ ]context"
        );
        Skript.registerExpression(
                ExprLootContextEntity.class,
                Entity.class,
                "[the] looted entity",
                "[the] looted entity of %objects%"
        );
        Skript.registerExpression(
                ExprLootContextLocation.class,
                FabricLocation.class,
                "[the] loot location",
                "[the] loot location of %objects%"
        );
        Skript.registerExpression(
                ExprLootContextLooter.class,
                ServerPlayer.class,
                "[the] looter",
                "[the] looter of %objects%"
        );
        Skript.registerExpression(
                ExprLootContextLuck.class,
                Float.class,
                "[the] loot luck",
                "[the] loot luck of %objects%"
        );
        Skript.registerExpression(
                ExprLootItems.class,
                ItemStack.class,
                "[the] loot items of %objects%",
                "[the] loot items of %objects% with %objects%"
        );
        Skript.registerExpression(
                ExprItemOfEntity.class,
                Slot.class,
                "[the] item [inside] of %entities%",
                "%entities%'[s] item [inside]"
        );
        Skript.registerExpression(
                ExprQuaternionAxisAngle.class,
                Object.class,
                "rotation angle of %quaternions%",
                "rotation axis of %quaternions%"
        );
        Skript.registerExpression(
                ExprRotate.class,
                Object.class,
                "%objects% rotated around [the] x axis by %number%",
                "%objects% rotated around [the] y axis by %number%",
                "%objects% rotated around [the] z axis by %number%",
                "%objects% locally rotated around [the] x axis by %number%",
                "%objects% locally rotated around [the] y axis by %number%",
                "%objects% locally rotated around [the] z axis by %number%",
                "%objects% rotated around %vector% by %number%",
                "%objects% rotated by %number%, %number%, %number%"
        );
        Skript.registerExpression(
                ExprTextOf.class,
                String.class,
                "[the] text of %entities%",
                "%entities%'[s] text"
        );
        Skript.registerExpression(
                ExprGameEffectWithData.class,
                (Class) GameEffect.class,
                "potion break effect [colored] %object%",
                "bone meal effect [with %number% particles]"
        );
        Skript.registerExpression(
                ExprParticleCount.class,
                Integer.class,
                "[the] particle count of %objects%",
                "%objects%'[s] particle count"
        );
        Skript.registerExpression(
                ExprParticleDistribution.class,
                Vec3.class,
                "[the] particle distribution of %objects%",
                "%objects%'[s] particle distribution"
        );
        Skript.registerExpression(
                ExprParticleOffset.class,
                Vec3.class,
                "[the] particle offset of %objects%",
                "%objects%'[s] particle offset"
        );
        Skript.registerExpression(
                ExprParticleSpeed.class,
                Double.class,
                "[the] (particle speed [value]|extra value) of %objects%",
                "%objects%'[s] (particle speed [value]|extra value)"
        );
        Skript.registerExpression(
                ExprParticleWithData.class,
                (Class) ParticleEffect.class,
                "[%-number%|a[n]] dust particle[s] [colored] %object% [with size %-number%]",
                "[%-number%|a[n]] item particle[s] [of] %object%"
        );
        Skript.registerExpression(
                ExprParticleWithOffset.class,
                (Class) ParticleEffect.class,
                "%objects% with [a] particle offset [of] %object%",
                "%objects% with [a] particle distribution [of] %object%",
                "%objects% with [a] particle direction [of] %object%"
        );
        Skript.registerExpression(
                ExprParticleWithSpeed.class,
                (Class) ParticleEffect.class,
                "%objects% with ([a] particle speed [value]|[an] extra value) [of] %number%"
        );
        Skript.registerExpression(
                ExprSkriptPotionEffect.class,
                (Class) SkriptPotionEffect.class,
                "[created] [potion] effect"
        );
        Skript.registerExpression(
                ExprSecPotionEffect.class,
                (Class) SkriptPotionEffect.class,
                "[a[n]] [ambient] potion effect of %objects% [[of tier] %-number%] [for %-timespan%]",
                "[an] (infinite|permanent) [ambient] potion effect of %objects% [[of tier] %-number%]",
                "[an] (infinite|permanent) [ambient] %objects% [[of tier] %-number%] [potion [effect]]",
                "[a] potion effect [of %-objects%] (from|using|based on) %objects%"
        );
        Skript.registerExpression(
                ExprTag.class,
                (Class) MinecraftTag.class,
                "tag %strings%",
                "item tag %strings%",
                "block tag %strings%",
                "entity tag %strings%",
                "minecraft tag %strings%",
                "minecraft item tag %strings%",
                "minecraft block tag %strings%",
                "minecraft entity tag %strings%",
                "vanilla tag %strings%",
                "vanilla item tag %strings%",
                "vanilla block tag %strings%",
                "vanilla entity tag %strings%",
                "skript tag %strings%",
                "skript item tag %strings%",
                "skript block tag %strings%",
                "skript entity tag %strings%"
        );
        Skript.registerExpression(
                ExprTagKey.class,
                String.class,
                "[the] [namespace[d]] key[s] of %objects%",
                "%objects%'[s] [namespace[d]] key[s]"
        );
        Skript.registerExpression(
                ExprTagContents.class,
                Object.class,
                "[the] tag contents of %objects%",
                "%objects%'[s] tag contents"
        );
        Skript.registerExpression(
                ExprTagsOf.class,
                (Class) MinecraftTag.class,
                "[the] tags of %objects%",
                "[the] item tags of %objects%",
                "[the] block tags of %objects%",
                "[the] entity tags of %objects%"
        );
        Skript.registerExpression(
                ExprTagsOfType.class,
                (Class) MinecraftTag.class,
                "[the] [all] tags",
                "[the] [all] item tags",
                "[the] [all] block tags",
                "[the] [all] entity tags"
        );
    }

    private static void forceInitialize(Class<?> type) {
        try {
            Class.forName(type.getName(), true, type.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
