package ch.njol.skript.expressions.base;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import java.util.Iterator;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Expression wrapper that forwards runtime, change, and simplification behaviour to another expression.
 */
public abstract class WrapperExpression<T> extends SimpleExpression<T> {

    private Expression<? extends T> expr;

    protected WrapperExpression() {
    }

    public WrapperExpression(SimpleExpression<? extends T> expr) {
        this.expr = expr;
    }

    protected void setExpr(Expression<? extends T> expr) {
        this.expr = expr;
    }

    public Expression<?> getExpr() {
        return expr;
    }

    @Override
    protected T[] get(SkriptEvent event) {
        return expr.getArray(event);
    }

    @Override
    public @Nullable Iterator<? extends T> iterator(SkriptEvent event) {
        return expr.iterator(event);
    }

    @Override
    public boolean isSingle() {
        return expr.isSingle();
    }

    @Override
    public boolean getAnd() {
        return expr.getAnd();
    }

    @Override
    public Class<? extends T> getReturnType() {
        return expr.getReturnType();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return expr.acceptChange(mode);
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        expr.change(event, delta, mode);
    }

    @Override
    public boolean setTime(int time) {
        return expr.setTime(time);
    }

    @Override
    public int getTime() {
        return expr.getTime();
    }

    @Override
    public boolean returnNestedStructures(boolean nested) {
        return expr.returnNestedStructures(nested);
    }

    @Override
    public boolean returnsNestedStructures() {
        return expr.returnsNestedStructures();
    }

    @Override
    public boolean isDefault() {
        return expr.isDefault();
    }

    @Override
    public Expression<? extends T> simplify() {
        setExpr(expr.simplify());
        if (getExpr() instanceof Literal<?>) {
            @SuppressWarnings("unchecked")
            Expression<T> self = (Expression<T>) this;
            return SimplifiedLiteral.fromExpression(self);
        }
        return this;
    }

    @Override
    public Object @Nullable [] beforeChange(Expression<?> changed, Object @Nullable [] delta) {
        return expr.beforeChange(changed, delta);
    }

    @Override
    public Class<? extends T>[] possibleReturnTypes() {
        return expr.possibleReturnTypes();
    }

    @Override
    public boolean canReturn(Class<?> returnType) {
        return expr.canReturn(returnType);
    }
}
