package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprCharacters extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprCharacters.class, String.class,
                "[(all [[of] the]|the)] [:alphanumeric] characters (between|from) %string% (and|to) %string%");
    }

    private Expression<String> start;
    private Expression<String> end;
    private boolean alphanumericOnly;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        start = (Expression<String>) exprs[0];
        end = (Expression<String>) exprs[1];
        alphanumericOnly = parseResult.hasTag("alphanumeric");
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        String startValue = start.getSingle(event);
        String endValue = end.getSingle(event);
        if (startValue == null || endValue == null || startValue.isEmpty() || endValue.isEmpty()) {
            return new String[0];
        }

        char startChar = startValue.charAt(0);
        char endChar = endValue.charAt(0);
        int step = startChar <= endChar ? 1 : -1;

        int count = Math.abs(endChar - startChar) + 1;
        String[] characters = new String[count];
        int index = 0;
        for (int current = startChar; ; current += step) {
            char character = (char) current;
            if (!alphanumericOnly || Character.isLetterOrDigit(character)) {
                characters[index++] = String.valueOf(character);
            }
            if (current == endChar) {
                break;
            }
        }

        if (index == characters.length) {
            return characters;
        }

        String[] compact = new String[index];
        System.arraycopy(characters, 0, compact, 0, index);
        return compact;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Expression<? extends String> simplify() {
        if (start instanceof Literal<?> && end instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "all the " + (alphanumericOnly ? "alphanumeric " : "") + "characters between "
                + start.toString(event, debug) + " and " + end.toString(event, debug);
    }
}
