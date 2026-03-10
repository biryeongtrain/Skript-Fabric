package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.PropertyExpression;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprSeed extends PropertyExpression<ServerLevel, Long> {

    static {
        register(ExprSeed.class, Long.class, "seed[s]", "worlds");
    }

    @Override
    protected Long[] get(SkriptEvent event, ServerLevel[] source) {
        return get(source, ServerLevel::getSeed);
    }

    @Override
    public Class<Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the seed of " + getExpr().toString(event, debug);
    }
}
