package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
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

@Name("Run")
@Description("Executes a task (a function). Any returned result is discarded.")
@Example("""
        set {_function} to the function named "myFunction"
        run {_function}
        run {_function} with arguments {_things::*}
        """)
@Since("2.10")
@Keywords({"run", "execute", "reflection", "function"})
@SuppressWarnings({"rawtypes", "unchecked"})
public class EffRun extends Effect implements ReflectionExperimentSyntax {

    private static boolean registered;

    private Expression<Executable> executable;
    private Expression<?> arguments;
    private DynamicFunctionReference.Input input;
    private boolean hasArguments;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffRun.class,
                "run %executable% [arguments:with arg[ument]s %-objects%]",
                "execute %executable% [arguments:with arg[ument]s %-objects%]"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult result) {
        executable = (Expression<Executable>) expressions[0];
        hasArguments = result.hasTag("arguments");
        if (hasArguments) {
            arguments = LiteralUtils.defendExpression(expressions[1]);
            Expression<?>[] resolvedArguments;
            if (arguments instanceof ExpressionList<?> list) {
                resolvedArguments = list.getExpressions();
            } else {
                resolvedArguments = new Expression[]{arguments};
            }
            input = new DynamicFunctionReference.Input(resolvedArguments);
            return LiteralUtils.canInitSafely(arguments);
        }
        input = new DynamicFunctionReference.Input();
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Executable task = executable.getSingle(event);
        if (task == null) {
            return;
        }
        Object[] resolvedArguments;
        if (task instanceof DynamicFunctionReference<?> reference) {
            Expression<?> validated = reference.validate(input);
            if (validated == null) {
                return;
            }
            resolvedArguments = validated.getArray(event);
        } else if (hasArguments) {
            resolvedArguments = arguments.getArray(event);
        } else {
            resolvedArguments = new Object[0];
        }
        task.execute(event, resolvedArguments);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (hasArguments) {
            return "run " + executable.toString(event, debug) + " with arguments " + arguments.toString(event, debug);
        }
        return "run " + executable.toString(event, debug);
    }
}
