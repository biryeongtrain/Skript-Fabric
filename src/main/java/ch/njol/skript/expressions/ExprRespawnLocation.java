package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRespawnLocation extends SimpleExpression<FabricLocation> {

    private static final @Nullable Class<?> PLAYER_RESPAWN_EVENT =
            ExpressionHandleSupport.resolveClass("ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn");

    static {
        Skript.registerExpression(ExprRespawnLocation.class, FabricLocation.class, "[the] respawn location");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (PLAYER_RESPAWN_EVENT == null || !getParser().isCurrentEvent(PLAYER_RESPAWN_EVENT)) {
            Skript.error("The expression 'respawn location' may only be used in the respawn event");
            return false;
        }
        return true;
    }

    @Override
    protected FabricLocation @Nullable [] get(SkriptEvent event) {
        Object location = ExpressionHandleSupport.invoke(event.handle(), "respawnLocation");
        return location instanceof FabricLocation fabricLocation ? new FabricLocation[]{fabricLocation} : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{FabricLocation.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null) {
            return;
        }
        ExpressionHandleSupport.set(event.handle(), "setRespawnLocation", delta[0]);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the respawn location";
    }
}
