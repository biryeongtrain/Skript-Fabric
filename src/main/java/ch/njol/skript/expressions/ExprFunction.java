package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Namespace;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

@Name("Function")
@Description("Obtain a function by name, which can be executed.")
@Example("set {_function} to the function named \"myFunction\"")
@Example("run {_function} with arguments 13 and true")
@Since("2.10")
@SuppressWarnings("rawtypes")
public class ExprFunction extends SimpleExpression<DynamicFunctionReference> implements ReflectionExperimentSyntax {

    static {
        Skript.registerExpression(ExprFunction.class, DynamicFunctionReference.class,
                "[the|a] function [named] %string% [(in|from) %-script%]",
                "[the] functions [named] %strings% [(in|from) %-script%]",
                "[all [[of] the]|the] functions (in|from) %script%");
    }

    private Expression<String> name;
    private Expression<Script> script;
    private int mode;
    private boolean local;
    private Script here;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult result) {
        this.mode = matchedPattern;
        this.local = mode == 2 || expressions[1] != null;
        switch (mode) {
            case 0, 1 -> {
                this.name = (Expression<String>) expressions[0];
                if (local) {
                    this.script = (Expression<Script>) expressions[1];
                }
            }
            case 2 -> this.script = (Expression<Script>) expressions[0];
            default -> {
            }
        }
        this.here = this.getParser().getCurrentScript();
        return true;
    }

    @Override
    protected DynamicFunctionReference<?>[] get(SkriptEvent event) {
        Script script = local ? this.script.getSingle(event) : here;
        String sourceName = script != null && script.getConfig() != null ? script.getConfig().getFileName() : null;
        return switch (mode) {
            case 0 -> {
                String name = this.name.getSingle(event);
                if (name == null) {
                    yield new DynamicFunctionReference<?>[0];
                }
                DynamicFunctionReference<?> reference = DynamicFunctionReference.resolveFunction(name, sourceName);
                if (reference == null) {
                    yield new DynamicFunctionReference<?>[0];
                }
                yield new DynamicFunctionReference<?>[]{reference};
            }
            case 1 -> this.name.stream(event)
                    .map(string -> DynamicFunctionReference.resolveFunction(string, sourceName))
                    .filter(Objects::nonNull)
                    .toArray(DynamicFunctionReference[]::new);
            case 2 -> {
                if (script == null || script.getConfig() == null) {
                    yield new DynamicFunctionReference<?>[0];
                }
                Namespace namespace = Functions.getScriptNamespace(script.getConfig().getFileName());
                if (namespace == null) {
                    yield new DynamicFunctionReference<?>[0];
                }
                yield namespace.getFunctions().stream()
                        .map(DynamicFunctionReference::new)
                        .toArray(DynamicFunctionReference[]::new);
            }
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean isSingle() {
        return mode != 2 && name.isSingle();
    }

    @Override
    public Class<? extends DynamicFunctionReference> getReturnType() {
        return DynamicFunctionReference.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (mode) {
            case 0 -> "the function named " + name.toString(event, debug)
                    + (local ? " from " + script.toString(event, debug) : "");
            case 1 -> "functions named " + name.toString(event, debug)
                    + (local ? " from " + script.toString(event, debug) : "");
            case 2 -> "the functions from " + script.toString(event, debug);
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }
}
