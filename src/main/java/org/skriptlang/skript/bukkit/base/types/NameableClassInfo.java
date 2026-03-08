package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.placeholder.SkriptTextPlaceholders;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class NameableClassInfo {

    private NameableClassInfo() {
    }

    public static void register() {
        ClassInfo<Nameable> info = new ClassInfo<>(Nameable.class, "nameable");
        info.setPropertyInfo(Property.NAME, new NameableNameHandler());
        info.setPropertyInfo(Property.DISPLAY_NAME, new NameableDisplayNameHandler());
        Classes.registerClassInfo(info);
    }

    public static class NameableNameHandler implements ExpressionPropertyHandler<Nameable, String> {

        @Override
        public @Nullable String convert(Nameable propertyHolder) {
            return propertyHolder.getName().getString();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }

    public static class NameableDisplayNameHandler implements ExpressionPropertyHandler<Nameable, String> {

        @Override
        public @Nullable String convert(Nameable propertyHolder) {
            Component customName = propertyHolder.getCustomName();
            return customName != null ? customName.getString() : null;
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return switch (mode) {
                case SET, RESET, DELETE -> new Class[]{String.class};
                default -> null;
            };
        }

        @Override
        public void change(Nameable propertyHolder, Object[] delta, ChangeMode mode) {
            if (!(propertyHolder instanceof Entity entity)) {
                throw new UnsupportedOperationException("Display name changes currently require an Entity-backed nameable.");
            }

            if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
                entity.setCustomName(null);
                return;
            }

            String value = delta != null && delta.length > 0 ? String.valueOf(delta[0]) : null;
            entity.setCustomName(value == null ? null : SkriptTextPlaceholders.resolveComponent(value, null));
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
