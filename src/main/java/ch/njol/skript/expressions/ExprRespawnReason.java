package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRespawnReason extends SimpleExpression<String> {

    private static final @Nullable Class<?> PLAYER_RESPAWN_EVENT =
            ExpressionHandleSupport.resolveClass("ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn");

    static {
        Skript.registerExpression(ExprRespawnReason.class, String.class, "respawn[ing] reason");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (PLAYER_RESPAWN_EVENT == null || !getParser().isCurrentEvent(PLAYER_RESPAWN_EVENT)) {
            Skript.error("The expression 'respawning reason' may only be used in the respawn event");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        Object reason = ExpressionHandleSupport.invoke(event.handle(), "reason");
        return reason == null ? null : new String[]{String.valueOf(reason)};
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
        return "respawn reason";
    }
}
