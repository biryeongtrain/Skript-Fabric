package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Comparators;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprIndicesOfValue extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprIndicesOfValue.class, Object.class,
                "[the] [1:first|2:last|3:all] (position[mult:s]|mult:indices|index[mult:es]) of [[the] value] %strings% in %string%",
                "[the] [1:first|2:last|3:all] position[mult:s] of [[the] value] %objects% in %~objects%",
                "[the] [1:first|2:last|3:all] (mult:indices|index[mult:es]) of [[the] value] %objects% in %~objects%");
    }

    private IndexType indexType;
    private boolean position;
    private boolean string;
    private Expression<?> needle;
    private Expression<?> haystack;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (exprs[1].isSingle() && matchedPattern > 0) {
            Skript.error("'" + exprs[1] + "' can only ever have one value at most, thus the 'indices of x in list' expression has no effect.");
            return false;
        }
        if (!KeyProviderExpression.canReturnKeys(exprs[1]) && matchedPattern == 2) {
            Skript.error("'" + exprs[1] + "' is not a keyed expression. You can only get the indices of a keyed expression.");
            return false;
        }

        indexType = IndexType.values()[parseResult.mark == 0 ? 0 : parseResult.mark - 1];
        if (parseResult.mark == 0 && parseResult.hasTag("mult")) {
            indexType = IndexType.ALL;
        }

        position = matchedPattern <= 1;
        string = matchedPattern == 0;
        needle = exprs[0];
        haystack = exprs[1];
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object[] needleValues = needle.getAll(event);
        if (needleValues.length == 0) {
            return position ? new Long[0] : new String[0];
        }

        if (!position) {
            return getIndices((KeyProviderExpression<?>) haystack, needleValues, event);
        }
        if (!string) {
            return getListPositions(haystack, needleValues, event);
        }

        Object haystackValue = haystack.getSingle(event);
        if (!(haystackValue instanceof String haystackString)) {
            return new Long[0];
        }
        return getStringPositions(haystackString, Arrays.copyOf(needleValues, needleValues.length, String[].class));
    }

    private Long[] getStringPositions(String haystackValue, String[] needles) {
        List<Long> positions = new ArrayList<>();
        String normalizedHaystack = haystackValue.toLowerCase(Locale.ENGLISH);
        for (String needleValue : needles) {
            if (needleValue == null || needleValue.isEmpty()) {
                continue;
            }
            String normalizedNeedle = needleValue.toLowerCase(Locale.ENGLISH);
            long positionIndex = normalizedHaystack.indexOf(normalizedNeedle);
            if (positionIndex == -1) {
                continue;
            }

            switch (indexType) {
                case FIRST -> positions.add(positionIndex + 1);
                case LAST -> positions.add((long) normalizedHaystack.lastIndexOf(normalizedNeedle) + 1);
                case ALL -> {
                    do {
                        positions.add(positionIndex + 1);
                        positionIndex = normalizedHaystack.indexOf(normalizedNeedle, (int) positionIndex + 1);
                    } while (positionIndex != -1);
                }
            }
        }
        return positions.toArray(Long[]::new);
    }

    private <Item, Index, Value> Index[] getMatches(
            Iterator<Item> haystackIterator,
            Value[] needles,
            Function<Item, Value> valueMapper,
            BiFunction<Item, Long, Index> indexMapper,
            IntFunction<Index[]> arrayFactory
    ) {
        @SuppressWarnings("unchecked")
        List<Index>[] results = new List[needles.length];
        long index = 1;
        while (haystackIterator.hasNext()) {
            Item item = haystackIterator.next();
            for (int i = 0; i < needles.length; i++) {
                if (!equals(valueMapper.apply(item), needles[i])) {
                    continue;
                }
                Index mappedIndex = indexMapper.apply(item, index);
                switch (indexType) {
                    case FIRST, LAST -> results[i] = List.of(mappedIndex);
                    case ALL -> {
                        if (results[i] == null) {
                            results[i] = new ArrayList<>();
                        }
                        results[i].add(mappedIndex);
                    }
                }
            }
            if (indexType == IndexType.FIRST && Arrays.stream(results).noneMatch(Objects::isNull)) {
                break;
            }
            index++;
        }

        return Arrays.stream(results)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toArray(arrayFactory);
    }

    private Long[] getListPositions(Expression<?> haystackExpression, Object[] needles, SkriptEvent event) {
        Iterator<?> haystackIterator = haystackExpression.iterator(event);
        if (haystackIterator == null) {
            return new Long[0];
        }
        return getMatches(haystackIterator, needles, item -> item, (item, index) -> index, Long[]::new);
    }

    private String[] getIndices(KeyProviderExpression<?> haystackExpression, Object[] needles, SkriptEvent event) {
        Iterator<? extends KeyedValue<?>> haystackIterator = haystackExpression.keyedIterator(event);
        if (haystackIterator == null) {
            return new String[0];
        }
        return getMatches(haystackIterator, needles, KeyedValue::value, (item, index) -> item.key(), String[]::new);
    }

    private boolean equals(@Nullable Object left, @Nullable Object right) {
        if (left instanceof String leftString && right instanceof String rightString) {
            return leftString.equalsIgnoreCase(rightString);
        }
        if (left == null || right == null) {
            return left == right;
        }
        return left.equals(right) || Comparators.compare(left, right) == Relation.EQUAL;
    }

    @Override
    public boolean isSingle() {
        return (indexType == IndexType.FIRST || indexType == IndexType.LAST) && needle.isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        return position ? Long.class : String.class;
    }

    @Override
    public Expression<?> simplify() {
        if (position && string && needle instanceof Literal<?> && haystack instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(indexType.name().toLowerCase(Locale.ENGLISH));
        builder.append(position ? "positions" : "indices");
        builder.append("of value", needle, "in", haystack);
        return builder.toString();
    }

    private enum IndexType {
        FIRST,
        LAST,
        ALL
    }
}
