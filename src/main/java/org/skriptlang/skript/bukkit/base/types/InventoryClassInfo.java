package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;

public final class InventoryClassInfo {

    private InventoryClassInfo() {
    }

    public static void register() {
        ClassInfo<FabricInventory> info = new ClassInfo<>(FabricInventory.class, "inventory");
        info.setPropertyInfo(Property.CONTAINS, new InventoryContainsHandler());
        info.setPropertyInfo(Property.IS_EMPTY, new InventoryIsEmptyHandler());
        Classes.registerClassInfo(info);
    }

    public static class InventoryContainsHandler implements ContainsHandler<FabricInventory, ItemStack> {

        @Override
        public boolean contains(FabricInventory container, ItemStack element) {
            int requiredAmount = Math.max(1, element.getCount());
            for (int slot = 0; slot < container.container().getContainerSize(); slot++) {
                ItemStack stack = container.container().getItem(slot);
                if (!stack.isEmpty() && stack.getItem() == element.getItem() && stack.getCount() >= requiredAmount) {
                    return true;
                }
            }
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends ItemStack>[] elementTypes() {
            return new Class[]{ItemStack.class};
        }
    }

    public static class InventoryIsEmptyHandler implements ConditionPropertyHandler<FabricInventory> {

        @Override
        public boolean check(FabricInventory inventory) {
            for (int slot = 0; slot < inventory.container().getContainerSize(); slot++) {
                if (!inventory.container().getItem(slot).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
}
