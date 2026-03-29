package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.conditions.CondContains;
import ch.njol.skript.conditions.CondIsWearing;
import ch.njol.skript.conditions.CondItemInHand;
import ch.njol.skript.expressions.ExprChestInventory;
import ch.njol.skript.expressions.ExprEnderChest;
import ch.njol.skript.expressions.ExprFirstEmptySlot;
import ch.njol.skript.expressions.ExprInventory;
import ch.njol.skript.expressions.ExprInventoryInfo;
import ch.njol.skript.expressions.ExprInventorySlot;
import ch.njol.skript.expressions.ExprItemsIn;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("isolated-registry")
final class InventorySyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void inventoryExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprChestInventory.class, parseExpression("chest inventory named \"Menu\" with 2 rows"));
        assertInstanceOf(ExprEnderChest.class, parseExpressionInEvent("ender chest of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprInventory.class, parseExpressionInEvent("inventory of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprInventoryInfo.class, parseExpressionInEvent("amount of rows of inventory of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprInventoryInfo.class, parseExpressionInEvent("amount of slots of inventory of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprInventorySlot.class, parseExpressionInEvent("slot 1 of inventory of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprItemsIn.class, parseExpressionInEvent("items in inventory of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprFirstEmptySlot.class, parseExpressionInEvent("first empty slot in inventory of event-player", FabricUseEntityHandle.class));
    }

    @Test
    void inventoryConditionsParseThroughBootstrap() {
        assertInstanceOf(CondContains.class, parseConditionInEvent("inventory of event-player contains diamond", FabricUseEntityHandle.class));
        assertInstanceOf(CondItemInHand.class, parseConditionInEvent("event-player is holding diamond sword", FabricUseEntityHandle.class));
        assertInstanceOf(CondIsWearing.class, parseConditionInEvent("event-player is wearing diamond helmet", FabricUseEntityHandle.class));
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

    private Condition parseConditionInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            Condition parsed = Condition.parse(input, null);
            assertNotNull(parsed, input);
            return parsed;
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private void restoreEventContext(ParserInstance parser, String previousEventName, Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }
    }
}
