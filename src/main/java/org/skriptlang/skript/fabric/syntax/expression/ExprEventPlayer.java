package org.skriptlang.skript.fabric.syntax.expression;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventPlayer extends SimpleExpression<ServerPlayer> {

    @Override
    protected ServerPlayer @Nullable [] get(SkriptEvent event) {
        if (event.player() == null) {
            return null;
        }
        return new ServerPlayer[]{event.player()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ServerPlayer> getReturnType() {
        return ServerPlayer.class;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return expressions.length == 0;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-player";
    }
}
