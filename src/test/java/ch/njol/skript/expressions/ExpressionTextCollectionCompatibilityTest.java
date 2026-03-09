package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ExpressionTextCollectionCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void lengthAndCharacterExpressionsMatchUpstreamBehaviors() {
        ExprLength length = new ExprLength();
        length.init(new Expression[]{new SimpleLiteral<>(new String[]{"AbC123"}, String.class, true)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(6L, length.getSingle(SkriptEvent.EMPTY));

        ExprNumberOfCharacters uppercase = new ExprNumberOfCharacters();
        uppercase.init(new Expression[]{new SimpleLiteral<>("AbC123", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(2L, uppercase.getSingle(SkriptEvent.EMPTY));

        ExprNumberOfCharacters lowercase = new ExprNumberOfCharacters();
        lowercase.init(new Expression[]{new SimpleLiteral<>("AbC123", false)}, 1, Kleenean.FALSE, parseResult(""));
        assertEquals(1L, lowercase.getSingle(SkriptEvent.EMPTY));

        ExprNumberOfCharacters digits = new ExprNumberOfCharacters();
        digits.init(new Expression[]{new SimpleLiteral<>("AbC123", false)}, 2, Kleenean.FALSE, parseResult(""));
        assertEquals(3L, digits.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void substringSupportsRangesFirstLastAndIndexedSelections() {
        ExprSubstring range = new ExprSubstring();
        range.init(new Expression[]{
                new SimpleLiteral<>("abcdef", false),
                new SimpleLiteral<>(2, false),
                new SimpleLiteral<>(4, false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"bcd"}, range.getArray(SkriptEvent.EMPTY));

        ExprSubstring first = new ExprSubstring();
        SkriptParser.ParseResult firstParse = parseResult("");
        firstParse.mark = 1;
        first.init(new Expression[]{
                new SimpleLiteral<>(3, false),
                new SimpleLiteral<>("abcdef", false)
        }, 1, Kleenean.FALSE, firstParse);
        assertArrayEquals(new String[]{"abc"}, first.getArray(SkriptEvent.EMPTY));

        ExprSubstring last = new ExprSubstring();
        SkriptParser.ParseResult lastParse = parseResult("");
        lastParse.mark = 2;
        last.init(new Expression[]{
                new SimpleLiteral<>(2, false),
                new SimpleLiteral<>("abcdef", false)
        }, 1, Kleenean.FALSE, lastParse);
        assertArrayEquals(new String[]{"ef"}, last.getArray(SkriptEvent.EMPTY));

        ExprSubstring indexed = new ExprSubstring();
        indexed.init(new Expression[]{
                new SimpleLiteral<>(new Number[]{1, 3, 6}, Number.class, true),
                new SimpleLiteral<>("abcdef", false)
        }, 3, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"a", "c", "f"}, indexed.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void charactersAndCodepointsFollowLegacyTextSemantics() {
        ExprCharacters ascending = new ExprCharacters();
        ascending.init(new Expression[]{
                new SimpleLiteral<>("a", false),
                new SimpleLiteral<>("d", false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, ascending.getArray(SkriptEvent.EMPTY));

        ExprCharacters descendingAlphanumeric = new ExprCharacters();
        SkriptParser.ParseResult descendingParse = parseResult("");
        descendingParse.tags.add("alphanumeric");
        descendingAlphanumeric.init(new Expression[]{
                new SimpleLiteral<>("d", false),
                new SimpleLiteral<>("a", false)
        }, 0, Kleenean.FALSE, descendingParse);
        assertArrayEquals(new String[]{"d", "c", "b", "a"}, descendingAlphanumeric.getArray(SkriptEvent.EMPTY));

        ExprCharacters alphanumericRange = new ExprCharacters();
        SkriptParser.ParseResult alphanumericParse = parseResult("");
        alphanumericParse.tags.add("alphanumeric");
        alphanumericRange.init(new Expression[]{
                new SimpleLiteral<>("0", false),
                new SimpleLiteral<>("C", false)
        }, 0, Kleenean.FALSE, alphanumericParse);
        assertArrayEquals(
                new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C"},
                alphanumericRange.getArray(SkriptEvent.EMPTY)
        );

        ExprCodepoint codepoint = new ExprCodepoint();
        codepoint.init(new Expression[]{new SimpleLiteral<>("A", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(65, codepoint.getSingle(SkriptEvent.EMPTY));

        ExprCodepoint emojiCodepoint = new ExprCodepoint();
        emojiCodepoint.init(new Expression[]{new SimpleLiteral<>("🙂", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("🙂".codePointAt(0), emojiCodepoint.getSingle(SkriptEvent.EMPTY));

        ExprCharacterFromCodepoint fromCodepoint = new ExprCharacterFromCodepoint();
        fromCodepoint.init(new Expression[]{new SimpleLiteral<>(65, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("A", fromCodepoint.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void alphabeticAndNaturalSortPreserveKeyedIteration() {
        ExprAlphabetList alphabetic = new ExprAlphabetList();
        alphabetic.init(new Expression[]{new KeyedStringExpression(
                new String[]{"beta", "alpha", "gamma"},
                new String[]{"two", "one", "three"}
        )}, 0, Kleenean.FALSE, parseResult(""));

        assertArrayEquals(new String[]{"alpha", "beta", "gamma"}, alphabetic.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"one", "two", "three"}, collectKeys(alphabetic.keyedIterator(SkriptEvent.EMPTY)));

        ExprSortedList sorted = new ExprSortedList();
        sorted.init(new Expression[]{new KeyedObjectExpression(
                new Object[]{10, 2, 5},
                new String[]{"ten", "two", "five"}
        )}, 0, Kleenean.FALSE, parseResult(""));

        assertArrayEquals(new Object[]{2, 5, 10}, sorted.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"two", "five", "ten"}, collectKeys(sorted.keyedIterator(SkriptEvent.EMPTY)));
        assertTrue(sorted.canReturn(Number.class));
    }

    @Test
    void anyOfExceptReverseShuffleAndDifferenceMatchLegacySemantics() {
        ExprAnyOf anyOf = new ExprAnyOf();
        anyOf.init(new Expression[]{new SimpleLiteral<>(new String[]{"alpha", "beta"}, String.class, true)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(anyOf.isSingle());
        assertEquals(false, anyOf.getAnd());
        assertNull(anyOf.acceptChange(ch.njol.skript.classes.Changer.ChangeMode.SET));

        ExprExcept except = new ExprExcept();
        except.init(new Expression[]{
                new ExpressionList<>(
                        new Expression[]{
                                new SimpleLiteral<>("alpha", false),
                                new SimpleLiteral<>("beta", false),
                                new SimpleLiteral<>("gamma", false)
                        },
                        Object.class,
                        true
                ),
                new SimpleLiteral<>(new String[]{"beta"}, String.class, true)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new Object[]{"alpha", "gamma"}, except.getArray(SkriptEvent.EMPTY));

        ExprReversedList reversed = new ExprReversedList();
        reversed.init(new Expression[]{new KeyedStringExpression(
                new String[]{"alpha", "beta", "gamma"},
                new String[]{"one", "two", "three"}
        )}, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new Object[]{"gamma", "beta", "alpha"}, reversed.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"three", "two", "one"}, collectKeys(reversed.keyedIterator(SkriptEvent.EMPTY)));

        ExprShuffledList shuffled = new ExprShuffledList();
        shuffled.init(new Expression[]{new KeyedObjectExpression(
                new Object[]{1, 2, 3},
                new String[]{"one", "two", "three"}
        )}, 0, Kleenean.FALSE, parseResult(""));
        Object[] shuffledValues = shuffled.getArray(SkriptEvent.EMPTY);
        assertEquals(3, shuffledValues.length);
        assertTrue(Arrays.asList(shuffledValues).containsAll(List.of(1, 2, 3)));
        assertEquals(3, collectKeys(shuffled.keyedIterator(SkriptEvent.EMPTY)).length);

    }

    @Test
    void joinSplitDefaultValueAndIndicesMatchLegacyBehaviors() {
        ExprJoinSplit join = new ExprJoinSplit();
        join.init(new Expression[]{
                new SimpleLiteral<>(new String[]{"alpha", "beta", "gamma"}, String.class, true),
                new SimpleLiteral<>(" | ", false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"alpha | beta | gamma"}, join.getArray(SkriptEvent.EMPTY));

        ExprJoinSplit split = new ExprJoinSplit();
        split.init(new Expression[]{
                new SimpleLiteral<>("Alpha--beta--", false),
                new SimpleLiteral<>("--", false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"Alpha", "beta", ""}, split.getArray(SkriptEvent.EMPTY));

        ExprJoinSplit caseInsensitiveSplit = new ExprJoinSplit();
        caseInsensitiveSplit.init(new Expression[]{
                new SimpleLiteral<>("oneXtwoxtHree", false),
                new SimpleLiteral<>("x", false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"one", "two", "tHree"}, caseInsensitiveSplit.getArray(SkriptEvent.EMPTY));

        ExprJoinSplit regexSplit = new ExprJoinSplit();
        SkriptParser.ParseResult regexParse = parseResult("");
        regexParse.tags.add("trailing");
        regexSplit.init(new Expression[]{
                new SimpleLiteral<>("1,  2,3,", false),
                new SimpleLiteral<>(",\\s*", false)
        }, 3, Kleenean.FALSE, regexParse);
        assertArrayEquals(new String[]{"1", "2", "3"}, regexSplit.getArray(SkriptEvent.EMPTY));

        ExprDefaultValue defaultValue = new ExprDefaultValue();
        defaultValue.init(new Expression[]{
                new SimpleLiteral<>(new Object[0], Object.class, true),
                new SimpleLiteral<>("fallback", false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("fallback", defaultValue.getSingle(SkriptEvent.EMPTY));
        assertEquals(Object.class, defaultValue.getReturnType());

        ExprIndicesOfValue firstStringPosition = new ExprIndicesOfValue();
        SkriptParser.ParseResult firstParse = parseResult("");
        firstParse.mark = 1;
        firstStringPosition.init(new Expression[]{
                new SimpleLiteral<>("abc", false),
                new SimpleLiteral<>("abcabcABC", false)
        }, 0, Kleenean.FALSE, firstParse);
        assertArrayEquals(new Long[]{1L}, firstStringPosition.getArray(SkriptEvent.EMPTY));

        ExprIndicesOfValue allListPositions = new ExprIndicesOfValue();
        SkriptParser.ParseResult allPositionsParse = parseResult("");
        allPositionsParse.mark = 3;
        allListPositions.init(new Expression[]{
                new SimpleLiteral<>("beta", false),
                new SimpleLiteral<>(new String[]{"alpha", "beta", "gamma", "beta"}, String.class, true)
        }, 1, Kleenean.FALSE, allPositionsParse);
        assertArrayEquals(new Long[]{2L, 4L}, allListPositions.getArray(SkriptEvent.EMPTY));

        ExprIndicesOfValue keyedIndices = new ExprIndicesOfValue();
        SkriptParser.ParseResult keyedIndicesParse = parseResult("");
        keyedIndicesParse.mark = 3;
        keyedIndices.init(new Expression[]{
                new SimpleLiteral<>(100, false),
                new KeyedProviderExpression(new Object[]{100, "skip", 100}, new String[]{"first", "second", "third"})
        }, 2, Kleenean.FALSE, keyedIndicesParse);
        assertArrayEquals(new String[]{"first", "third"}, keyedIndices.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void randomUuidProducesSingleValueAndWhetherParsesConditionBodies() {
        ExprRandomUUID randomUuid = new ExprRandomUUID();
        randomUuid.init(new Expression[0], 0, Kleenean.FALSE, parseResult(""));
        UUID first = randomUuid.getSingle(SkriptEvent.EMPTY);
        UUID second = randomUuid.getSingle(SkriptEvent.EMPTY);
        assertTrue(randomUuid.isSingle());
        assertNotEquals(first, second);

        Skript.registerCondition(TestCondition.class, "test condition");
        ExprWhether whether = new ExprWhether();
        Matcher matcher = Pattern.compile(".+").matcher("test condition");
        assertTrue(matcher.find());
        SkriptParser.ParseResult parse = parseResult("");
        parse.regexes = List.of(matcher);

        assertTrue(whether.init(new Expression[0], 0, Kleenean.FALSE, parse));
        assertEquals(Boolean.TRUE, whether.getSingle(SkriptEvent.EMPTY));
        assertEquals("whether test condition", whether.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void randomNumberSupportsIntegerDoubleAndAmountEdgeCases() {
        ExprRandomNumber singleInteger = new ExprRandomNumber();
        SkriptParser.ParseResult integerParse = parseResult("");
        integerParse.tags.add("integer");
        singleInteger.init(new Expression[]{
                null,
                new SimpleLiteral<>(5, false),
                new SimpleLiteral<>(5, false)
        }, 0, Kleenean.FALSE, integerParse);
        assertEquals(5L, singleInteger.getSingle(SkriptEvent.EMPTY));
        assertTrue(singleInteger.isSingle());
        assertEquals(Long.class, singleInteger.getReturnType());

        ExprRandomNumber multipleIntegers = new ExprRandomNumber();
        SkriptParser.ParseResult multipleParse = parseResult("");
        multipleParse.tags.add("integer");
        multipleIntegers.init(new Expression[]{
                new SimpleLiteral<>(3, false),
                new SimpleLiteral<>(1, false),
                new SimpleLiteral<>(1, false)
        }, 0, Kleenean.FALSE, multipleParse);
        assertArrayEquals(new Number[]{1L, 1L, 1L}, multipleIntegers.getArray(SkriptEvent.EMPTY));
        assertFalse(multipleIntegers.isSingle());

        ExprRandomNumber decimal = new ExprRandomNumber();
        decimal.init(new Expression[]{
                null,
                new SimpleLiteral<>(2.5, false),
                new SimpleLiteral<>(2.5, false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(2.5D, decimal.getSingle(SkriptEvent.EMPTY));
        assertEquals(Double.class, decimal.getReturnType());

        ExprRandomNumber impossibleIntegerRange = new ExprRandomNumber();
        SkriptParser.ParseResult impossibleParse = parseResult("");
        impossibleParse.tags.add("integer");
        impossibleIntegerRange.init(new Expression[]{
                null,
                new SimpleLiteral<>(0.5, false),
                new SimpleLiteral<>(0.6, false)
        }, 0, Kleenean.FALSE, impossibleParse);
        assertArrayEquals(new Number[0], impossibleIntegerRange.getArray(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static String[] collectKeys(Iterator<? extends KeyedValue<?>> iterator) {
        List<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            keys.add(iterator.next().key());
        }
        return keys.toArray(String[]::new);
    }

    private static final class KeyedStringExpression extends SimpleExpression<String> implements KeyedIterableExpression<String> {

        private final String[] values;
        private final String[] keys;

        private KeyedStringExpression(String[] values, String[] keys) {
            this.values = values;
            this.keys = keys;
        }

        @Override
        protected String @Nullable [] get(SkriptEvent event) {
            return values.clone();
        }

        @Override
        public boolean canIterateWithKeys() {
            return true;
        }

        @Override
        public Iterator<KeyedValue<String>> keyedIterator(SkriptEvent event) {
            return List.of(KeyedValue.zip(values, keys)).iterator();
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public boolean isSingle() {
            return false;
        }
    }

    private static final class KeyedObjectExpression extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {

        private final Object[] values;
        private final String[] keys;

        private KeyedObjectExpression(Object[] values, String[] keys) {
            this.values = values;
            this.keys = keys;
        }

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return values.clone();
        }

        @Override
        public boolean canIterateWithKeys() {
            return true;
        }

        @Override
        public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
            return List.of(KeyedValue.zip(values, keys)).iterator();
        }

        @Override
        public Class<? extends Object> getReturnType() {
            return Number.class;
        }

        @Override
        public boolean isSingle() {
            return false;
        }
    }

    private static final class KeyedProviderExpression extends SimpleExpression<Object> implements KeyProviderExpression<Object> {

        private final Object[] values;
        private final String[] keys;

        private KeyedProviderExpression(Object[] values, String[] keys) {
            this.values = values;
            this.keys = keys;
        }

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return values.clone();
        }

        @Override
        public String[] getArrayKeys(SkriptEvent event) {
            return keys.clone();
        }

        @Override
        public Class<? extends Object> getReturnType() {
            return Object.class;
        }
    }

    public static final class TestCondition extends ch.njol.skript.lang.Condition {

        @Override
        public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "test condition";
        }
    }
}
