package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class WorldClassInfo {

    private WorldClassInfo() {
    }

    public static void register() {
        ClassInfo<ServerLevel> info = new ClassInfo<>(ServerLevel.class);
        info.setPropertyInfo(Property.NAME, new WorldNameHandler());
        Classes.registerClassInfo(info);
    }

    public static class WorldNameHandler implements ExpressionPropertyHandler<ServerLevel, String> {

        @Override
        public @Nullable String convert(ServerLevel propertyHolder) {
            return String.valueOf(propertyHolder.dimension());
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
