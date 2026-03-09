package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class LitInfinity extends SimpleLiteral<Double> {

    private static final String[] PATTERNS = {
            "positive infinity [value]",
            "infinity value",
            "value of [positive] infinity"
    };

    public static void register() {
        Skript.registerExpression(LitInfinity.class, Double.class, PATTERNS);
    }

    public LitInfinity() {
        super(Double.POSITIVE_INFINITY, false);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "infinity";
    }
}
