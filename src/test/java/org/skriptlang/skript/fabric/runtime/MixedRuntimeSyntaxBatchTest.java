package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.conditions.CondCancelled;
import ch.njol.skript.conditions.CondChatColors;
import ch.njol.skript.conditions.CondChatFiltering;
import ch.njol.skript.conditions.CondChatVisibility;
import ch.njol.skript.conditions.CondDamageCause;
import ch.njol.skript.conditions.CondFromMobSpawner;
import ch.njol.skript.conditions.CondHasClientWeather;
import ch.njol.skript.conditions.CondHasResourcePack;
import ch.njol.skript.conditions.CondIsPreferredTool;
import ch.njol.skript.conditions.CondIsSedated;
import ch.njol.skript.conditions.CondIsLeashed;
import ch.njol.skript.conditions.CondLeashWillDrop;
import ch.njol.skript.conditions.CondResourcePack;
import ch.njol.skript.conditions.CondRespawnLocation;
import ch.njol.skript.effects.EffCopy;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.effects.EffDoIf;
import ch.njol.skript.effects.EffEquip;
import ch.njol.skript.effects.EffExceptionDebug;
import ch.njol.skript.effects.EffHealth;
import ch.njol.skript.effects.EffSort;
import ch.njol.skript.effects.EffToggle;
import ch.njol.skript.events.EvtBeaconEffect;
import ch.njol.skript.events.EvtBeaconToggle;
import ch.njol.skript.events.EvtBlock;
import ch.njol.skript.events.EvtBookEdit;
import ch.njol.skript.events.EvtBookSign;
import ch.njol.skript.events.EvtClick;
import ch.njol.skript.events.EvtEntity;
import ch.njol.skript.events.EvtEntityBlockChange;
import ch.njol.skript.events.EvtEntityTransform;
import ch.njol.skript.events.EvtExperienceSpawn;
import ch.njol.skript.events.EvtGrow;
import ch.njol.skript.events.EvtHealing;
import ch.njol.skript.events.EvtItem;
import ch.njol.skript.events.EvtMove;
import ch.njol.skript.events.EvtPlantGrowth;
import ch.njol.skript.events.EvtPlayerChunkEnter;
import ch.njol.skript.events.EvtPlayerCommandSend;
import ch.njol.skript.events.EvtPressurePlate;
import ch.njol.skript.events.EvtResourcePackResponse;
import ch.njol.skript.events.EvtVehicleCollision;
import ch.njol.skript.expressions.ExprAffectedEntities;
import ch.njol.skript.expressions.ExprArrowKnockbackStrength;
import ch.njol.skript.expressions.ExprArrowPierceLevel;
import ch.njol.skript.expressions.ExprBarterDrops;
import ch.njol.skript.expressions.ExprClicked;
import ch.njol.skript.expressions.ExprConsumedItem;
import ch.njol.skript.expressions.ExprDrops;
import ch.njol.skript.expressions.ExprExplodedBlocks;
import ch.njol.skript.expressions.ExprExplosionBlockYield;
import ch.njol.skript.expressions.ExprExplosionYield;
import ch.njol.skript.expressions.ExprExplosiveYield;
import ch.njol.skript.expressions.ExprFertilizedBlocks;
import ch.njol.skript.expressions.ExprHatchingNumber;
import ch.njol.skript.expressions.ExprHanging;
import ch.njol.skript.expressions.ExprHealAmount;
import ch.njol.skript.expressions.ExprLastSpawnedEntity;
import ch.njol.skript.expressions.ExprLevel;
import ch.njol.skript.expressions.ExprMaxDurability;
import ch.njol.skript.expressions.ExprRawName;
import ch.njol.skript.expressions.ExprSpeed;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.config.SectionNode;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("isolated-registry")
final class MixedRuntimeSyntaxBatchTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void mixedConditionsParseThroughBootstrap() {
        assertInstanceOf(CondCancelled.class, parseConditionInEvent("event is cancelled", CancelledHandle.class));
        assertInstanceOf(CondDamageCause.class, parseConditionInEvent(
                "damage was caused by \"fire\"",
                FabricDamageSourceEventHandle.class
        ));
        assertInstanceOf(CondLeashWillDrop.class, parseConditionInEvent(
                "the leash will drop",
                resolveEventClass("ch.njol.skript.effects.FabricEffectEventHandles$EntityUnleash")
        ));
        assertInstanceOf(CondChatColors.class, parseConditionInEvent(
                "event-player can see chat colors",
                FabricUseEntityHandle.class
        ));
        assertInstanceOf(CondChatFiltering.class, parseConditionInEvent(
                "event-player is using text filtering",
                FabricUseEntityHandle.class
        ));
        assertInstanceOf(CondChatVisibility.class, parseConditionInEvent(
                "event-player can see all messages",
                FabricUseEntityHandle.class
        ));
        assertInstanceOf(CondFromMobSpawner.class, parseConditionInEvent(
                "event-entity is from a spawner",
                ch.njol.skript.events.FabricEventCompatHandles.EntityLifecycle.class
        ));
        assertInstanceOf(CondHasClientWeather.class, parseConditionInEvent(
                "event-player is using custom weather",
                FabricUseEntityHandle.class
        ));
        assertInstanceOf(CondHasResourcePack.class, parseConditionInEvent(
                "event-player is using a resource pack",
                FabricUseEntityHandle.class
        ));
        assertInstanceOf(CondIsLeashed.class, parseConditionInEvent(
                "event-entity is leashed",
                ch.njol.skript.events.FabricEventCompatHandles.EntityLifecycle.class
        ));
        assertInstanceOf(CondRespawnLocation.class, parseConditionInEvent(
                "respawn location is a bed",
                resolveEventClass("ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn")
        ));
        assertInstanceOf(CondResourcePack.class, parseConditionInEvent(
                "resource pack was accepted",
                ch.njol.skript.events.FabricEventCompatHandles.ResourcePackResponse.class
        ));
        assertInstanceOf(CondIsPreferredTool.class, parseConditionInEvent(
                "diamond pickaxe is the preferred tool for event-block",
                ch.njol.skript.events.FabricEventCompatHandles.BeaconToggle.class
        ));
        assertInstanceOf(CondIsSedated.class, parseConditionInEvent(
                "event-block is sedated",
                ch.njol.skript.events.FabricEventCompatHandles.BeaconToggle.class
        ));
    }

    @Test
    void mixedExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprConsumedItem.class, parseExpressionInEvent(
                "consumed item",
                ch.njol.skript.events.FabricEventCompatHandles.Item.class
        ));
        assertInstanceOf(ExprHealAmount.class, parseExpressionInEvent(
                "heal amount",
                ch.njol.skript.events.FabricEventCompatHandles.Healing.class
        ));
        assertInstanceOf(ExprExplodedBlocks.class, parseExpressionInEvent(
                "exploded blocks",
                ch.njol.skript.events.FabricEventCompatHandles.Explosion.class
        ));
        assertInstanceOf(ExprExplosionBlockYield.class, parseExpressionInEvent(
                "explosion block yield",
                ch.njol.skript.events.FabricEventCompatHandles.Explosion.class
        ));
        assertInstanceOf(ExprBarterDrops.class, parseExpressionInEvent(
                "barter drops",
                ch.njol.skript.events.FabricEventCompatHandles.PiglinBarter.class
        ));
        assertInstanceOf(ExprAffectedEntities.class, parseExpressionInEvent(
                "affected entities",
                ch.njol.skript.events.FabricEventCompatHandles.AreaEffectCloudApply.class
        ));
        assertInstanceOf(ExprHatchingNumber.class, parseExpressionInEvent(
                "hatching number",
                resolveEventClass("ch.njol.skript.effects.FabricEffectEventHandles$PlayerEggThrow")
        ));
        assertInstanceOf(ExprLevel.class, parseExpressionInEvent("xp level of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprSpeed.class, parseExpressionInEvent("walk speed of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprArrowKnockbackStrength.class, parseExpressionInEvent(
                "arrow knockback strength of event-entity",
                ch.njol.skript.events.FabricEventCompatHandles.EntityLifecycle.class
        ));
        assertInstanceOf(ExprArrowPierceLevel.class, parseExpressionInEvent(
                "arrow pierce level of event-entity",
                ch.njol.skript.events.FabricEventCompatHandles.EntityLifecycle.class
        ));
        assertInstanceOf(ExprClicked.class, parseExpressionInEvent("clicked entity", ch.njol.skript.events.FabricEventCompatHandles.Click.class));
        assertInstanceOf(ExprDrops.class, parseExpressionInEvent(
                "drops",
                resolveEventClass("ch.njol.skript.effects.FabricEffectEventHandles$EntityDeath")
        ));
        assertInstanceOf(ExprExplosionYield.class, parseExpressionInEvent(
                "explosion yield",
                resolveEventClass("ch.njol.skript.effects.FabricEffectEventHandles$ExplosionPrime")
        ));
        assertInstanceOf(ExprExplosiveYield.class, parseExpressionInEvent(
                "explosive yield of event-entity",
                ch.njol.skript.events.FabricEventCompatHandles.EntityLifecycle.class
        ));
        assertInstanceOf(ExprFertilizedBlocks.class, parseExpressionInEvent(
                "fertilized blocks",
                ch.njol.skript.events.FabricEventCompatHandles.BlockFertilize.class
        ));
        assertInstanceOf(ExprHanging.class, parseExpressionInEvent(
                "hanging entity",
                resolveEventClass("ch.njol.skript.effects.FabricEffectEventHandles$HangingBreak")
        ));
        assertInstanceOf(ExprLastSpawnedEntity.class, parseExpression("last dropped item"));
        assertInstanceOf(ExprMaxDurability.class, parseExpression("maximum durability of diamond sword"));
        assertInstanceOf(ExprRawName.class, parseExpression("minecraft name of diamond sword"));
    }

    @Test
    void mixedEffectsParseThroughBootstrap() {
        assertInstanceOf(EffCopy.class, parseEffect("copy {source::*} to {target::*}"));
        assertInstanceOf(EffSort.class, parseEffect("sort {values::*}"));
        assertInstanceOf(EffExceptionDebug.class, parseEffect("cause exception"));
        assertInstanceOf(Delay.class, parseEffect("wait 1 tick"));
        assertInstanceOf(EffDoIf.class, parseEffectInEvent("set test name of entity event-entity to \"x\" if event-entity is visible", FabricUseEntityHandle.class));
        assertInstanceOf(EffEquip.class, parseEffectInEvent("equip event-entity with diamond helmet", FabricUseEntityHandle.class));
        assertInstanceOf(EffHealth.class, parseEffectInEvent("damage event-entity by 1 heart", FabricUseEntityHandle.class));
        assertInstanceOf(EffToggle.class, parseEffectInEvent("toggle gravity of event-entity", FabricUseEntityHandle.class));
    }

    @Test
    void mixedEventsParseThroughBootstrap() {
        assertInstanceOf(EvtBookEdit.class, parseEvent("book edit"));
        assertInstanceOf(EvtBookSign.class, parseEvent("book signing"));
        assertInstanceOf(EvtBeaconEffect.class, parseEvent("secondary beacon effect of speed"));
        assertInstanceOf(EvtBeaconToggle.class, parseEvent("beacon activation"));
        assertInstanceOf(EvtBlock.class, parseEvent("block breaking of stone"));
        assertInstanceOf(EvtClick.class, parseEvent("right click with stick on stone"));
        assertInstanceOf(EvtEntity.class, parseEvent("spawning of zombie"));
        assertInstanceOf(EvtEntityBlockChange.class, parseEvent("sheep eat"));
        assertInstanceOf(EvtEntityTransform.class, parseEvent("zombie transforming due to \"curing\""));
        assertInstanceOf(EvtExperienceSpawn.class, parseEvent("experience orb spawn"));
        assertInstanceOf(EvtGrow.class, parseEvent("growth"));
        assertInstanceOf(EvtHealing.class, parseEvent("healing of zombie by \"magic\""));
        assertInstanceOf(EvtItem.class, parseEvent("item spawn of stick"));
        assertInstanceOf(EvtMove.class, parseEvent("player move"));
        assertInstanceOf(EvtPlantGrowth.class, parseEvent("plant growth"));
        assertInstanceOf(EvtPlayerChunkEnter.class, parseEvent("player enters a chunk"));
        assertInstanceOf(EvtPlayerCommandSend.class, parseEvent("send command list"));
        assertInstanceOf(EvtPressurePlate.class, parseEvent("tripwire"));
        assertInstanceOf(EvtResourcePackResponse.class, parseEvent("resource pack accepted"));
        assertInstanceOf(EvtVehicleCollision.class, parseEvent("vehicle entity collision of zombie"));
    }

    private Condition parseCondition(String input) {
        Condition parsed = Condition.parse(input, "failed");
        assertNotNull(parsed, input);
        return parsed;
    }

    private Condition parseConditionInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return parseCondition(input);
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private Expression<?> parseExpression(String input) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{Object.class});
        assertNotNull(parsed, input);
        return parsed;
    }

    private Expression<?> parseExpressionInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return parseExpression(input);
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private Statement parseEffect(String input) {
        Statement parsed = Statement.parse(input, "failed");
        assertNotNull(parsed, input);
        return parsed;
    }

    private Statement parseEffectInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return parseEffect(input);
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private ch.njol.skript.lang.SkriptEvent parseEvent(String input) {
        ch.njol.skript.lang.SkriptEvent parsed = ch.njol.skript.lang.SkriptEvent.parse(
                input,
                new SectionNode(input),
                "failed"
        );
        assertNotNull(parsed, input);
        return parsed;
    }

    private void restoreEventContext(ParserInstance parser, @Nullable String previousEventName, Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }
    }

    private static Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record CancelledHandle(boolean isCancelled) {
    }
}
