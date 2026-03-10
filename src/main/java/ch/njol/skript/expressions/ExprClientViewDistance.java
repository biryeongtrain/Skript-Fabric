package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@Name("View Distance of Client")
@Description("The view distance requested by the player's client.")
@Example("set {_view} to client view distance of player")
@Since("2.5, Fabric")
public class ExprClientViewDistance extends SimplePropertyExpression<ServerPlayer, Long> {

    static {
        register(ExprClientViewDistance.class, Long.class, "client view distance[s]", "players");
    }

    @Override
    public @Nullable Long convert(ServerPlayer player) {
        return (long) player.clientInformation().viewDistance();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "client view distance";
    }
}
