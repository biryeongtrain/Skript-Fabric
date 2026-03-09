package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class LitNaN extends SimpleLiteral<Double> {

    private static final String[] PATTERNS = {"NaN [value]", "value of NaN"};

    public static void register() {
        Skript.registerExpression(LitNaN.class, Double.class, PATTERNS);
    }

    public LitNaN() {
        super(Double.NaN, false);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "NaN";
    }
}
