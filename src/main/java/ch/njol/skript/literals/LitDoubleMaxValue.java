package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class LitDoubleMaxValue extends SimpleLiteral<Double> {

    private static final String[] PATTERNS = {"[the] max[imum] double value"};

    public static void register() {
        Skript.registerExpression(LitDoubleMaxValue.class, Double.class, PATTERNS);
    }

    public LitDoubleMaxValue() {
        super(Double.MAX_VALUE, false);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "max double value";
    }
}
