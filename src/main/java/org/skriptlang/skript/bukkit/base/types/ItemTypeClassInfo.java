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
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
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

            normalized = normalizeItemId(normalized);

            ResourceLocation itemId;
            try {
                itemId = normalized.indexOf(':') >= 0
                        ? ResourceLocation.parse(normalized)
                        : ResourceLocation.withDefaultNamespace(normalized);
            } catch (RuntimeException ignored) {
                return null;
            }
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            if (item == null || item == Items.AIR && !"minecraft:air".equals(itemId.toString())) {
                return null;
            }
            return new FabricItemType(item, amount, null);
        }

        private String normalizeItemId(String input) {
            String normalized = input;
            while (normalized.length() >= 2) {
                if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                        || (normalized.startsWith("'") && normalized.endsWith("'"))) {
                    normalized = normalized.substring(1, normalized.length() - 1).trim();
                    continue;
                }
                if ((normalized.startsWith("\\\"") && normalized.endsWith("\\\""))
                        || (normalized.startsWith("\\'") && normalized.endsWith("\\'"))) {
                    normalized = normalized.substring(2, normalized.length() - 2).trim();
                    continue;
                }
                break;
            }
            return normalized
                    .replace("\\\"", "")
                    .replace("\\'", "")
                    .replaceAll("\\s+", "");
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
