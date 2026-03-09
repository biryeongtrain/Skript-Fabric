package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprJoinSplit extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprJoinSplit.class, String.class,
                "(concat[enate]|join) %strings% [(with|using|by) [[the] delimiter] %-string%]",
                "split %string% (at|using|by) [[the] delimiter] %string% [case:with case sensitivity] [trailing:without [the] trailing [empty] (string|text)]",
                "%string% split (at|using|by) [[the] delimiter] %string% [case:with case sensitivity] [trailing:without [the] trailing [empty] (string|text)]",
                "regex split %string% (at|using|by) [[the] delimiter] %string% [trailing:without [the] trailing [empty] (string|text)]",
                "regex %string% split (at|using|by) [[the] delimiter] %string% [trailing:without [the] trailing [empty] (string|text)]");
    }

    private boolean join;
    private boolean regex;
    private boolean caseSensitivity;
    private boolean removeTrailing;

    private Expression<String> strings;
    private @Nullable Expression<String> delimiter;
    private @Nullable Pattern pattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        join = matchedPattern == 0;
        regex = matchedPattern >= 3;
        caseSensitivity = parseResult.hasTag("case");
        removeTrailing = parseResult.hasTag("trailing");
        strings = (Expression<String>) exprs[0];
        delimiter = (Expression<String>) exprs[1];
        if (!join && delimiter instanceof Literal<String> literal) {
            String patternText = literal.getSingle(SkriptEvent.EMPTY);
            try {
                pattern = compilePattern(patternText);
            } catch (PatternSyntaxException e) {
                Skript.error("'" + patternText + "' is not a valid regular expression");
                return false;
            }
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        String[] values = strings.getArray(event);
        String splitDelimiter = delimiter != null ? delimiter.getSingle(event) : "";

        if (values.length == 0 || splitDelimiter == null) {
            return new String[0];
        }

        if (join) {
            return new String[]{String.join(splitDelimiter, values)};
        }

        try {
            Pattern compiledPattern = pattern != null ? pattern : compilePattern(splitDelimiter);
            return compiledPattern.split(values[0], removeTrailing ? 0 : -1);
        } catch (PatternSyntaxException e) {
            return new String[0];
        }
    }

    @Override
    public boolean isSingle() {
        return join;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Expression<? extends String> simplify() {
        if (strings instanceof Literal<String> && (delimiter == null || delimiter instanceof Literal<String>)) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        if (join) {
            builder.append("join", strings);
            if (delimiter != null) {
                builder.append("with", delimiter);
            }
            return builder.toString();
        }

        if (regex) {
            builder.append("regex");
        }
        builder.append("split", strings, "at", delimiter);
        if (removeTrailing) {
            builder.append("without trailing text");
        }
        if (!regex) {
            builder.append("(case sensitive:", caseSensitivity + ")");
        }
        return builder.toString();
    }

    private Pattern compilePattern(String delimiterValue) {
        if (regex) {
            return Pattern.compile(delimiterValue);
        }
        return Pattern.compile(Pattern.quote(delimiterValue), caseSensitivity ? 0 : Pattern.CASE_INSENSITIVE);
    }
}
