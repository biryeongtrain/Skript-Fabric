package org.skriptlang.skript.fabric.compat;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FabricInventoryCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
    }

    @Test
    void inferredInventoriesBuildSguiWithRedirectedSlots() {
        SimpleContainer container = new SimpleContainer(2);
        container.setItem(0, new ItemStack(Items.STICK, 3));
        container.setItem(1, new ItemStack(Items.DIRT, 1));

        FabricInventory inventory = new FabricInventory(container);
        SimpleGui gui = inventory.createGui(null);

        assertEquals(MenuType.HOPPER, inventory.menuType());
        assertEquals(MenuType.HOPPER, gui.getType());
        assertEquals(5, gui.getVirtualSize());
        assertNotNull(gui.getCustomSlot(0));
        assertNotNull(gui.getCustomSlot(1));
        assertNull(gui.getCustomSlot(2));
        assertEquals(Items.STICK, gui.getCustomSlot(0).getItem().getItem());
        assertEquals(Items.DIRT, gui.getCustomSlot(1).getItem().getItem());
    }

    @Test
    void menuFactoriesMatchOpenInventoryLayouts() {
        FabricInventory chest = FabricInventory.menu("chest");
        assertEquals(MenuType.GENERIC_9x3, chest.menuType());
        assertEquals(27, chest.container().getContainerSize());
        assertEquals(27, chest.createGui(null).getVirtualSize());

        FabricInventory crafting = FabricInventory.menu("crafting");
        assertEquals(MenuType.CRAFTING, crafting.menuType());
        assertEquals(10, crafting.container().getContainerSize());
        assertEquals(10, crafting.createGui(null).getVirtualSize());

        FabricInventory anvil = FabricInventory.menu("anvil");
        assertEquals(MenuType.ANVIL, anvil.menuType());
        assertEquals(3, anvil.container().getContainerSize());
        assertEquals(3, anvil.createGui(null).getVirtualSize());

        FabricInventory hopper = FabricInventory.menu("hopper");
        assertEquals(MenuType.HOPPER, hopper.menuType());
        assertEquals(5, hopper.container().getContainerSize());
        assertEquals(5, hopper.createGui(null).getVirtualSize());

        FabricInventory dropper = FabricInventory.menu("dropper");
        assertEquals(MenuType.GENERIC_3x3, dropper.menuType());
        assertEquals(9, dropper.container().getContainerSize());
        assertEquals(9, dropper.createGui(null).getVirtualSize());

        FabricInventory dispenser = FabricInventory.menu("dispenser");
        assertEquals(MenuType.GENERIC_3x3, dispenser.menuType());
        assertEquals(9, dispenser.container().getContainerSize());
        assertEquals(9, dispenser.createGui(null).getVirtualSize());
    }
}
