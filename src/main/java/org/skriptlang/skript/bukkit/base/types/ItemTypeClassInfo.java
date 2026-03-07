package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.MinecraftRegistryLookup;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class ItemTypeClassInfo {

    private static final Pattern AMOUNT_PREFIX = Pattern.compile("^(\\d+)\\s+(.+)$");

    private ItemTypeClassInfo() {
    }

    public static void register() {
        ClassInfo<FabricItemType> info = new ClassInfo<>(FabricItemType.class);
        info.setParser(new ItemTypeParser());
        info.setPropertyInfo(Property.NAME, new ItemTypeNameHandler());
        info.setPropertyInfo(Property.DISPLAY_NAME, new ItemTypeNameHandler());
        info.setPropertyInfo(Property.AMOUNT, new ItemTypeAmountHandler());
        Classes.registerClassInfo(info);
    }

    private static class ItemTypeParser implements ClassInfo.Parser<FabricItemType> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable FabricItemType parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }

            String normalized = input.trim();
            int amount = 1;
            Matcher matcher = AMOUNT_PREFIX.matcher(normalized);
            if (matcher.matches()) {
                amount = Integer.parseInt(matcher.group(1));
                normalized = matcher.group(2).trim();
            }

            Item item = MinecraftRegistryLookup.lookup(normalized, ItemTypeParser::itemFromId);
            if (item == null) {
                item = itemFromAlias(normalized);
            }
            if (item == null) {
                return null;
            }
            return new FabricItemType(item, amount, null);
        }

        private static @Nullable Item itemFromId(ResourceLocation id) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            ResourceLocation key = item == null ? null : BuiltInRegistries.ITEM.getKey(item);
            return id.equals(key) ? item : null;
        }

        private static @Nullable Item itemFromAlias(String raw) {
            var lookupKeys = MinecraftRegistryLookup.candidateLookupKeys(raw);
            if (lookupKeys.isEmpty()) {
                return null;
            }
            for (Item item : BuiltInRegistries.ITEM) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                if (key == null) {
                    continue;
                }
                if (matchesAlias(lookupKeys, key.toString())
                        || matchesAlias(lookupKeys, MinecraftResourceParser.display(key))
                        || matchesAlias(lookupKeys, new ItemStack(item).getHoverName().getString())) {
                    return item;
                }
            }
            return null;
        }

        private static boolean matchesAlias(java.util.Set<String> lookupKeys, String alias) {
            String normalized = MinecraftRegistryLookup.normalizeAlias(alias);
            return !normalized.isBlank() && lookupKeys.contains(normalized);
        }
    }

    public static class ItemTypeNameHandler implements ExpressionPropertyHandler<FabricItemType, String> {

        @Override
        public @Nullable String convert(FabricItemType propertyHolder) {
            return propertyHolder.name();
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return switch (mode) {
                case SET, RESET, DELETE -> new Class[]{String.class};
                default -> null;
            };
        }

        @Override
        public void change(FabricItemType propertyHolder, Object[] delta, ChangeMode mode) {
            String value = delta != null && delta.length > 0 ? String.valueOf(delta[0]) : null;
            if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
                propertyHolder.name(null);
                return;
            }
            propertyHolder.name(value);
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }

    public static class ItemTypeAmountHandler implements ExpressionPropertyHandler<FabricItemType, Integer> {

        @Override
        public @Nullable Integer convert(FabricItemType propertyHolder) {
            return propertyHolder.amount();
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{Integer.class, Number.class} : null;
        }

        @Override
        public void change(FabricItemType propertyHolder, Object[] delta, ChangeMode mode) {
            if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
                throw new UnsupportedOperationException("Only integer set changes are supported for item type amount.");
            }
            propertyHolder.amount(number.intValue());
        }

        @Override
        public Class<Integer> returnType() {
            return Integer.class;
        }
    }
}
