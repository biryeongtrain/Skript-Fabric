package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.util.Executable;

@Name("Result")
@Description({
        "Runs something (like a function) and returns its result.",
        "If the thing is expected to return multiple values, use 'results' instead of 'result'."
})
@Example("set {_function} to the function named \"myFunction\"")
@Example("set {_result} to the result of {_function}")
@Example("set {_list::*} to the results of {_function}")
@Example("set {_result} to the result of {_function} with arguments 13 and true")
@Since("2.10")
@Keywords({"run", "result", "execute", "function", "reflection"})
public class ExprResult extends PropertyExpression<Executable<SkriptEvent, Object>, Object>
        implements ReflectionExperimentSyntax {

    static {
        Skript.registerExpression(
                ExprResult.class,
                Object.class,
                "[the] result[plural:s] of [running|executing] %executable% [arguments:with arg[ument]s %-objects%]"
        );
    }

    private @Nullable Expression<?> arguments;
    private boolean hasArguments;
    private boolean plural;
    private DynamicFunctionReference.Input input = new DynamicFunctionReference.Input();

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends Executable<SkriptEvent, Object>>) expressions[0]);
        hasArguments = parseResult.hasTag("arguments");
        plural = parseResult.hasTag("plural");
        if (!hasArguments) {
            return true;
        }
        arguments = LiteralUtils.defendExpression(expressions[1]);
        Expression<?>[] parameters;
        if (arguments instanceof ExpressionList<?> list) {
            parameters = list.getExpressions();
        } else {
            parameters = new Expression[]{arguments};
        }
        input = new DynamicFunctionReference.Input(parameters);
        return LiteralUtils.canInitSafely(arguments);
    }

    @Override
    protected Object[] get(SkriptEvent event, Executable<SkriptEvent, Object>[] source) {
        for (Executable<SkriptEvent, Object> executable : source) {
            Object[] resolvedArguments = resolveArguments(event, executable);
            if (resolvedArguments == null) {
                return new Object[0];
            }
            Object result = executable.execute(event, resolvedArguments);
            if (result instanceof Object[] values) {
                return values;
            }
            return new Object[]{result};
        }
        return new Object[0];
    }

    private @Nullable Object[] resolveArguments(SkriptEvent event, Executable<SkriptEvent, Object> executable) {
        if (executable instanceof DynamicFunctionReference<?> reference) {
            Expression<?> validated = reference.validate(input);
            if (validated == null) {
                return null;
            }
            return validated.getArray(event);
        }
        if (!hasArguments || arguments == null) {
            return new Object[0];
        }
        return arguments.getArray(event);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return null;
    }

    @Override
    public Class<Object> getReturnType() {
        return Object.class;
    }

    @Override
    public boolean isSingle() {
        return !plural;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String text = "the result" + (plural ? "s" : "") + " of " + getExpr().toString(event, debug);
        if (hasArguments && arguments != null) {
            text += " with arguments " + arguments.toString(event, debug);
        }
        return text;
    }
}
