package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.events.FabricPlayerEventHandles;
import ch.njol.skript.events.TeleportCause;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTeleportCause extends SimpleExpression<TeleportCause> {

    static {
        Skript.registerExpression(ExprTeleportCause.class, TeleportCause.class, "teleport (cause|reason|type)");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricPlayerEventHandles.Teleport.class)) {
            Skript.error("The expression 'teleport cause' may only be used in a teleport event");
            return false;
        }
        return true;
    }

    @Override
    protected TeleportCause @Nullable [] get(SkriptEvent event) {
        if (event.handle() instanceof FabricPlayerEventHandles.Teleport handle && handle.cause() != null) {
            return new TeleportCause[]{handle.cause()};
        }
        return new TeleportCause[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends TeleportCause> getReturnType() {
        return TeleportCause.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "teleport cause";
    }
}
