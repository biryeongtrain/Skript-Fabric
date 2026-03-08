package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class EntityClassInfo {

    private EntityClassInfo() {
    }

    public static void register() {
        ClassInfo<Entity> info = new ClassInfo<>(Entity.class, "entity");
        info.setPropertyInfo(Property.NAME, new EntityNameHandler());
        Classes.registerClassInfo(info);
    }

    public static class EntityNameHandler implements ExpressionPropertyHandler<Entity, String> {

        @Override
        public @Nullable String convert(Entity propertyHolder) {
            return propertyHolder.getName().getString();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
