package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprHostname extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprHostname.class, String.class, "[the] (host|domain)[ ]name");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(ClientIntentionPacket.class)) {
            Skript.error("The hostname expression must be used in a player connect event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        String hostname = resolveHostname(event);
        return hostname == null ? null : new String[]{hostname};
    }

    static @Nullable String resolveHostname(@Nullable SkriptEvent event) {
        return event != null && event.handle() instanceof ClientIntentionPacket packet ? packet.hostName() : null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "hostname";
    }
}
