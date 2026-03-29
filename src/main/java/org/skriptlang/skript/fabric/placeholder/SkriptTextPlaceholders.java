package org.skriptlang.skript.fabric.placeholder;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import com.mojang.authlib.GameProfile;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.SkriptFabric;
import org.skriptlang.skript.lang.event.CurrentSkriptEvent;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Resolves Patbox placeholders and Skript expression placeholders against the active Skript event context.
 */
public final class SkriptTextPlaceholders {

    private static final Pattern PATBOX_PLACEHOLDER =
            Pattern.compile("%[a-z0-9_.-]+:[^%\\s][^%]*%");

    private static final Pattern SKRIPT_PLACEHOLDER =
            Pattern.compile("%([^%]+)%");

    private static final Object PARSE_FAILED = new Object();

    private static final Map<String, Object> EXPRESSION_CACHE = new ConcurrentHashMap<>();

    private SkriptTextPlaceholders() {
    }

    public static String resolveString(String input, @Nullable SkriptEvent event) {
        return resolveComponent(input, event).getString();
    }

    public static Component resolveComponent(String input, @Nullable SkriptEvent event) {
        String value = input == null ? "" : input;

        if (value.contains("%")) {
            value = resolveSkriptExpressions(value, event);
        }

        if (!PATBOX_PLACEHOLDER.matcher(value).find()) {
            return SkriptFabric.byMiniMessage(value);
        }

        ServerPlaceholderContext context = createContext(event != null ? event : CurrentSkriptEvent.get());
        if (context == null) {
            return SkriptFabric.byMiniMessage(value);
        }

        return Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(
                eu.pb4.placeholders.api.node.TextNode.convert(SkriptFabric.byMiniMessage(value)), context.asParserContext());
    }

    @SuppressWarnings("unchecked")
    private static String resolveSkriptExpressions(String input, @Nullable SkriptEvent event) {
        if (event == null) {
            return input;
        }
        Matcher matcher = SKRIPT_PLACEHOLDER.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String exprText = matcher.group(1);
            if (exprText.contains(":")) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                continue;
            }
            Expression<?> parsed = cachedParse(exprText);
            if (parsed != null) {
                Object[] values = parsed.getArray(event);
                String replacement = values != null && values.length > 0
                        ? Classes.toString(values, true)
                        : "";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static @Nullable Expression<?> cachedParse(String exprText) {
        Object cached = EXPRESSION_CACHE.get(exprText);
        if (cached == PARSE_FAILED) {
            return null;
        }
        if (cached instanceof Expression<?> expression) {
            return expression;
        }
        try {
            Expression<?> parsed = new SkriptParser(exprText, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                    .parseExpression(new Class[]{Object.class});
            if (parsed != null) {
                EXPRESSION_CACHE.put(exprText, parsed);
                return parsed;
            }
        } catch (Exception ignored) {
        }
        EXPRESSION_CACHE.put(exprText, PARSE_FAILED);
        return null;
    }

    static @Nullable ServerPlaceholderContext createContext(@Nullable SkriptEvent event) {
        if (event == null) {
            return null;
        }

        ServerPlayer player = event.player();
        if (player != null) {
            return ServerPlaceholderContext.of(player);
        }

        Entity entity = event.handle() instanceof Entity resolved ? resolved : null;
        if (entity != null) {
            return ServerPlaceholderContext.of(entity);
        }

        if (event.handle() instanceof CommandSourceStack commandSource) {
            return ServerPlaceholderContext.of(commandSource);
        }

        MinecraftServer server = event.server();
        if (server == null) {
            return null;
        }
        return ServerPlaceholderContext.of(server);
    }
}
