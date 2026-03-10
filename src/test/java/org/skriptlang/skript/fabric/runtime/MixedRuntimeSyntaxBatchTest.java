package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.conditions.CondCancelled;
import ch.njol.skript.conditions.CondDamageCause;
import ch.njol.skript.conditions.CondIsPreferredTool;
import ch.njol.skript.conditions.CondIsSedated;
import ch.njol.skript.conditions.CondLeashWillDrop;
import ch.njol.skript.conditions.CondRespawnLocation;
import ch.njol.skript.conditions.CondScriptLoaded;
import ch.njol.skript.effects.EffCopy;
import ch.njol.skript.effects.EffExceptionDebug;
import ch.njol.skript.effects.EffSort;
import ch.njol.skript.effects.EffToggle;
import ch.njol.skript.events.EvtBeaconEffect;
import ch.njol.skript.events.EvtBeaconToggle;
import ch.njol.skript.events.EvtBlock;
import ch.njol.skript.events.EvtBookEdit;
import ch.njol.skript.events.EvtBookSign;
import ch.njol.skript.events.EvtClick;
import ch.njol.skript.events.EvtEntity;
import ch.njol.skript.events.EvtEntityTransform;
import ch.njol.skript.events.EvtExperienceSpawn;
import ch.njol.skript.events.EvtHealing;
import ch.njol.skript.events.EvtItem;
import ch.njol.skript.expressions.ExprAffectedEntities;
import ch.njol.skript.expressions.ExprConsumedItem;
import ch.njol.skript.expressions.ExprExplodedBlocks;
import ch.njol.skript.expressions.ExprHatchingNumber;
import ch.njol.skript.expressions.ExprHealAmount;
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
        assertInstanceOf(CondRespawnLocation.class, parseConditionInEvent(
                "respawn location is a bed",
                resolveEventClass("ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn")
        ));
        assertInstanceOf(CondScriptLoaded.class, parseCondition("script \"example.sk\" is loaded"));
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
        assertInstanceOf(ExprMaxDurability.class, parseExpression("maximum durability of diamond sword"));
        assertInstanceOf(ExprRawName.class, parseExpression("minecraft name of diamond sword"));
    }

    @Test
    void mixedEffectsParseThroughBootstrap() {
        assertInstanceOf(EffCopy.class, parseEffect("copy {source::*} to {target::*}"));
        assertInstanceOf(EffSort.class, parseEffect("sort {values::*}"));
        assertInstanceOf(EffExceptionDebug.class, parseEffect("cause exception"));
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
        assertInstanceOf(EvtEntityTransform.class, parseEvent("zombie transforming due to \"curing\""));
        assertInstanceOf(EvtExperienceSpawn.class, parseEvent("experience orb spawn"));
        assertInstanceOf(EvtHealing.class, parseEvent("healing of zombie by \"magic\""));
        assertInstanceOf(EvtItem.class, parseEvent("item spawn of stick"));
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
