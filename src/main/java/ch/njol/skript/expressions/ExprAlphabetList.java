package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprAlphabetList extends SimpleExpression<String> implements KeyedIterableExpression<String> {

    static {
        Skript.registerExpression(ExprAlphabetList.class, String.class,
                "alphabetically sorted %strings%");
    }

    private Expression<String> texts;
    private boolean keyed;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        texts = (Expression<String>) expressions[0];
        if (texts.isSingle()) {
            Skript.error("A single string cannot be sorted.");
            return false;
        }
        keyed = KeyedIterableExpression.canIterateWithKeys(texts);
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        String[] sorted = texts.getArray(event);
        Arrays.sort(sorted);
        return sorted;
    }

    @Override
    public boolean canIterateWithKeys() {
        return keyed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<KeyedValue<String>> keyedIterator(SkriptEvent event) {
        if (!keyed) {
            throw new UnsupportedOperationException();
        }
        Iterator<KeyedValue<String>> iterator = ((KeyedIterableExpression<String>) texts).keyedIterator(event);
        List<KeyedValue<String>> values = new ArrayList<>();
        while (iterator.hasNext()) {
            values.add(iterator.next());
        }
        values.sort(Comparator.comparing(KeyedValue::value));
        return values.iterator();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isIndexLoop(String input) {
        if (!keyed) {
            throw new IllegalStateException();
        }
        return ((KeyedIterableExpression<String>) texts).isIndexLoop(input);
    }

    @Override
    public boolean isLoopOf(String input) {
        return texts.isLoopOf(input);
    }

    @Override
    public Expression<? extends String> simplify() {
        if (texts instanceof Literal<String>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "alphabetically sorted " + texts.toString(event, debug);
    }
}
