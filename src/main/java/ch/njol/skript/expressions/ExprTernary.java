package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTernary extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(
                ExprTernary.class,
                Object.class,
                "%objects% if <.+>[,] (otherwise|else) %objects%"
        );
    }

    private Class<?>[] types;
    private Class<?> superType;
    private Expression<Object> ifTrue;
    private Condition condition;
    private Expression<Object> ifFalse;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        ifTrue = (Expression<Object>) LiteralUtils.defendExpression(exprs[0]);
        ifFalse = (Expression<Object>) LiteralUtils.defendExpression(exprs[1]);
        if (ifTrue instanceof ExprTernary || ifFalse instanceof ExprTernary) {
            Skript.error("Ternary operators may not be nested!");
            return false;
        }
        if (!LiteralUtils.canInitSafely(ifTrue) || !LiteralUtils.canInitSafely(ifFalse)) {
            return false;
        }

        String cond = parseResult.regexes.get(0).group();
        condition = Condition.parse(cond, "Can't understand this condition: " + cond);
        if (condition == null) {
            return false;
        }

        Set<Class<?>> possibleTypes = new HashSet<>();
        Collections.addAll(possibleTypes, ifTrue.possibleReturnTypes());
        Collections.addAll(possibleTypes, ifFalse.possibleReturnTypes());
        types = possibleTypes.toArray(new Class[0]);
        superType = Utils.getSuperType(types);
        return true;
    }

    @Override
    protected Object[] get(SkriptEvent event) {
        return condition.check(event) ? ifTrue.getArray(event) : ifFalse.getArray(event);
    }

    @Override
    public boolean isSingle() {
        return ifTrue.isSingle() && ifFalse.isSingle();
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
    public String toString(SkriptEvent event, boolean debug) {
        return ifTrue.toString(event, debug)
                + " if " + condition.toString(event, debug)
                + " otherwise " + ifFalse.toString(event, debug);
    }
}
