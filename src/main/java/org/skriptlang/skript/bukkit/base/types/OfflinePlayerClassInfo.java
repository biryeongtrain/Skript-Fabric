package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public final class OfflinePlayerClassInfo {

    private static final Pattern PLAYER_NAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    private OfflinePlayerClassInfo() {
    }

    public static void register() {
        ClassInfo<GameProfile> info = new ClassInfo<>(GameProfile.class);
        info.setParser(new OfflinePlayerParser());
        info.setPropertyInfo(Property.NAME, new OfflinePlayerNameHandler());
        Classes.registerClassInfo(info);
    }

    private static class OfflinePlayerParser implements ClassInfo.Parser<GameProfile> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable GameProfile parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String normalized = input.trim();
            try {
                UUID uuid = UUID.fromString(normalized);
                return new GameProfile(uuid, normalized);
            } catch (IllegalArgumentException ignored) {
            }

            if (!PLAYER_NAME.matcher(normalized).matches()) {
                return null;
            }

            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + normalized).getBytes(StandardCharsets.UTF_8));
            return new GameProfile(uuid, normalized);
        }
    }

    public static class OfflinePlayerNameHandler implements ExpressionPropertyHandler<GameProfile, String> {

        @Override
        public @Nullable String convert(GameProfile propertyHolder) {
            return propertyHolder.getName();
        }

        @Override
        public Class<String> returnType() {
            return String.class;
        }
    }
}
