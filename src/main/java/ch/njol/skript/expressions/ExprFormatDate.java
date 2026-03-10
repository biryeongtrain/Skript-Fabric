package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import java.text.SimpleDateFormat;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprFormatDate extends PropertyExpression<Date, String> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss z";

    static {
        Skript.registerExpression(ExprFormatDate.class, String.class,
                "%dates% formatted [human-readable] [(with|as) %-string%]",
                "[human-readable] formatted %dates% [(with|as) %-string%]");
    }

    private @Nullable Expression<String> customFormat;
    private @Nullable String fixedPattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends Date>) exprs[0]);
        customFormat = (Expression<String>) exprs[1];
        if (customFormat instanceof Literal<String> literal) {
            String format = literal.getSingle(SkriptEvent.EMPTY);
            if (format != null && !validatePattern(format)) {
                Skript.error("Invalid date format: " + format);
                return false;
            }
            fixedPattern = format;
        }
        return true;
    }

    @Override
    protected String[] get(SkriptEvent event, Date[] source) {
        String pattern = fixedPattern;
        if (pattern == null && customFormat != null) {
            pattern = customFormat.getSingle(event);
            if (pattern == null || !validatePattern(pattern)) {
                return null;
            }
        }
        if (pattern == null) {
            pattern = DEFAULT_PATTERN;
        }

        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return get(source, date -> format.format(new java.util.Date(date.getTime())));
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Expression<? extends String> simplify() {
        if (getExpr() instanceof Literal<?> && (customFormat == null || customFormat instanceof Literal<?>)) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (customFormat != null) {
            return getExpr().toString(event, debug) + " formatted as " + customFormat.toString(event, debug);
        }
        if (fixedPattern != null) {
            return getExpr().toString(event, debug) + " formatted as " + fixedPattern;
        }
        return getExpr().toString(event, debug) + " formatted human-readable";
    }

    private static boolean validatePattern(String pattern) {
        try {
            new SimpleDateFormat(pattern);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
