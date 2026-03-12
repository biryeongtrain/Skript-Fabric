package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTPS extends SimpleExpression<Number> {

    static final double MAX_TPS = 20.0D;
    private int index;
    private String expr = "tps";

    static {
        Skript.registerExpression(ExprTPS.class, Number.class,
                "tps from [the] last ([1] minute|1[ ]m[inute])",
                "tps from [the] last 5[ ]m[inutes]",
                "tps from [the] last 15[ ]m[inutes]",
                "[the] tps");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        expr = parseResult.expr;
        index = matchedPattern;
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        MinecraftServer server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null) {
            return new Number[0];
        }
        double value = resolveTps(server);
        if (index == 3) {
            return new Number[]{value, value, value};
        }
        return new Number[]{value};
    }

    static double resolveTps(MinecraftServer server) {
        return resolveTpsFromAverageTickTimeNanos(server.getAverageTickTimeNanos());
    }

    static double resolveTpsFromAverageTickTimeNanos(long averageTickTimeNanos) {
        if (averageTickTimeNanos <= 0L) {
            return MAX_TPS;
        }
        double tps = 1_000_000_000D / averageTickTimeNanos;
        return Math.max(0.0D, Math.min(MAX_TPS, tps));
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public boolean isSingle() {
        return index != 3;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return expr;
    }
}
