package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class PlayerClassInfo {

    private PlayerClassInfo() {
    }

    public static void register() {
        ClassInfo<ServerPlayer> info = new ClassInfo<>(ServerPlayer.class);
        info.setPropertyInfo(Property.NAME, new PlayerNameHandler());
        info.setPropertyInfo(Property.DISPLAY_NAME, new PlayerDisplayNameHandler());
        Classes.registerClassInfo(info);
    }

    public static class PlayerNameHandler implements ExpressionPropertyHandler<ServerPlayer, String> {

        @Override
        public @Nullable String convert(ServerPlayer propertyHolder) {
            return propertyHolder.getGameProfile().getName();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }

    public static class PlayerDisplayNameHandler implements ExpressionPropertyHandler<ServerPlayer, String> {

        @Override
        public @Nullable String convert(ServerPlayer propertyHolder) {
            return propertyHolder.getDisplayName().getString();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
