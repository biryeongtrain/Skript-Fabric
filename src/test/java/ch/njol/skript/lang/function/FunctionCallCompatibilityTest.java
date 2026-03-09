package ch.njol.skript.lang.function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class FunctionCallCompatibilityTest {

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
    }

    @Test
    void functionReferenceParsesValidatesAndExecutes() {
        EchoFunction function = registerEchoFunction();

        FunctionReference<?> reference = FunctionReference.parse("echo(\"abc\")", null, new Class[]{String.class});
        assertNotNull(reference);
        assertTrue(reference.validateFunction(true));

        Object[] result = reference.execute(SkriptEvent.EMPTY);
        assertNotNull(result);
        assertArrayEquals(new Object[]{"abc"}, result);
        assertEquals(1, function.calls.get());
    }

    @Test
    void exprFunctionCallConvertsValuesAndExposesKeys() {
        registerEchoFunction();
        FunctionReference<String> reference = new FunctionReference<>(
                "echo",
                null,
                new Class[]{String.class},
                new Expression[]{new SimpleLiteral<>("xyz", false)}
        );
        assertTrue(reference.validateFunction(true));

        ExprFunctionCall<String> expression = new ExprFunctionCall<>(reference);
        String[] values = expression.getArray(SkriptEvent.EMPTY);
        assertArrayEquals(new String[]{"xyz"}, values);
        assertArrayEquals(new String[]{"arg0"}, expression.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void effectFunctionCallRunsAndIgnoresReturnValue() {
        EchoFunction function = registerEchoFunction();
        EffFunctionCall effect = EffFunctionCall.parse("echo(\"run\")");

        assertNotNull(effect);
        effect.execute(SkriptEvent.EMPTY);
        assertEquals(1, function.calls.get());
    }

    @Test
    void functionReferenceParseUnescapesDoubledQuotesInStringLiteralArguments() {
        registerEchoFunction();

        FunctionReference<?> reference = FunctionReference.parse("echo(\"a \"\"b\"\" c\")", null, new Class[]{String.class});

        assertNotNull(reference);
        assertTrue(reference.validateFunction(true));
        Object[] result = reference.execute(SkriptEvent.EMPTY);
        assertNotNull(result);
        assertArrayEquals(new Object[]{"a \"b\" c"}, result);
    }

    @Test
    void dynamicFunctionReferenceResolvesAndExecutes() {
        registerEchoFunction();

        DynamicFunctionReference<?> reference = new DynamicFunctionReference<>("echo");
        assertTrue(reference.valid());

        Object[] values = reference.execute(SkriptEvent.EMPTY, "hello");
        assertNotNull(values);
        assertArrayEquals(new Object[]{"hello"}, values);

        Expression<?> validated = reference.validate(new Expression[]{new SimpleLiteral<>("value", false)});
        assertNotNull(validated);
    }

    @Test
    void dynamicFunctionReferenceValidationCacheDistinguishesExpressionShape() {
        registerEchoFunction();

        DynamicFunctionReference<?> reference = new DynamicFunctionReference<>("echo");
        Expression<?> single = new SimpleLiteral<>("value", false);
        Expression<?> plural = new PluralStringExpression("value");

        assertNotNull(reference.validate(new Expression[]{single}));
        assertSame(single.getReturnType(), plural.getReturnType());
        assertTrue(!plural.isSingle());
        assertNull(reference.validate(new Expression[]{plural}));
    }

    @Test
    void dynamicFunctionReferenceResolvesLocalFunctionUsingSourceScript() {
        registerLocalEchoFunction("local.sk", "localEcho");

        DynamicFunctionReference<?> reference = DynamicFunctionReference.resolveFunction("localEcho", "local.sk");
        assertNotNull(reference);
        assertTrue(reference.valid());

        Object[] values = reference.execute(SkriptEvent.EMPTY, "local");
        assertNotNull(values);
        assertArrayEquals(new Object[]{"local"}, values);
    }

    @Test
    void functionReferencePrefersLocalSignatureOverCompatibleGlobalCandidate() {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);

        Signature<String> globalSignature = new Signature<>(
                null,
                "shadowedEcho",
                new Parameter[]{new Parameter<>("value", Classes.getSuperClassInfo(Object.class), true, null)},
                false,
                stringInfo,
                true
        );
        EchoFunction globalFunction = new EchoFunction(globalSignature);
        Functions.register(globalFunction);

        Signature<String> localSignature = new Signature<>(
                "local.sk",
                "shadowedEcho",
                new Parameter[]{new Parameter<>("value", stringInfo, true, null)},
                true,
                stringInfo,
                true
        );
        EchoFunction localFunction = new EchoFunction(localSignature);
        assertNotNull(Functions.registerSignature(localSignature));
        Namespace namespace = Functions.getScriptNamespace("local.sk");
        assertNotNull(namespace);
        namespace.addFunction(localFunction);
        FunctionRegistry.getRegistry().register("local.sk", localFunction);

        FunctionReference<String> reference = new FunctionReference<>(
                "shadowedEcho",
                "local.sk",
                new Class[]{String.class},
                new Expression[]{new SimpleLiteral<>("local", false)}
        );

        assertTrue(reference.validateFunction(true));
        assertSame(localSignature, reference.getRegisteredSignature());
        assertSame(localFunction, reference.function());
    }

    @Test
    void parseFunctionResolvesStringifiedLocalReference() {
        registerLocalEchoFunction("local.sk", "stringifiedLocal");

        DynamicFunctionReference<?> reference = DynamicFunctionReference.parseFunction("stringifiedLocal() from local.sk");
        assertNotNull(reference);
        assertTrue(reference.valid());
    }

    @Test
    void dynamicLocalFunctionReferenceRetainsSourceScriptInStringForm() {
        registerLocalEchoFunction("local.sk", "stringifiedLocal");

        DynamicFunctionReference<?> reference = DynamicFunctionReference.resolveFunction("stringifiedLocal", "local.sk");

        assertNotNull(reference);
        assertEquals("stringifiedLocal() from local.sk", reference.toString());
    }

    @Test
    void functionReferenceTracksValidatedSignatureCalls() {
        EchoFunction function = registerEchoFunction();

        FunctionReference<?> reference = new FunctionReference<>(
                "echo",
                "caller.sk",
                new Class[]{String.class},
                new Expression[]{new SimpleLiteral<>("track", false)}
        );

        assertTrue(reference.validateFunction(true));
        assertSame(function.getSignature(), reference.getRegisteredSignature());
        assertTrue(function.getSignature().calls().contains(reference));
    }

    @Test
    void functionReferenceMergesArgumentsForSinglePluralParameter() {
        registerPluralEchoFunction("collect");

        FunctionReference<String> reference = new FunctionReference<>(
                "collect",
                null,
                null,
                new Expression[]{
                        new SimpleLiteral<>("a", false),
                        new SimpleLiteral<>("b", false),
                        new SimpleLiteral<>("c", false)
                }
        );

        assertTrue(reference.validateFunction(true));
        String[] values = reference.execute(SkriptEvent.EMPTY);
        assertNotNull(values);
        assertArrayEquals(new String[]{"a", "b", "c"}, values);
    }

    @Test
    void exprFunctionCallPreservesKeysForSinglePluralParameter() {
        registerPluralEchoFunction("collectKeys");

        FunctionReference<String> reference = new FunctionReference<>(
                "collectKeys",
                null,
                null,
                new Expression[]{
                        new FixedKeyExpression(new String[]{"x", "y"}, new String[]{"k1", "k2"})
                }
        );

        assertTrue(reference.validateFunction(true));
        ExprFunctionCall<String> expression = new ExprFunctionCall<>(reference);

        assertArrayEquals(new String[]{"x", "y"}, expression.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"k1", "k2"}, expression.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void keyedFunctionArgumentsPreserveProvidedKeysOutsideSingleListShortcut() {
        registerPluralAndSingleEchoFunction("collectKeysWithSuffix");

        FunctionReference<String> reference = new FunctionReference<>(
                "collectKeysWithSuffix",
                null,
                null,
                new Expression[]{
                        new FixedKeyExpression(new String[]{"x", "y"}, new String[]{"k1", "k2"}),
                        new SimpleLiteral<>("tail", false)
                }
        );

        assertTrue(reference.validateFunction(true));
        ExprFunctionCall<String> expression = new ExprFunctionCall<>(reference);

        assertArrayEquals(new String[]{"x", "y"}, expression.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"k1", "k2"}, expression.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void keyedFunctionArgumentsReceiveClonedArrayValues() {
        ArrayMutatingFunction function = registerArrayMutatingFunction("mutateArrays");
        int[] original = {1, 2};

        FunctionReference<?> reference = new FunctionReference<>(
                "mutateArrays",
                null,
                null,
                new Expression[]{new ArrayValueExpression(original)}
        );

        assertTrue(reference.validateFunction(true));
        reference.execute(SkriptEvent.EMPTY);

        assertArrayEquals(new int[]{1, 2}, original);
        assertNotSame(original, function.observedArray);
        assertArrayEquals(new int[]{99, 2}, function.observedArray);
    }

    private static EchoFunction registerEchoFunction() {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Signature<String> signature = new Signature<>(
                null,
                "echo",
                new Parameter[]{new Parameter<>("value", stringInfo, true, null)},
                false,
                stringInfo,
                true
        );
        EchoFunction function = new EchoFunction(signature);
        Functions.register(function);
        return function;
    }

    private static ListEchoFunction registerPluralAndSingleEchoFunction(String name) {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Signature<String> signature = new Signature<>(
                null,
                name,
                new Parameter[]{
                        new Parameter<>("values", stringInfo, false, null),
                        new Parameter<>("suffix", stringInfo, true, null)
                },
                false,
                stringInfo,
                false
        );
        ListEchoFunction function = new ListEchoFunction(signature);
        Functions.register(function);
        return function;
    }

    private static EchoFunction registerLocalEchoFunction(String script, String name) {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Signature<String> signature = new Signature<>(
                script,
                name,
                new Parameter[]{new Parameter<>("value", stringInfo, true, null)},
                true,
                stringInfo,
                true
        );
        EchoFunction function = new EchoFunction(signature);
        Functions.registerSignature(signature);
        Namespace namespace = Functions.getScriptNamespace(script);
        assertNotNull(namespace);
        namespace.addFunction(function);
        FunctionRegistry.getRegistry().register(script, function);
        return function;
    }

    private static ListEchoFunction registerPluralEchoFunction(String name) {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Signature<String> signature = new Signature<>(
                null,
                name,
                new Parameter[]{new Parameter<>("values", stringInfo, false, null)},
                false,
                stringInfo,
                false
        );
        ListEchoFunction function = new ListEchoFunction(signature);
        Functions.register(function);
        return function;
    }

    private static ArrayMutatingFunction registerArrayMutatingFunction(String name) {
        ClassInfo<Object> objectInfo = Classes.getSuperClassInfo(Object.class);
        Signature<Object> signature = new Signature<>(
                null,
                name,
                new Parameter[]{new Parameter<>("values", objectInfo, false, null)},
                false,
                null,
                true
        );
        ArrayMutatingFunction function = new ArrayMutatingFunction(signature);
        Functions.register(function);
        return function;
    }

    private static class EchoFunction extends Function<String> {

        private final AtomicInteger calls = new AtomicInteger();
        private String[] returnedKeys;

        private EchoFunction(Signature<String> signature) {
            super(signature);
        }

        @Override
        public String[] execute(FunctionEvent<?> event, Object[][] params) {
            calls.incrementAndGet();
            if (params.length == 0 || params[0] == null || params[0].length == 0) {
                returnedKeys = new String[0];
                return new String[0];
            }
            String[] values = new String[params[0].length];
            String[] keys = new String[params[0].length];
            for (int i = 0; i < params[0].length; i++) {
                values[i] = String.valueOf(params[0][i]);
                keys[i] = "arg" + i;
            }
            returnedKeys = keys;
            return values;
        }

        @Override
        public String[] returnedKeys() {
            return returnedKeys;
        }

        @Override
        public boolean resetReturnValue() {
            returnedKeys = null;
            return true;
        }
    }

    private static final class ListEchoFunction extends Function<String> {

        private String[] returnedKeys;

        private ListEchoFunction(Signature<String> signature) {
            super(signature);
        }

        @Override
        public String[] execute(FunctionEvent<?> event, Object[][] params) {
            if (params.length == 0 || params[0] == null) {
                returnedKeys = new String[0];
                return new String[0];
            }
            Object[] source = params[0];
            String[] values = new String[source.length];
            String[] keys = new String[source.length];
            for (int i = 0; i < source.length; i++) {
                Object value = source[i];
                if (value instanceof KeyedValue<?> keyedValue) {
                    values[i] = String.valueOf(keyedValue.value());
                    keys[i] = keyedValue.key();
                } else {
                    values[i] = String.valueOf(value);
                    keys[i] = String.valueOf(i + 1);
                }
            }
            returnedKeys = keys;
            return values;
        }

        @Override
        public String[] returnedKeys() {
            return returnedKeys;
        }

        @Override
        public boolean resetReturnValue() {
            returnedKeys = null;
            return true;
        }
    }

    private static final class ArrayMutatingFunction extends Function<Object> {

        private int[] observedArray;

        private ArrayMutatingFunction(Signature<Object> signature) {
            super(signature);
        }

        @Override
        public Object[] execute(FunctionEvent<?> event, Object[][] params) {
            KeyedValue<?> keyedValue = (KeyedValue<?>) params[0][0];
            observedArray = (int[]) keyedValue.value();
            observedArray[0] = 99;
            return null;
        }

        @Override
        public boolean resetReturnValue() {
            observedArray = null;
            return true;
        }
    }

    private static final class FixedKeyExpression extends SimpleExpression<String> implements KeyProviderExpression<String> {

        private final String[] values;
        private final String[] keys;

        private FixedKeyExpression(String[] values, String[] keys) {
            this.values = values;
            this.keys = keys;
        }

        @Override
        protected String[] get(SkriptEvent event) {
            return values.clone();
        }

        @Override
        public String[] getArrayKeys(SkriptEvent event) {
            return keys.clone();
        }

        @Override
        public boolean areKeysRecommended() {
            return true;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }
    }

    private static final class PluralStringExpression extends SimpleExpression<String> {

        private final String[] values;

        private PluralStringExpression(String... values) {
            this.values = values;
        }

        @Override
        protected String[] get(SkriptEvent event) {
            return values.clone();
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }
    }

    private static final class ArrayValueExpression extends SimpleExpression<Object> {

        private final Object value;

        private ArrayValueExpression(Object value) {
            this.value = value;
        }

        @Override
        protected Object[] get(SkriptEvent event) {
            return new Object[]{value};
        }

        @Override
        public Class<? extends Object> getReturnType() {
            return Object.class;
        }
    }
}
