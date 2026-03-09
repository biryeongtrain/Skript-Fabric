package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprSubstring extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprSubstring.class, String.class,
                "[the] (part|sub[ ](text|string)) of %strings% (between|from) (ind(ex|ices)|character[s]|) %number% (and|to) (index|character|) %number%",
                "[the] (1¦first|2¦last) [%-number%] character[s] of %strings%",
                "[the] %number% (1¦first|2¦last) characters of %strings%",
                "[the] character[s] at [(index|position|indexes|indices|positions)] %numbers% (in|of) %strings%");
    }

    private Expression<String> string;
    private @Nullable Expression<Number> start;
    private @Nullable Expression<Number> end;
    private boolean usedSubstring;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        usedSubstring = matchedPattern == 0;
        string = (Expression<String>) exprs[usedSubstring ? 0 : 1];
        start = (Expression<Number>) (usedSubstring
                ? exprs[1]
                : parseResult.mark == 1 ? null : exprs[0] == null ? new SimpleLiteral<>(1, false) : exprs[0]);
        end = (Expression<Number>) (usedSubstring
                ? exprs[2]
                : parseResult.mark == 2 ? null : exprs[0] == null ? new SimpleLiteral<>(1, false) : exprs[0]);
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        List<String> parts = new ArrayList<>();
        for (String value : string.getArray(event)) {
            if (start != null && !start.isSingle()) {
                for (Number index : start.getArray(event)) {
                    int position = index.intValue();
                    if (position >= 1 && position <= value.length()) {
                        parts.add(value.substring(position - 1, position));
                    }
                }
                continue;
            }

            Number from = start != null ? start.getSingle(event) : 1;
            Number to = end != null ? end.getSingle(event) : value.length();
            if (from == null || to == null) {
                continue;
            }
            if (end == null) {
                from = value.length() - from.intValue() + 1;
            }
            int startIndex = Math.max(from.intValue() - 1, 0);
            int endIndex = Math.min(to.intValue(), value.length());
            if (startIndex < endIndex) {
                parts.add(value.substring(startIndex, endIndex));
            }
        }
        return parts.toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return string.isSingle() && (start == null || start.isSingle());
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Expression<? extends String> simplify() {
        if (string instanceof Literal<String>
                && (start == null || start instanceof Literal<Number>)
                && (end == null || end instanceof Literal<Number>)) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (start == null) {
            return "the first " + end.toString(event, debug) + " characters of " + string.toString(event, debug);
        }
        if (end == null) {
            return "the last " + start.toString(event, debug) + " characters of " + string.toString(event, debug);
        }
        if (usedSubstring) {
            return "the substring of " + string.toString(event, debug) + " from index "
                    + start.toString(event, debug) + " to " + end.toString(event, debug);
        }
        return "the character at " + (start.isSingle() ? "index " : "indexes ")
                + start.toString(event, debug) + " in " + string.toString(event, debug);
    }
}
