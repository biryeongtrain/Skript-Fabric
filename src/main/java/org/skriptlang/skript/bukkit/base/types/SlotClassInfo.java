package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class SlotClassInfo {

    private SlotClassInfo() {
    }

    public static void register() {
        ClassInfo<Slot> info = new ClassInfo<>(Slot.class);
        info.setPropertyInfo(Property.NAME, new SlotNameHandler());
        info.setPropertyInfo(Property.DISPLAY_NAME, new SlotNameHandler());
        info.setPropertyInfo(Property.AMOUNT, new SlotAmountHandler());
        info.setPropertyInfo(Property.IS_EMPTY, new SlotEmptyHandler());
        Classes.registerClassInfo(info);
    }

    public static class SlotEmptyHandler implements ConditionPropertyHandler<Slot> {

        @Override
        public boolean check(Slot propertyHolder) {
            return !propertyHolder.hasItem() || propertyHolder.getItem().isEmpty();
        }
    }

    public static class SlotNameHandler implements ExpressionPropertyHandler<Slot, String> {

        @Override
        public @Nullable String convert(Slot propertyHolder) {
            ItemStack stack = propertyHolder.getItem();
            if (stack.isEmpty() || stack.getCustomName() == null) {
                return null;
            }
            return stack.getCustomName().getString();
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return switch (mode) {
                case SET, RESET, DELETE -> new Class[]{String.class};
                default -> null;
            };
        }

        @Override
        public void change(Slot propertyHolder, Object[] delta, ChangeMode mode) {
            ItemStack stack = propertyHolder.getItem();
            if (stack.isEmpty()) {
                return;
            }

            ItemStack updated = stack.copy();
            if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
                updated.remove(DataComponents.CUSTOM_NAME);
            } else {
                String value = delta != null && delta.length > 0 ? String.valueOf(delta[0]) : null;
                if (value == null || value.isBlank()) {
                    updated.remove(DataComponents.CUSTOM_NAME);
                } else {
                    updated.set(DataComponents.CUSTOM_NAME, Component.literal(value));
                }
            }
            propertyHolder.set(updated);
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }

    public static class SlotAmountHandler implements ExpressionPropertyHandler<Slot, Integer> {

        @Override
        public @Nullable Integer convert(Slot propertyHolder) {
            return propertyHolder.getItem().getCount();
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{Integer.class, Number.class} : null;
        }

        @Override
        public void change(Slot propertyHolder, Object[] delta, ChangeMode mode) {
            if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
                throw new UnsupportedOperationException("Only integer set changes are supported for slot amount.");
            }

            int amount = Math.max(0, number.intValue());
            if (amount == 0) {
                propertyHolder.set(ItemStack.EMPTY);
                return;
            }

            ItemStack stack = propertyHolder.getItem();
            if (stack.isEmpty()) {
                return;
            }
            ItemStack updated = stack.copyWithCount(amount);
            propertyHolder.set(updated);
        }

        @Override
        public Class<Integer> returnType() {
            return Integer.class;
        }
    }
}
