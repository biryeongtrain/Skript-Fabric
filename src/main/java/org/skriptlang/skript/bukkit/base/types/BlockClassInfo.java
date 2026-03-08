package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class BlockClassInfo {

    private BlockClassInfo() {
    }

    public static void register() {
        ClassInfo<FabricBlock> info = new ClassInfo<>(FabricBlock.class, "block");
        info.setPropertyInfo(Property.NAME, new BlockNameHandler());
        Classes.registerClassInfo(info);
    }

    public static class BlockNameHandler implements ExpressionPropertyHandler<FabricBlock, String> {

        @Override
        public @Nullable String convert(FabricBlock propertyHolder) {
            return propertyHolder.block().getName().getString();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
