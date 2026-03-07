package org.skriptlang.skript.bukkit.loottables;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class LootTableClassInfo {

    private LootTableClassInfo() {
    }

    public static void register() {
        ClassInfo<LootTable> info = new ClassInfo<>(LootTable.class);
        info.setParser(new Parser());
        info.setPropertyInfo(Property.NAME, new NameHandler());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<LootTable> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable LootTable parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String normalized = input.trim();
            if (!normalized.contains(":") && !normalized.contains("/")) {
                return null;
            }
            try {
                return LootTable.fromId(MinecraftResourceParser.parse(normalized));
            } catch (RuntimeException ignored) {
                return null;
            }
        }
    }

    public static final class NameHandler implements ExpressionPropertyHandler<LootTable, String> {

        @Override
        public @Nullable String convert(LootTable propertyHolder) {
            return propertyHolder.toString();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
