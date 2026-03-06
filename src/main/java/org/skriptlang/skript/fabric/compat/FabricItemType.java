package org.skriptlang.skript.fabric.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FabricItemType {

    private final Item item;
    private int amount;
    private @Nullable String customName;

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

    public ItemStack toStack() {
        ItemStack stack = new ItemStack(item, amount);
        if (customName != null && !customName.isBlank()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(customName));
        }
        return stack;
    }

    public String itemId() {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key != null ? key.toString() : item.toString();
    }

    @Override
    public String toString() {
        return amount == 1 ? itemId() : amount + " " + itemId();
    }
}
