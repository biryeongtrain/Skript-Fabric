package ch.njol.skript.expressions.base;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Compatibility base for expressions that expose a property of another expression.
 */
public abstract class PropertyExpression<F, T> extends SimpleExpression<T> {

    public static final Priority DEFAULT_PRIORITY = Priority.before(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

    private @UnknownNullability Expression<? extends F> expr;

    private static String[] patternsOf(String property, String fromType, boolean defaultExpr) {
        Preconditions.checkNotNull(property, "property must be present");
        Preconditions.checkNotNull(fromType, "fromType must be present");
        String types = defaultExpr ? "[of %" + fromType + "%]" : "of %" + fromType + "%";
        return new String[]{"[the] " + property + " " + types, "%" + fromType + "%'[s] " + property};
    }

    public static String[] getPatterns(String property, String fromType) {
        return patternsOf(property, fromType, false);
    }

    public static String[] getDefaultPatterns(String property, String fromType) {
        return patternsOf(property, fromType, true);
    }

    public static <E extends Expression<T>, T> SyntaxInfo.Expression.Builder<E, T> infoBuilder(
            Class<E> expressionClass,
            Class<T> returnType,
            String property,
            String type,
            boolean isDefault
    ) {
        return SyntaxInfo.Expression.<E, T>builder(expressionClass, returnType)
                .priority(DEFAULT_PRIORITY)
                .patterns(patternsOf(property, type, isDefault));
    }

    public static <T> void register(
            Class<? extends Expression<T>> expressionClass,
            Class<T> type,
            String property,
            String fromType
    ) {
        Skript.registerExpression(expressionClass, type, getPatterns(property, fromType));
    }

    public static <T> void registerDefault(
            Class<? extends Expression<T>> expressionClass,
            Class<T> type,
            String property,
            String fromType
    ) {
        Skript.registerExpression(expressionClass, type, getDefaultPatterns(property, fromType));
    }

    protected final void setExpr(@NotNull Expression<? extends F> expr) {
        Preconditions.checkNotNull(expr, "The expr param cannot be null");
        this.expr = expr;
    }

    public final Expression<? extends F> getExpr() {
        return expr;
    }

    @Override
    protected final T[] get(SkriptEvent event) {
        return get(event, expr.getArray(event));
    }

    @Override
    public final T[] getAll(SkriptEvent event) {
        T[] result = get(event, expr.getAll(event));
        return Arrays.copyOf(result, result.length);
    }

    protected abstract T[] get(SkriptEvent event, F[] source);

    protected T[] get(F[] source, Converter<? super F, ? extends T> converter) {
        return Converters.convertUnsafe(source, getReturnType(), converter);
    }

    @Override
    public boolean isSingle() {
        return expr.isSingle();
    }

    @Override
    public final boolean getAnd() {
        return expr.getAnd();
    }

    @Override
    public Expression<? extends T> simplify() {
        expr = expr.simplify();
        return this;
    }
}
