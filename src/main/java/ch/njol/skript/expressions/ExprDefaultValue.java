package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDefaultValue extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprDefaultValue.class, Object.class, "%objects% (otherwise|?) %objects%");
    }

    private Class<?>[] types;
    private Class<?> superType;
    private Expression<Object> values;
    private Expression<Object> defaultValues;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        values = (Expression<Object>) LiteralUtils.defendExpression(exprs[0]);
        defaultValues = (Expression<Object>) LiteralUtils.defendExpression(exprs[1]);
        if (!LiteralUtils.canInitSafely(values) || !LiteralUtils.canInitSafely(defaultValues)) {
            return false;
        }

        Set<Class<?>> allTypes = new HashSet<>();
        Collections.addAll(allTypes, values.possibleReturnTypes());
        Collections.addAll(allTypes, defaultValues.possibleReturnTypes());
        types = allTypes.toArray(Class<?>[]::new);
        superType = Utils.getSuperType(types);
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object[] resolvedValues = values.getArray(event);
        if (resolvedValues.length != 0) {
            return resolvedValues;
        }
        return defaultValues.getArray(event);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle() && defaultValues.isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        return superType;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return Arrays.copyOf(types, types.length);
    }

    @Override
    public Expression<?> simplify() {
        if (values instanceof Literal<Object> literal
                && (defaultValues instanceof Literal<Object> || literal.getAll(SkriptEvent.EMPTY).length > 0)) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return values.toString(event, debug) + " or else " + defaultValues.toString(event, debug);
    }
}
