package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class ItemStackClassInfo {

    private ItemStackClassInfo() {
    }

    public static void register() {
        ClassInfo<ItemStack> info = new ClassInfo<>(ItemStack.class, "itemstack");
        info.setPropertyInfo(Property.AMOUNT, new ItemStackAmountHandler());
        info.setPropertyInfo(Property.NAME, new ItemStackNameHandler());
        info.setPropertyInfo(Property.IS_EMPTY, new ItemStackIsEmptyHandler());
        Classes.registerClassInfo(info);
    }

    public static class ItemStackAmountHandler implements ExpressionPropertyHandler<ItemStack, Integer> {

        @Override
        public @Nullable Integer convert(ItemStack propertyHolder) {
            return propertyHolder.getCount();
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{Integer.class, Number.class} : null;
        }

        @Override
        public void change(ItemStack propertyHolder, Object[] delta, ChangeMode mode) {
            if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
                throw new UnsupportedOperationException("Only integer set changes are supported for item stack amount.");
            }
            propertyHolder.setCount(Math.max(0, number.intValue()));
        }

        @Override
        public Class<Integer> returnType() {
            return Integer.class;
        }
    }

    public static class ItemStackNameHandler implements ExpressionPropertyHandler<ItemStack, String> {

        @Override
        public @Nullable String convert(ItemStack propertyHolder) {
            Component customName = propertyHolder.get(DataComponents.CUSTOM_NAME);
            return customName != null ? customName.getString() : propertyHolder.getHoverName().getString();
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{String.class} : null;
        }

        @Override
        public void change(ItemStack propertyHolder, Object[] delta, ChangeMode mode) {
            if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof String name)) {
                return;
            }
            propertyHolder.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }

    public static class ItemStackIsEmptyHandler implements ConditionPropertyHandler<ItemStack> {

        @Override
        public boolean check(ItemStack propertyHolder) {
            return propertyHolder.isEmpty();
        }
    }
}
