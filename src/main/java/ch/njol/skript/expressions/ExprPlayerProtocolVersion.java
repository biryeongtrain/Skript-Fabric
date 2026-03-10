package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@Name("Player Protocol Version")
@Description("The protocol version used by the current server runtime for connected players.")
@Example("send protocol version of player")
@Since("2.6.2, Fabric")
public class ExprPlayerProtocolVersion extends SimplePropertyExpression<ServerPlayer, Integer> {

    static {
        register(ExprPlayerProtocolVersion.class, Integer.class, "protocol version", "players");
    }

    @Override
    public @Nullable Integer convert(ServerPlayer player) {
        return SharedConstants.getProtocolVersion();
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "protocol version";
    }
}
