package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprMemory extends SimpleExpression<Double> {

    private static final double BYTES_IN_MEGABYTES = 1E-6;
    private static final Runtime RUNTIME = Runtime.getRuntime();

    static {
        Skript.registerExpression(ExprMemory.class, Double.class, "[the] [server] (:free|max:max[imum]|total) (memory|ram)");
    }

    private enum Type {
        FREE,
        MAXIMUM,
        TOTAL
    }

    private Type type;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (parseResult.hasTag("free")) {
            type = Type.FREE;
        } else if (parseResult.hasTag("max")) {
            type = Type.MAXIMUM;
        } else {
            type = Type.TOTAL;
        }
        return true;
    }

    @Override
    protected Double @Nullable [] get(SkriptEvent event) {
        double memory = switch (type) {
            case FREE -> RUNTIME.freeMemory();
            case MAXIMUM -> RUNTIME.maxMemory();
            case TOTAL -> RUNTIME.totalMemory();
        };
        return new Double[]{memory * BYTES_IN_MEGABYTES};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Double> getReturnType() {
        return Double.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return type.name().toLowerCase(Locale.ENGLISH) + " memory";
    }
}
