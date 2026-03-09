package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRandomUUID extends SimpleExpression<UUID> {

    static {
        Skript.registerExpression(ExprRandomUUID.class, UUID.class, "[a] random uuid");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected UUID @Nullable [] get(SkriptEvent event) {
        return new UUID[]{UUID.randomUUID()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends UUID> getReturnType() {
        return UUID.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "random uuid";
    }
}
