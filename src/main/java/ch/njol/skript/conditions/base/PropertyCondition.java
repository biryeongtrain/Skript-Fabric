package ch.njol.skript.conditions.base;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

/**
 * Compatibility base for conditions in the form of "x is y", "x has y", "x can y", or "x will y".
 */
public abstract class PropertyCondition<T> extends Condition implements Predicate<T> {

    public static final Priority DEFAULT_PRIORITY = Priority.before(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

    public enum PropertyType {
        BE,
        CAN,
        HAVE,
        WILL
    }

    private Expression<? extends T> expr;

    public static void register(Class<? extends Condition> condition, String property, String type) {
        register(condition, PropertyType.BE, property, type);
    }

    public static void register(
            Class<? extends Condition> condition,
            PropertyType propertyType,
            String property,
            String type
    ) {
        Skript.registerCondition(condition, getPatterns(propertyType, property, type));
    }

    public static String[] getPatterns(PropertyType propertyType, String property, String type) {
        if (type.contains("%")) {
            throw new SkriptAPIException("The type argument must not contain any '%'s");
        }

        return switch (propertyType) {
            case BE -> new String[]{
                    "%" + type + "% (is|are) " + property,
                    "%" + type + "% (isn't|is not|aren't|are not) " + property
            };
            case CAN -> new String[]{
                    "%" + type + "% can " + property,
                    "%" + type + "% (can't|cannot|can not) " + property
            };
            case HAVE -> new String[]{
                    "%" + type + "% (has|have) " + property,
                    "%" + type + "% (doesn't|does not|do not|don't) have " + property
            };
            case WILL -> new String[]{
                    "%" + type + "% will " + property,
                    "%" + type + "% (will (not|neither)|won't) " + property
            };
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        expr = (Expression<? extends T>) expressions[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public final boolean check(SkriptEvent event) {
        return expr.check(event, this, isNegated());
    }

    public abstract boolean check(T value);

    @Override
    public final boolean test(T value) {
        return check(value);
    }

    protected abstract String getPropertyName();

    protected PropertyType getPropertyType() {
        return PropertyType.BE;
    }

    public final Expression<? extends T> getExpr() {
        return expr;
    }

    protected final void setExpr(Expression<? extends T> expr) {
        this.expr = expr;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return toString(this, getPropertyType(), event, debug, expr, getPropertyName());
    }

    public static String toString(
            Condition condition,
            PropertyType propertyType,
            @Nullable SkriptEvent event,
            boolean debug,
            Expression<?> expr,
            String property
    ) {
        return switch (propertyType) {
            case BE -> expr.toString(event, debug)
                    + (expr.isSingle() ? " is " : " are ")
                    + (condition.isNegated() ? "not " : "")
                    + property;
            case CAN -> expr.toString(event, debug)
                    + (condition.isNegated() ? " can't " : " can ")
                    + property;
            case HAVE -> expr.toString(event, debug)
                    + (expr.isSingle()
                    ? (condition.isNegated() ? " doesn't have " : " has ")
                    : (condition.isNegated() ? " don't have " : " have "))
                    + property;
            case WILL -> expr.toString(event, debug)
                    + (condition.isNegated() ? " won't " : " will ")
                    + "be "
                    + property;
        };
    }

    public static String toString(
            Object condition,
            PropertyType propertyType,
            @Nullable SkriptEvent event,
            boolean debug,
            Expression<?> expr,
            String property
    ) {
        if (condition instanceof Condition propertyCondition) {
            return toString(propertyCondition, propertyType, event, debug, expr, property);
        }
        return switch (propertyType) {
            case BE -> expr.toString(event, debug) + " is " + property;
            case CAN -> expr.toString(event, debug) + " can " + property;
            case HAVE -> expr.toString(event, debug) + " has " + property;
            case WILL -> expr.toString(event, debug) + " will be " + property;
        };
    }

    @Override
    public @NotNull Predicate<T> and(@NotNull Predicate<? super T> other) {
        throw new UnsupportedOperationException("Combining property conditions is undefined behaviour");
    }

    @Override
    public @NotNull Predicate<T> negate() {
        throw new UnsupportedOperationException("Negating property conditions without setNegated is undefined behaviour");
    }

    @Override
    public @NotNull Predicate<T> or(@NotNull Predicate<? super T> other) {
        throw new UnsupportedOperationException("Combining property conditions is undefined behaviour");
    }
}
