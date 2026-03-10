package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Copy Into Variable")
@Description({
        "Copies objects into a variable. When copying a list over to another list, the source list and its sublists are also copied over.",
        "<strong>Note: Copying a value into a variable/list will overwrite the existing data.</strong>"
})
@Example("""
        set {_foo::bar} to 1
        set {_foo::sublist::foobar} to "hey"
        copy {_foo::*} to {_copy::*}
        broadcast indices of {_copy::*} # bar, sublist
        broadcast {_copy::bar} # 1
        broadcast {_copy::sublist::foobar} # "hey!"
        """)
@Since("2.8.0")
@Keywords({"clone", "variable", "list"})
public final class EffCopy extends Effect {

    private static boolean registered;

    private Expression<?> source;
    private Expression<?> rawDestination;
    private List<Variable<?>> destinations;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffCopy.class, "copy %~objects% [in]to %~objects%");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        source = exprs[0];
        rawDestination = exprs[1];
        if (exprs[1] instanceof Variable<?> variable) {
            destinations = Collections.singletonList(variable);
        } else if (exprs[1] instanceof ExpressionList<?> expressionList) {
            destinations = unwrapExpressionList(expressionList);
        }
        if (destinations == null) {
            Skript.error("You can only copy objects into variables");
            return false;
        }
        for (Variable<?> destination : destinations) {
            if (!source.isSingle() && destination.isSingle()) {
                Skript.error("Cannot copy multiple objects into a single variable");
                return false;
            }
        }

        Class<?>[] sourceHints = source.possibleReturnTypes();
        HintManager hintManager = getParser().getHintManager();
        for (Variable<?> destination : destinations) {
            if (HintManager.canUseHints(destination)) {
                hintManager.set(destination, sourceHints);
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void execute(SkriptEvent event) {
        if (!(source instanceof Variable<?> sourceVariable) || source.isSingle()) {
            ChangeMode mode = ChangeMode.SET;
            Object[] clone = (Object[]) Classes.clone(source.getArray(event));
            if (clone.length == 0) {
                mode = ChangeMode.DELETE;
            }
            for (Variable<?> destination : destinations) {
                destination.change(event, clone, mode);
            }
            return;
        }

        Object rawSource = Variables.getVariable(sourceVariable.getName().toString(event), event, sourceVariable.isLocal());
        Map<String, Object> copied = copyMap((Map<String, Object>) rawSource);
        if (copied != null) {
            copied.remove(null);
        }

        for (Variable<?> destination : destinations) {
            destination.change(event, null, ChangeMode.DELETE);
            if (copied == null) {
                continue;
            }
            String target = destination.getName().toString(event);
            target = target.substring(0, target.length() - (Variable.SEPARATOR + "*").length());
            set(event, target, copied, destination.isLocal());
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "copy " + source.toString(event, debug) + " into " + rawDestination.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Map<String, Object> copyMap(@Nullable Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> copy = new HashMap<>(map.size());
        map.forEach((key, value) -> {
            if (value instanceof Map<?, ?> nestedMap) {
                copy.put(key, copyMap((Map<String, Object>) nestedMap));
                return;
            }
            copy.put(key, Classes.clone(value));
        });
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static void set(SkriptEvent event, String targetName, Map<String, Object> source, boolean local) {
        source.forEach((key, value) -> {
            String node = targetName + (key == null ? "" : Variable.SEPARATOR + key);
            if (value instanceof Map<?, ?> nestedMap) {
                set(event, node, (Map<String, Object>) nestedMap, local);
                return;
            }
            Variables.setVariable(node, value, event, local);
        });
    }

    private static @Nullable List<Variable<?>> unwrapExpressionList(ExpressionList<?> expressionList) {
        Expression<?>[] expressions = expressionList.getExpressions();
        List<Variable<?>> unwrapped = new ArrayList<>();
        for (Expression<?> expression : expressions) {
            if (expression instanceof Variable<?> variable) {
                unwrapped.add(variable);
                continue;
            }
            if (!(expression instanceof ExpressionList<?> nested)) {
                return null;
            }
            List<Variable<?>> nestedVariables = unwrapExpressionList(nested);
            if (nestedVariables == null) {
                return null;
            }
            unwrapped.addAll(nestedVariables);
        }
        return unwrapped;
    }
}
