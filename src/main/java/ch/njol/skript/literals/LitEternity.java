package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class LitEternity extends SimpleLiteral<Timespan> {

    private static final String[] PATTERNS = {
            "[an] eternity",
            "forever",
            "[an] (indefinite|infinite) (duration|timespan)"
    };

    public static void register() {
        Skript.registerExpression(LitEternity.class, Timespan.class, PATTERNS);
    }

    public LitEternity() {
        super(Timespan.infinite(), false);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "an eternity";
    }
}
