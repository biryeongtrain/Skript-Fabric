package org.skriptlang.skript.bukkit.input.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Input;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprCurrentInputKeys extends SimpleExpression<InputKey> {

    private Expression<ServerPlayer> players;
    private boolean delayed;
    private int time;

    @Override
    protected InputKey @Nullable [] get(SkriptEvent event) {
        List<InputKey> keys = new ArrayList<>();
        FabricPlayerInputEventHandle inputEvent = event.handle() instanceof FabricPlayerInputEventHandle handle ? handle : null;
        for (ServerPlayer player : players.getAll(event)) {
            Input input;
            if (!delayed && inputEvent != null && inputEvent.player() == player) {
                input = time == 1 ? inputEvent.previousInput() : inputEvent.currentInput();
            } else {
                input = player.getLastClientInput();
            }
            for (InputKey inputKey : InputKey.fromInput(input)) {
                keys.add(inputKey);
            }
        }
        return keys.toArray(InputKey[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends InputKey> getReturnType() {
        return InputKey.class;
    }

    @Override
    public boolean setTime(int time) {
        if (time == 0) {
            this.time = 0;
            return true;
        }
        if (time == 1 && getParser().isCurrentEvent(FabricPlayerInputEventHandle.class)) {
            this.time = 1;
            return true;
        }
        return false;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(ServerPlayer.class)) {
            return false;
        }
        players = (Expression<ServerPlayer>) expressions[0];
        delayed = !isDelayed.isFalse();
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (time == 1 ? "past " : "") + "current input keys of " + players.toString(event, debug);
    }
}
