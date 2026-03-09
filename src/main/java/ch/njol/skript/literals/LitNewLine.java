package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class LitNewLine extends SimpleLiteral<String> {

    private static final String[] PATTERNS = {"nl", "new[ ]line", "line[ ]break"};

    public static void register() {
        Skript.registerExpression(LitNewLine.class, String.class, PATTERNS);
    }

    public LitNewLine() {
        super("\n", false);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "new line";
    }
}
