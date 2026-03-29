package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import ch.njol.skript.events.EvtPlantGrowth;
import ch.njol.skript.events.EvtPressurePlate;
import ch.njol.skript.events.EvtVehicleCollision;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventBlock;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventEntity;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventItem;

final class EventBridgeBindingTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        EvtBeaconEffect.register();
        EvtBeaconToggle.register();
        EvtBlock.register();
        EvtBookEdit.register();
        EvtBookSign.register();
        EvtClick.register();
        EvtEntity.register();
        EvtEntityBlockChange.register();
        EvtEntityTransform.register();
        EvtExperienceSpawn.register();
        EvtGrow.register();
        EvtHealing.register();
        EvtItem.register();
        EvtPlantGrowth.register();
        EvtPressurePlate.register();
        EvtVehicleCollision.register();
    }

    @Test
    void blockBackedCompatHandlesBindEventBlock() {
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.BeaconEffect.class));
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.BeaconToggle.class));
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.Block.class));
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.EntityBlockChange.class));
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.Grow.class));
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.PlantGrowth.class));
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.PressurePlate.class));
        assertInstanceOf(ExprEventBlock.class, parseExpressionInEvent("event-block", ch.njol.skript.events.FabricEventCompatHandles.VehicleCollision.class));
    }

    @Test
    void entityBackedCompatHandlesBindEventEntity() {
        assertInstanceOf(ExprEventEntity.class, parseExpressionInEvent("event-entity", ch.njol.skript.events.FabricEventCompatHandles.EntityLifecycle.class));
        assertInstanceOf(ExprEventEntity.class, parseExpressionInEvent("event-entity", ch.njol.skript.events.FabricEventCompatHandles.EntityBlockChange.class));
        assertInstanceOf(ExprEventEntity.class, parseExpressionInEvent("event-entity", ch.njol.skript.events.FabricEventCompatHandles.EntityTransform.class));
        assertInstanceOf(ExprEventEntity.class, parseExpressionInEvent("event-entity", ch.njol.skript.events.FabricEventCompatHandles.Healing.class));
        assertInstanceOf(ExprEventEntity.class, parseExpressionInEvent("event-entity", ch.njol.skript.events.FabricEventCompatHandles.VehicleCollision.class));
    }

    @Test
    void itemBackedCompatHandlesBindEventItem() {
        assertInstanceOf(ExprEventItem.class, parseExpressionInEvent("event-item", ch.njol.skript.events.FabricEventCompatHandles.BookEdit.class));
        assertInstanceOf(ExprEventItem.class, parseExpressionInEvent("event-item", ch.njol.skript.events.FabricEventCompatHandles.Click.class));
        assertInstanceOf(ExprEventItem.class, parseExpressionInEvent("event-item", ch.njol.skript.events.FabricEventCompatHandles.Item.class));
    }

    private Expression<?> parseExpressionInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("compat", eventClasses);
            Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                    .parseExpression(new Class[]{Object.class});
            assertNotNull(parsed, input);
            return parsed;
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }
}
