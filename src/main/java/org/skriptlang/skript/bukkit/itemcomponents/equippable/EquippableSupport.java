package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public final class EquippableSupport {

    private EquippableSupport() {
    }

    public static @Nullable EquippableWrapper getWrapper(@Nullable Object value) {
        if (value instanceof EquippableWrapper wrapper) {
            return wrapper;
        }
        if (value instanceof Equippable equippable) {
            return new EquippableWrapper(equippable);
        }
        if (value instanceof ItemStack itemStack) {
            return new EquippableWrapper(itemStack);
        }
        if (value instanceof Slot slot) {
            return new EquippableWrapper(slot);
        }
        if (value instanceof FabricItemType itemType) {
            return new EquippableWrapper(itemType);
        }
        if (value instanceof Item item) {
            return new EquippableWrapper(new ItemStack(item));
        }
        if (value instanceof String raw) {
            try {
                Item item = BuiltInRegistries.ITEM.getValue(MinecraftResourceParser.parse(raw.trim()));
                return item != null ? new EquippableWrapper(new ItemStack(item)) : null;
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    public static @Nullable Equippable getEquippable(@Nullable Object value) {
        EquippableWrapper wrapper = getWrapper(value);
        return wrapper != null ? wrapper.getComponent() : null;
    }
}
