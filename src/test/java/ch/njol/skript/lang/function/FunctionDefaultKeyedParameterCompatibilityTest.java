package ch.njol.skript.lang.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Upstream-backed regression: for a keyed (plural) parameter that uses a default expression,
 * upstream only zips to keyed pairs when that default yields a single value. If the default
 * yields multiple values, they remain un-keyed. Our local implementation zipped all defaults,
 * which diverged from upstream behavior.
 */
class FunctionDefaultKeyedParameterCompatibilityTest {

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
    }

    @Test
    void keyedPluralDefaultWithMultipleValuesRemainsUnkeyed() {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Expression<String> multiDefault = new SimpleExpression<>() {
            @Override
            protected String[] get(SkriptEvent event) {
                return new String[]{"a", "b"};
            }

            @Override
            public boolean isSingle() {
                return false;
            }

            @Override
            public Class<? extends String> getReturnType() {
                return String.class;
            }

            @Override
            public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
                return true;
            }
        };

        Parameter<String> param = new Parameter<>("values", stringInfo, false, multiDefault);
        Signature<String> sig = new Signature<>(null, "defKeyedMany", new Parameter[]{param}, false, stringInfo, false);
        ObservingFunction fn = new ObservingFunction(sig);

        fn.execute(new Object[][]{});

        assertFalse(fn.observedKeyed, "default with multiple values should not be zipped to KeyedValue[]");
        assertEquals(2, fn.observedLength);
    }

    @Test
    void keyedPluralDefaultWithSingleValueBecomesKeyed() {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Expression<String> singleDefault = new SimpleExpression<>() {
            @Override
            protected String[] get(SkriptEvent event) {
                return new String[]{"x"};
            }

            @Override
            public boolean isSingle() {
                return true;
            }

            @Override
            public Class<? extends String> getReturnType() {
                return String.class;
            }

            @Override
            public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
                return true;
            }
        };

        Parameter<String> param = new Parameter<>("values", stringInfo, false, singleDefault);
        Signature<String> sig = new Signature<>(null, "defKeyedOne", new Parameter[]{param}, false, stringInfo, false);
        ObservingFunction fn = new ObservingFunction(sig);

        fn.execute(new Object[][]{});

        assertTrue(fn.observedKeyed, "single default value should be zipped to KeyedValue[]");
        assertEquals(1, fn.observedLength);
        assertEquals("1", fn.firstKey);
        assertEquals("x", fn.firstValue);
    }

    private static final class ObservingFunction extends Function<String> {
        boolean observedKeyed;
        int observedLength;
        String firstKey;
        String firstValue;

        private ObservingFunction(Signature<String> signature) {
            super(signature);
        }

        @Override
        public String[] execute(FunctionEvent<?> event, Object[][] params) {
            Object[] p = params[0];
            observedLength = p == null ? 0 : p.length;
            if (p != null && p.length > 0 && p[0] instanceof KeyedValue<?> kv) {
                observedKeyed = true;
                firstKey = kv.key();
                firstValue = String.valueOf(kv.value());
            } else {
                observedKeyed = false;
            }
            return null;
        }

        @Override
        public boolean resetReturnValue() {
            return true;
        }
    }
}

