package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Transform List")
@Description({
        "Transforms (or 'maps') a list's values using a given expression. This is akin to looping over the list and setting "
                + "each value to a modified version of itself.",
        "Evaluates the given expression for each element in the list, replacing the original element with the expression's result.",
        "If the given expression returns a single value, the indices of the list will not change. If the expression returns "
                + "multiple values, then then indices will be reset as a single index cannot contain multiple values.",
        "Only variable lists can be transformed with this effect. For other lists, see the transform expression."
})
@Example("""
        set {_a::*} to 1, 2, and 3
        transform {_a::*} using input * 2
        # {_a::*} is now 2, 4, and 6
        """)
@Example("""
        # get a list of the sizes of all clans without manually looping
        set {_clan-sizes::*} to indices of {clans::*}
        transform {_clan-sizes::*} using {clans::%input%::size}
        """)
@Example("""
        # set all existing values of a list to 0:
        transform {_list::*} with 0
        """)
@Since("2.10")
@Keywords("input")
public class EffTransform extends Effect implements InputSource {

    private static boolean registered;

    private @UnknownNullability Expression<?> mappingExpr;
    private @UnknownNullability Variable<?> unmappedObjects;
    private final Set<ExprInput<?>> dependentInputs = new HashSet<>();
    private @Nullable Object currentValue;
    private @UnknownNullability String currentIndex;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffTransform.class, "(transform|map) %~objects% (using|with) <.+>");
        if (!ParserInstance.isRegistered(InputSource.InputData.class)) {
            ParserInstance.registerData(InputSource.InputData.class, InputSource.InputData::new);
        }
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (parseResult.regexes.isEmpty()) {
            return false;
        }
        if (expressions[0].isSingle() || !(expressions[0] instanceof Variable<?> variable)) {
            Skript.error("You can only transform list variables!");
            return false;
        }
        unmappedObjects = variable;

        String unparsedExpression = parseResult.regexes.get(0).group();
        mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.ALL_FLAGS);
        if (mappingExpr == null) {
            return false;
        }

        if (HintManager.canUseHints(variable)) {
            getParser().getHintManager().set(variable, mappingExpr.possibleReturnTypes());
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Map<String, Object> mappedValues = new HashMap<>();
        boolean single = mappingExpr.isSingle();

        String varName = unmappedObjects.getName().toString(event);
        String varSubName = varName.substring(0, varName.length() - 1);
        boolean local = unmappedObjects.isLocal();

        int nextIndex = 1;
        for (Iterator<? extends KeyedValue<?>> it = unmappedObjects.keyedIterator(event); it.hasNext(); ) {
            KeyedValue<?> keyedValue = it.next();
            currentIndex = keyedValue.key();
            currentValue = keyedValue.value();

            if (single) {
                mappedValues.put(currentIndex, mappingExpr.getSingle(event));
                continue;
            }
            for (Object value : mappingExpr.getArray(event)) {
                mappedValues.put(String.valueOf(nextIndex++), value);
                mappedValues.putIfAbsent(currentIndex, null);
            }
        }

        for (Map.Entry<String, Object> pair : mappedValues.entrySet()) {
            Variables.setVariable(varSubName + pair.getKey(), pair.getValue(), event, local);
        }
    }

    @Override
    public Set<ExprInput<?>> getDependentInputs() {
        return dependentInputs;
    }

    @Override
    public @Nullable Object getCurrentValue() {
        return currentValue;
    }

    @Override
    public boolean hasIndices() {
        return true;
    }

    @Override
    public @UnknownNullability String getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "transform " + unmappedObjects.toString(event, debug) + " using " + mappingExpr.toString(event, debug);
    }
}
