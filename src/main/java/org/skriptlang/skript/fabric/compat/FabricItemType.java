package org.skriptlang.skript.fabric.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public final class FabricItemType {

    private final Item item;
    private int amount;
    private @Nullable String customName;
    private @Nullable Equippable equippable;

    public FabricItemType(Item item) {
        this(item, 1, null);
    }

    public FabricItemType(Item item, int amount, @Nullable String customName) {
        this.item = item;
        this.amount = Math.max(1, amount);
        this.customName = customName;
    }

    public Item item() {
        return item;
    }

    public int amount() {
        return amount;
    }

    public void amount(int amount) {
        this.amount = Math.max(1, amount);
    }

    public @Nullable String name() {
        return customName;
    }

    public void name(@Nullable String customName) {
        this.customName = customName;
    }

    public @Nullable Equippable equippable() {
        return equippable;
    }

    public void equippable(@Nullable Equippable equippable) {
        this.equippable = equippable;
    }

    public ItemStack toStack() {
        ItemStack stack = new ItemStack(item, amount);
        if (customName != null && !customName.isBlank()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(customName));
        }
        if (equippable != null) {
            stack.set(DataComponents.EQUIPPABLE, equippable);
        }
        return stack;
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(item)) {
            return false;
        }
        if (stack.getCount() < amount) {
            return false;
        }
        if (customName != null && !customName.isBlank()) {
            Component stackName = stack.get(DataComponents.CUSTOM_NAME);
            if (stackName == null || !customName.equals(stackName.getString())) {
                return false;
            }
        }
        if (equippable != null && !Objects.equals(equippable, stack.get(DataComponents.EQUIPPABLE))) {
            return false;
        }
        return true;
    }

    public String itemId() {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key != null ? MinecraftResourceParser.display(key) : item.toString();
    }

    @Override
    public String toString() {
        return amount == 1 ? itemId() : amount + " " + itemId();
    }
}
