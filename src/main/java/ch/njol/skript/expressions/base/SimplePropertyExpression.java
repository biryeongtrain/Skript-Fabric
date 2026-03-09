package ch.njol.skript.expressions.base;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Compact property-expression base that converts each source value individually.
 */
public abstract class SimplePropertyExpression<F, T> extends PropertyExpression<F, T> implements Converter<F, T> {

    protected String rawExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (LiteralUtils.hasUnparsedLiteral(expressions[0])) {
            setExpr((Expression<? extends F>) LiteralUtils.defendExpression(expressions[0]));
            return LiteralUtils.canInitSafely(getExpr());
        }
        setExpr((Expression<? extends F>) expressions[0]);
        rawExpr = parseResult.expr;
        return true;
    }

    @Override
    @Nullable
    public abstract T convert(F from);

    @Override
    protected T[] get(SkriptEvent event, F[] source) {
        return super.get(source, this);
    }

    protected abstract String getPropertyName();

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getPropertyName() + " of " + getExpr().toString(event, debug);
    }
}
