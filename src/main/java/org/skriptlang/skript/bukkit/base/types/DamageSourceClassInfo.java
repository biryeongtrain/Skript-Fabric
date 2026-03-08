package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class DamageSourceClassInfo {

    private DamageSourceClassInfo() {
    }

    public static void register() {
        ClassInfo<DamageSource> info = new ClassInfo<>(DamageSource.class, "damagesource");
        info.setPropertyInfo(Property.NAME, new DamageSourceNameHandler());
        Classes.registerClassInfo(info);
    }

    public static class DamageSourceNameHandler implements ExpressionPropertyHandler<DamageSource, String> {

        @Override
        public @Nullable String convert(DamageSource propertyHolder) {
            return propertyHolder.getMsgId();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
