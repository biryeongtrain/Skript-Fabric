package org.skriptlang.skript.bukkit.input.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Input;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsPressingKey extends Condition {

    private Expression<ServerPlayer> players;
    private Expression<InputKey> inputKeys;
    private boolean past;
    private boolean delayed;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2
                || !expressions[0].canReturn(ServerPlayer.class)
                || !expressions[1].canReturn(InputKey.class)) {
            return false;
        }
        players = (Expression<ServerPlayer>) expressions[0];
        inputKeys = (Expression<InputKey>) expressions[1];
        past = matchedPattern > 1;
        delayed = !isDelayed.isFalse();
        if (past) {
            if (!getParser().isCurrentEvent(FabricPlayerInputEventHandle.class)) {
                Skript.warning("Checking the past state of a player's input outside the 'player input' event has no effect.");
            } else if (delayed) {
                Skript.warning("Checking the past state of a player's input after the event has passed has no effect.");
            }
        }
        setNegated(matchedPattern == 1 || matchedPattern == 3);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        InputKey[] requestedKeys = inputKeys.getAll(event);
        boolean and = inputKeys.getAnd();
        return players.check(event, player -> checkPlayerInput(event, player, requestedKeys, and), isNegated());
    }

    private boolean checkPlayerInput(SkriptEvent event, ServerPlayer player, InputKey[] requestedKeys, boolean and) {
        if (requestedKeys.length == 0) {
            return false;
        }
        Input input = resolveInput(event, player);
        for (InputKey inputKey : requestedKeys) {
            boolean pressed = inputKey != null && inputKey.check(input);
            if (and && !pressed) {
                return false;
            }
            if (!and && pressed) {
                return true;
            }
        }
        return and;
    }

    private Input resolveInput(SkriptEvent event, ServerPlayer player) {
        if (!delayed
                && event.handle() instanceof FabricPlayerInputEventHandle inputEvent
                && inputEvent.player() == player) {
            return normalizeInput(past ? inputEvent.previousInput() : inputEvent.currentInput());
        }
        return normalizeInput(player.getLastClientInput());
    }

    private Input normalizeInput(@Nullable Input input) {
        return input != null ? input : Input.EMPTY;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder(players.toString(event, debug)).append(' ');
        builder.append(past ? (players.isSingle() ? "was" : "were") : (players.isSingle() ? "is" : "are"));
        if (isNegated()) {
            builder.append(" not");
        }
        builder.append(" pressing ").append(inputKeys.toString(event, debug));
        return builder.toString();
    }
}
