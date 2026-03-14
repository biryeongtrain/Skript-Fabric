package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.events.SpawnReason;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprSpawnReason extends SimpleExpression<SpawnReason> {

    static {
        Skript.registerExpression(ExprSpawnReason.class, SpawnReason.class, "spawn[ing] reason");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricEventCompatHandles.EntityLifecycle.class)) {
            Skript.error("The expression 'spawn reason' may only be used in a spawn event");
            return false;
        }
        return true;
    }

    @Override
    protected SpawnReason @Nullable [] get(SkriptEvent event) {
        if (event.handle() instanceof FabricEventCompatHandles.EntityLifecycle handle && handle.spawnReason() != null) {
            return new SpawnReason[]{handle.spawnReason()};
        }
        return new SpawnReason[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends SpawnReason> getReturnType() {
        return SpawnReason.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "spawn reason";
    }
}
