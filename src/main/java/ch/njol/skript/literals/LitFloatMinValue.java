package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class LitFloatMinValue extends SimpleLiteral<Float> {

    private static final String[] PATTERNS = {"[the] min[imum] float value"};

    public static void register() {
        Skript.registerExpression(LitFloatMinValue.class, Float.class, PATTERNS);
    }

    public LitFloatMinValue() {
        super(Float.MIN_VALUE, false);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "min float value";
    }
}
