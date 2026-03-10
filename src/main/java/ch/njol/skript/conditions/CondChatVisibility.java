package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Locale;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondChatVisibility extends Condition {

    static {
        Skript.registerCondition(
                CondChatVisibility.class,
                "%player% can see all messages [in chat]",
                "%player% can only see (commands|system messages) [in chat]",
                "%player% can('t|[ ]not) see any (command[s]|message[s]) [in chat]",
                "%player% can('t|[ ]not) see all messages [in chat]",
                "%player% can('t|[ ]not) only see (commands|system messages) [in chat]"
        );
    }

    private int pattern;
    private Expression<ServerPlayer> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        player = (Expression<ServerPlayer>) expressions[0];
        setNegated(matchedPattern > 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        ServerPlayer current = player.getSingle(event);
        if (current == null) {
            return false;
        }
        Object visibility = ConditionRuntimeSupport.invokeCompatible(current, "getChatVisibility", "chatVisibility");
        if (visibility == null) {
            return false;
        }
        String value = visibility.toString().toLowerCase(Locale.ENGLISH);
        return switch (pattern) {
            case 0 -> value.contains("full");
            case 1 -> value.contains("system");
            case 2 -> value.contains("hidden");
            case 3 -> !value.contains("full");
            case 4 -> !value.contains("system");
            default -> false;
        };
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (pattern) {
            case 0 -> player.toString(event, debug) + " can see all messages";
            case 1 -> player.toString(event, debug) + " can only see commands";
            case 2 -> player.toString(event, debug) + " can't see any messages";
            case 3 -> player.toString(event, debug) + " can't see all messages";
            case 4 -> player.toString(event, debug) + " can't only see commands";
            default -> player.toString(event, debug) + " chat visibility";
        };
    }
}
