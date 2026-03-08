package org.skriptlang.skript.fabric.placeholder;

import com.mojang.authlib.GameProfile;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.CurrentSkriptEvent;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Resolves Patbox placeholders against the active Skript event context.
 */
public final class SkriptTextPlaceholders {

    private static final Pattern PATBOX_PLACEHOLDER =
            Pattern.compile("%[a-z0-9_.-]+:[^%\\s][^%]*%");

    private SkriptTextPlaceholders() {
    }

    public static String resolveString(String input, @Nullable SkriptEvent event) {
        return resolveComponent(input, event).getString();
    }

    public static Component resolveComponent(String input, @Nullable SkriptEvent event) {
        String value = input == null ? "" : input;
        if (!PATBOX_PLACEHOLDER.matcher(value).find()) {
            return Component.literal(value);
        }

        PlaceholderContext context = createContext(event != null ? event : CurrentSkriptEvent.get());
        if (context == null) {
            return Component.literal(value);
        }

        return Placeholders.parseText(Component.literal(value), context);
    }

    static @Nullable PlaceholderContext createContext(@Nullable SkriptEvent event) {
        if (event == null) {
            return null;
        }

        ServerPlayer player = event.player();
        Entity entity = player != null ? player : event.handle() instanceof Entity resolved ? resolved : null;
        MinecraftServer server = event.server();
        if (server == null) {
            if (player != null) {
                server = player.getServer();
            } else if (entity != null) {
                server = entity.getServer();
            }
        }
        if (server == null) {
            return null;
        }

        ServerLevel level = event.level();
        if (level == null) {
            if (player != null) {
                if (player.level() instanceof ServerLevel playerLevel) {
                    level = playerLevel;
                }
            } else if (entity != null && entity.level() instanceof ServerLevel entityLevel) {
                level = entityLevel;
            }
        }

        CommandSourceStack source;
        if (event.handle() instanceof CommandSourceStack commandSource) {
            source = commandSource;
            if (level == null) {
                level = commandSource.getLevel();
            }
            if (entity == null) {
                entity = commandSource.getEntity();
            }
            if (player == null) {
                try {
                    player = commandSource.getPlayer();
                } catch (Exception ignored) {
                    player = null;
                }
            }
        } else if (player != null) {
            source = player.createCommandSourceStack();
        } else {
            source = server.createCommandSourceStack();
        }

        if (level != null) {
            source = source.withLevel(level);
        }

        GameProfile profile = player != null ? player.getGameProfile() : null;
        return new PlaceholderContext(server, source, level, player, entity, profile);
    }
}
