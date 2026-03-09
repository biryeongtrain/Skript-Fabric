package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.DifferenceInfo;

public class ExprDifference extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprDifference.class, Object.class, "difference (between|of) %object% and %object%");
    }

    private Expression<?> first;
    private Expression<?> second;
    private @Nullable DifferenceInfo<?, ?> differenceInfo;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Expression<?> first = LiteralUtils.defendExpression(expressions[0]);
        Expression<?> second = LiteralUtils.defendExpression(expressions[1]);
        if (!LiteralUtils.canInitSafely(first) || !LiteralUtils.canInitSafely(second)) {
            return false;
        }

        Class<?> firstReturnType = first.getReturnType();
        Class<?> secondReturnType = second.getReturnType();
        Class<?> superType = Utils.getSuperType(firstReturnType, secondReturnType);
        boolean fail = false;

        if (superType == Object.class && (firstReturnType != Object.class || secondReturnType != Object.class)) {
            if (firstReturnType != Object.class && secondReturnType != Object.class) {
                fail = true;

                differenceInfo = Arithmetics.getDifferenceInfo(firstReturnType);
                if (differenceInfo != null) {
                    Expression<?> convertedSecond = second.getConvertedExpression(firstReturnType);
                    if (convertedSecond != null) {
                        second = convertedSecond;
                        fail = false;
                    }
                }

                if (fail) {
                    differenceInfo = Arithmetics.getDifferenceInfo(secondReturnType);
                    if (differenceInfo != null) {
                        Expression<?> convertedFirst = first.getConvertedExpression(secondReturnType);
                        if (convertedFirst != null) {
                            first = convertedFirst;
                            fail = false;
                        }
                    }
                }
            } else {
                Expression<?> converted;
                if (firstReturnType == Object.class) {
                    converted = first.getConvertedExpression(secondReturnType);
                    if (converted != null) {
                        first = converted;
                    }
                } else {
                    converted = second.getConvertedExpression(firstReturnType);
                    if (converted != null) {
                        second = converted;
                    }
                }

                if (converted == null) {
                    fail = true;
                } else {
                    superType = Utils.getSuperType(first.getReturnType(), second.getReturnType());
                }
            }
        }

        if (superType != Object.class && (differenceInfo = Arithmetics.getDifferenceInfo(superType)) == null) {
            fail = true;
        }

        if (fail) {
            Skript.error("Can't get the difference of " + describe(first) + " and " + describe(second));
            return false;
        }

        this.first = first;
        this.second = second;
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object firstValue = first.getSingle(event);
        Object secondValue = second.getSingle(event);
        if (firstValue == null || secondValue == null) {
            return new Object[0];
        }

        DifferenceInfo<?, ?> differenceInfo = this.differenceInfo;
        if (differenceInfo == null) {
            Class<?> superType = Utils.getSuperType(firstValue.getClass(), secondValue.getClass());
            differenceInfo = Arithmetics.getDifferenceInfo(superType);
            if (differenceInfo == null) {
                return new Object[0];
            }
        }

        Object[] value = (Object[]) Array.newInstance(differenceInfo.returnType(), 1);
        value[0] = calculateDifference(differenceInfo, firstValue, secondValue);
        return value;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object calculateDifference(DifferenceInfo differenceInfo, Object first, Object second) {
        return differenceInfo.operation().calculate(first, second);
    }

    private static String describe(Expression<?> expression) {
        if (expression.getReturnType() == Object.class) {
            return expression.toString(null, false);
        }
        return Classes.getSuperClassInfo(expression.getReturnType()).getName().withIndefiniteArticle();
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return differenceInfo == null ? Object.class : differenceInfo.returnType();
    }

    @Override
    public Expression<?> simplify() {
        if (first instanceof Literal<?> && second instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "difference between " + first.toString(event, debug) + " and " + second.toString(event, debug);
    }
}
