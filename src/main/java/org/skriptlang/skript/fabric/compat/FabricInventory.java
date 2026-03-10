package org.skriptlang.skript.fabric.compat;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public final class FabricInventory {

    private final Container container;
    private final MenuType<?> menuType;
    private final Component title;

    public FabricInventory(Container container) {
        this(container, inferMenuType(container.getContainerSize()), Component.empty());
    }

    public FabricInventory(Container container, MenuType<?> menuType) {
        this(container, menuType, Component.empty());
    }

    public FabricInventory(Container container, MenuType<?> menuType, @Nullable Component title) {
        this.container = Objects.requireNonNull(container, "container");
        this.menuType = Objects.requireNonNull(menuType, "menuType");
        this.title = title != null ? title : Component.empty();
    }

    public Container container() {
        return container;
    }

    public MenuType<?> menuType() {
        return menuType;
    }

    public Component title() {
        return title;
    }

    public int virtualSize() {
        return GuiHelpers.getWidth(menuType) * GuiHelpers.getHeight(menuType);
    }

    public SimpleGui createGui(@Nullable ServerPlayer player) {
        SimpleGui gui = new SimpleGui(menuType, player, false);
        gui.setTitle(title);
        gui.setLockPlayerInventory(false);
        int slotCount = Math.min(container.getContainerSize(), gui.getVirtualSize());
        for (int slot = 0; slot < slotCount; slot++) {
            gui.setSlotRedirect(slot, new Slot(container, slot, 0, 0));
        }
        return gui;
    }

    public boolean open(ServerPlayer player) {
        return createGui(player).open();
    }

    public static FabricInventory chest(int rows) {
        int normalizedRows = Math.clamp(rows, 1, 6);
        return new FabricInventory(
                new SimpleContainer(normalizedRows * 9),
                switch (normalizedRows) {
                    case 1 -> MenuType.GENERIC_9x1;
                    case 2 -> MenuType.GENERIC_9x2;
                    case 3 -> MenuType.GENERIC_9x3;
                    case 4 -> MenuType.GENERIC_9x4;
                    case 5 -> MenuType.GENERIC_9x5;
                    default -> MenuType.GENERIC_9x6;
                }
        );
    }

    public static FabricInventory menu(String menuType) {
        return switch (menuType) {
            case "crafting" -> new FabricInventory(new SimpleContainer(10), MenuType.CRAFTING);
            case "chest" -> chest(3);
            case "anvil" -> new FabricInventory(new SimpleContainer(3), MenuType.ANVIL);
            case "hopper" -> new FabricInventory(new SimpleContainer(5), MenuType.HOPPER);
            case "dropper", "dispenser" -> new FabricInventory(new SimpleContainer(9), MenuType.GENERIC_3x3);
            default -> throw new IllegalArgumentException("Unsupported inventory menu type: " + menuType);
        };
    }

    private static MenuType<?> inferMenuType(int slotCount) {
        if (slotCount <= 5) {
            return MenuType.HOPPER;
        }
        if (slotCount <= 9) {
            return MenuType.GENERIC_9x1;
        }
        if (slotCount == 10) {
            return MenuType.CRAFTING;
        }
        if (slotCount <= 18) {
            return MenuType.GENERIC_9x2;
        }
        if (slotCount <= 27) {
            return MenuType.GENERIC_9x3;
        }
        if (slotCount <= 36) {
            return MenuType.GENERIC_9x4;
        }
        if (slotCount <= 45) {
            return MenuType.GENERIC_9x5;
        }
        return MenuType.GENERIC_9x6;
    }
}
