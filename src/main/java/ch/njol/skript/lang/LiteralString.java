package ch.njol.skript.lang;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * A literal string expression.
 */
public class LiteralString implements Literal<String> {

    protected final String original;

    protected LiteralString(String input) {
        this.original = input;
    }

    public static LiteralString of(String input) {
        return new LiteralString(input);
    }

    @Override
    public String[] getArray(SkriptEvent event) {
        return new String[]{original};
    }

    @Override
    public @Nullable String getSingle(SkriptEvent event) {
        return original;
    }

    @Override
    public String[] getAll(SkriptEvent event) {
        return new String[]{original};
    }

    @Override
    public Optional<String> getOptionalSingle(SkriptEvent event) {
        return Optional.of(original);
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return '"' + original + '"';
    }
}
