package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;

@Name("Ping")
@Description("The player's current measured latency in milliseconds.")
@Example("send ping of player")
@Since("2.2-dev36, Fabric")
public class ExprPing extends SimplePropertyExpression<ServerPlayer, Long> {

    static {
        register(ExprPing.class, Long.class, "ping", "players");
    }

    @Override
    public Long convert(ServerPlayer player) {
        return (long) player.connection.latency();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "ping";
    }
}
