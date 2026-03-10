package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ExpressionInventoryCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void customChestInventoryUsesRowsAndTitle() {
        ExprChestInventory inventoryExpression = new ExprChestInventory();
        inventoryExpression.init(new Expression[]{
                new SimpleLiteral<>("Menu", false),
                new SimpleLiteral<>(2, false)
        }, 0, Kleenean.FALSE, parseResult(""));

        FabricInventory inventory = inventoryExpression.getSingle(SkriptEvent.EMPTY);
        assertEquals(MenuType.GENERIC_9x2, inventory.menuType());
        assertEquals(18, inventory.container().getContainerSize());
        assertEquals("Menu", inventory.title().getString());
    }

    @Test
    void inventoryExpressionsOperateOnCompatContainers() {
        SimpleContainer container = new SimpleContainer(4);
        container.setItem(0, new ItemStack(Items.STICK, 3));
        container.setItem(1, new ItemStack(Items.DIRT, 1));
        container.setItem(3, new ItemStack(Items.STICK, 1));
        FabricInventory inventory = new FabricInventory(container, MenuType.GENERIC_9x1, Component.empty(), "holder");

        ExprInventory inventoryExpression = new ExprInventory();
        inventoryExpression.init(new Expression[]{new SimpleLiteral<>(inventory, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertSame(inventory, inventoryExpression.getSingle(SkriptEvent.EMPTY));

        ExprInventorySlot slotExpression = new ExprInventorySlot();
        slotExpression.init(new Expression[]{
                new SimpleLiteral<>(1, false),
                new SimpleLiteral<>(inventory, false)
        }, 0, Kleenean.FALSE, parseResult(""));
        Slot slot = slotExpression.getSingle(SkriptEvent.EMPTY);
        assertEquals(Items.DIRT, slot.getItem().getItem());

        ExprItemsIn allItems = new ExprItemsIn();
        allItems.init(new Expression[]{new SimpleLiteral<>(inventory, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(3, allItems.getArray(SkriptEvent.EMPTY).length);

        ExprItemsIn matchingItems = new ExprItemsIn();
        matchingItems.init(new Expression[]{
                new SimpleLiteral<>(new FabricItemType(Items.STICK), false),
                new SimpleLiteral<>(inventory, false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertEquals(2, matchingItems.getArray(SkriptEvent.EMPTY).length);

        ExprFirstEmptySlot firstEmpty = new ExprFirstEmptySlot();
        firstEmpty.init(new Expression[]{new SimpleLiteral<>(inventory, false)}, 0, Kleenean.FALSE, parseResult(""));
        Slot emptySlot = firstEmpty.getSingle(SkriptEvent.EMPTY);
        emptySlot.set(new ItemStack(Items.STONE, 1));
        assertEquals(Items.STONE, container.getItem(2).getItem());

        ExprInventoryInfo rows = new ExprInventoryInfo();
        SkriptParser.ParseResult rowsParse = parseResult("");
        rowsParse.mark = 2;
        rows.init(new Expression[]{new SimpleLiteral<>(inventory, false)}, 0, Kleenean.FALSE, rowsParse);
        assertEquals(1, rows.getSingle(SkriptEvent.EMPTY));

        ExprInventoryInfo slots = new ExprInventoryInfo();
        SkriptParser.ParseResult slotsParse = parseResult("");
        slotsParse.mark = 3;
        slots.init(new Expression[]{new SimpleLiteral<>(inventory, false)}, 0, Kleenean.FALSE, slotsParse);
        assertEquals(4, slots.getSingle(SkriptEvent.EMPTY));

        ExprInventoryInfo holder = new ExprInventoryInfo();
        SkriptParser.ParseResult holderParse = parseResult("");
        holderParse.mark = 1;
        holder.init(new Expression[]{new SimpleLiteral<>(inventory, false)}, 0, Kleenean.FALSE, holderParse);
        assertEquals("holder", holder.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void importedInventoryExpressionsInstantiate() {
        assertDoesNotThrow(ExprEnderChest::new);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
