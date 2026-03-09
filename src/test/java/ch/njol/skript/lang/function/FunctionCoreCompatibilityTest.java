package ch.njol.skript.lang.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import java.util.Arrays;
import java.util.List;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class FunctionCoreCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
    }

    @Test
    void parameterParsesDefaultAndOptionalModifier() {
        ClassInfo<Integer> info = Classes.getSuperClassInfo(Integer.class);
        Parameter<Integer> parameter = Parameter.newInstance("count", info, true, "7");

        assertNotNull(parameter);
        assertTrue(parameter.isOptional());
        assertEquals(7, parameter.getDefaultExpression().getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
    }

    @Test
    void parameterParsesRegisteredExpressionAsDefaultValue() {
        Skript.registerExpression(FunctionDefaultNumberExpression.class, Integer.class, "function default number");

        Parameter<Integer> parameter = Parameter.newInstance(
                "count",
                Classes.getSuperClassInfo(Integer.class),
                true,
                "function default number"
        );

        assertNotNull(parameter);
        assertNotNull(parameter.getDefaultExpression());
        assertEquals(9, parameter.getDefaultExpression().getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parameterParseKeepsCommasInsideQuotedDefaultExpressions() {
        List<Parameter<?>> parameters = Parameter.parse("message: string = \"a, b\", count: number");

        assertNotNull(parameters);
        assertEquals(2, parameters.size());
        assertNotNull(parameters.get(0).getDefaultExpression());
        assertEquals("a, b", parameters.get(0).getDefaultExpression().getSingle(SkriptEvent.EMPTY));
        assertEquals("count", parameters.get(1).name());
    }

    @Test
    void parameterParseRejectsDuplicateNamesCaseInsensitively() {
        boolean previous = Variables.caseInsensitiveVariables;
        Variables.caseInsensitiveVariables = true;
        try {
            assertNull(Parameter.parse("Value: number, value: number"));
        } finally {
            Variables.caseInsensitiveVariables = previous;
        }
    }

    @Test
    void signatureTracksRequiredAndOptionalArity() {
        Parameter<Integer> required = new Parameter<>("a", Classes.getSuperClassInfo(Integer.class), true, null);
        Parameter<Integer> optional = new Parameter<>("b", Classes.getSuperClassInfo(Integer.class), true, new ch.njol.skript.lang.util.SimpleLiteral<>(1, true));
        Signature<Integer> signature = new Signature<>(
                null,
                "calc",
                new Parameter[]{required, optional},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );

        assertEquals(2, signature.getMaxParameters());
        assertEquals(1, signature.getMinParameters());
    }

    @Test
    void functionExecuteAppliesDefaultValues() {
        Parameter<Integer> optional = new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, new ch.njol.skript.lang.util.SimpleLiteral<>(3, true));
        Signature<Integer> signature = new Signature<>(
                null,
                "f",
                new Parameter[]{optional},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction function = new RecordingFunction(signature);

        Integer[] result = function.execute(new Object[][]{});

        assertNotNull(result);
        assertEquals(3, result[0]);
        assertNotNull(function.lastParams);
        assertEquals(3, function.lastParams[0][0]);
    }

    @Test
    void functionExecuteDoesNotReplaceExplicitEmptyArgumentWithDefault() {
        Parameter<Integer> optional = new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, new ch.njol.skript.lang.util.SimpleLiteral<>(3, true));
        Signature<Integer> signature = new Signature<>(
                null,
                "f",
                new Parameter[]{optional},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction function = new RecordingFunction(signature);

        Integer[] result = function.execute(new Object[][]{new Object[0]});

        assertNull(result);
        assertNotNull(function.lastParams);
        assertEquals(0, function.lastParams[0].length);
    }

    @Test
    void functionExecuteAllowsNullSlotsWhenExecuteWithNullsIsDisabled() {
        boolean previous = Function.executeWithNulls;
        Function.executeWithNulls = false;
        try {
            Signature<Integer> signature = new Signature<>(
                    null,
                    "f",
                    new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                    false,
                    Classes.getSuperClassInfo(Integer.class),
                    true
            );
            Function<Integer> function = new Function<>(signature) {
                @Override
                public Integer[] execute(FunctionEvent<?> event, Object[][] params) {
                    return new Integer[]{params[0] == null ? 7 : 0};
                }

                @Override
                public boolean resetReturnValue() {
                    return true;
                }
            };

            Integer[] result = function.execute(new Object[][]{null});

            assertNotNull(result);
            assertEquals(7, result[0]);
        } finally {
            Function.executeWithNulls = previous;
        }
    }

    @Test
    void functionRegistryResolvesExactAndAmbiguousFunctions() {
        FunctionRegistry registry = FunctionRegistry.getRegistry();
        registry.clear();

        Signature<Integer> globalInt = new Signature<>(
                null,
                "sum",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction globalFunction = new RecordingFunction(globalInt);
        registry.register(null, globalInt);
        registry.register(null, globalFunction);

        Signature<Integer> localString = new Signature<>(
                "script.sk",
                "sum",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(String.class), true, null)},
                true,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction localFunction = new RecordingFunction(localString);
        registry.register("script.sk", localString);
        registry.register("script.sk", localFunction);

        FunctionRegistry.Retrieval<Function<?>> localRetrieval = registry.getExactFunction("script.sk", "sum", String.class);
        assertEquals(FunctionRegistry.RetrievalResult.EXACT, localRetrieval.result());
        assertEquals(localFunction, localRetrieval.retrieved());

        FunctionRegistry.Retrieval<Function<?>> fallbackRetrieval = registry.getExactFunction("script.sk", "sum", Integer.class);
        assertEquals(FunctionRegistry.RetrievalResult.EXACT, fallbackRetrieval.result());
        assertEquals(globalFunction, fallbackRetrieval.retrieved());

        Signature<Integer> ambiguousA = new Signature<>(
                null,
                "dup",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Number.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        Signature<Integer> ambiguousB = new Signature<>(
                null,
                "dup",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Object.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        registry.register(null, ambiguousA);
        registry.register(null, new RecordingFunction(ambiguousA));
        registry.register(null, ambiguousB);
        registry.register(null, new RecordingFunction(ambiguousB));

        FunctionRegistry.Retrieval<Function<?>> ambiguous = registry.getFunction(null, "dup", Integer.class);
        assertEquals(FunctionRegistry.RetrievalResult.AMBIGUOUS, ambiguous.result());
        assertNull(ambiguous.retrieved());
        assertNotNull(ambiguous.conflictingArgs());
    }

    @Test
    void functionRegistryPrefersLocalMatchBeforeGlobalCandidates() {
        FunctionRegistry registry = FunctionRegistry.getRegistry();
        registry.clear();

        Signature<Integer> localSignature = new Signature<>(
                "script.sk",
                "preferLocal",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                true,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction localFunction = new RecordingFunction(localSignature);
        registry.register("script.sk", localSignature);
        registry.register("script.sk", localFunction);

        Signature<Integer> globalSignature = new Signature<>(
                null,
                "preferLocal",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Number.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction globalFunction = new RecordingFunction(globalSignature);
        registry.register(null, globalSignature);
        registry.register(null, globalFunction);

        FunctionRegistry.Retrieval<Signature<?>> signatureRetrieval = registry.getSignature("script.sk", "preferLocal", Integer.class);
        assertEquals(FunctionRegistry.RetrievalResult.EXACT, signatureRetrieval.result());
        assertSame(localSignature, signatureRetrieval.retrieved());

        FunctionRegistry.Retrieval<Function<?>> functionRetrieval = registry.getFunction("script.sk", "preferLocal", Integer.class);
        assertEquals(FunctionRegistry.RetrievalResult.EXACT, functionRetrieval.result());
        assertSame(localFunction, functionRetrieval.retrieved());
    }

    @Test
    void functionsFacadeRegistersGlobalAndLocalSignature() {
        Functions.clear();

        Signature<Integer> globalSignature = new Signature<>(
                null,
                "hello",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction function = new RecordingFunction(globalSignature);
        Functions.register(function);

        assertNotNull(Functions.getGlobalFunction("hello"));
        assertNotNull(Functions.getGlobalSignature("hello"));

        Signature<Integer> localSignature = new Signature<>(
                "script.sk",
                "helloLocal",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                true,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        Signature<?> registered = Functions.registerSignature(localSignature);
        assertNotNull(registered);
        assertNotNull(Functions.getLocalSignature("helloLocal", "script.sk"));
    }

    @Test
    void functionsFacadeAllowsOverloadedScriptSignatures() {
        Functions.clear();

        Signature<Integer> numberSignature = new Signature<>(
                "script.sk",
                "overloaded",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        Signature<Integer> textSignature = new Signature<>(
                "script.sk",
                "overloaded",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(String.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );

        assertSame(numberSignature, Functions.registerSignature(numberSignature));
        assertSame(textSignature, Functions.registerSignature(textSignature));
        assertEquals(
                FunctionRegistry.RetrievalResult.EXACT,
                FunctionRegistry.getRegistry().getExactSignature(null, "overloaded", Integer.class).result()
        );
        assertEquals(
                FunctionRegistry.RetrievalResult.EXACT,
                FunctionRegistry.getRegistry().getExactSignature(null, "overloaded", String.class).result()
        );
    }

    @Test
    void localLookupFallsBackToGlobalMembersInSameScriptNamespace() {
        Functions.clear();

        Signature<Integer> globalSignature = new Signature<>(
                "script.sk",
                "shared",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction function = new RecordingFunction(globalSignature);

        assertNotNull(Functions.registerSignature(globalSignature));
        Namespace namespace = Functions.getScriptNamespace("script.sk");
        assertNotNull(namespace);
        namespace.addFunction(function);
        FunctionRegistry.getRegistry().register(null, function);

        assertSame(globalSignature, Functions.getLocalSignature("shared", "script.sk"));
        assertSame(function, Functions.getLocalFunction("shared", "script.sk"));
    }

    @Test
    void unregisterFunctionRemovesRegisteredSignatureAndImplementation() {
        Functions.clear();

        Signature<Integer> globalSignature = new Signature<>(
                "script.sk",
                "removeMe",
                new Parameter[]{new Parameter<>("x", Classes.getSuperClassInfo(Integer.class), true, null)},
                false,
                Classes.getSuperClassInfo(Integer.class),
                true
        );
        RecordingFunction function = new RecordingFunction(globalSignature);

        assertNotNull(Functions.registerSignature(globalSignature));
        Namespace namespace = Functions.getScriptNamespace("script.sk");
        assertNotNull(namespace);
        namespace.addFunction(function);
        FunctionRegistry.getRegistry().register(null, function);

        Functions.unregisterFunction(globalSignature);

        assertNull(Functions.getGlobalSignature("removeMe"));
        assertNull(Functions.getGlobalFunction("removeMe"));
        assertEquals(
                FunctionRegistry.RetrievalResult.NOT_REGISTERED,
                FunctionRegistry.getRegistry().getExactSignature(null, "removeMe", Integer.class).result()
        );
        assertEquals(
                FunctionRegistry.RetrievalResult.NOT_REGISTERED,
                FunctionRegistry.getRegistry().getExactFunction(null, "removeMe", Integer.class).result()
        );
    }

    private static class RecordingFunction extends Function<Integer> {

        private @Nullable Object[][] lastParams;

        private RecordingFunction(Signature<Integer> signature) {
            super(signature);
        }

        @Override
        public Integer[] execute(FunctionEvent<?> event, Object[][] params) {
            this.lastParams = params;
            if (params.length == 0 || params[0] == null || params[0].length == 0) {
                return new Integer[0];
            }
            Object value = params[0][0];
            if (value instanceof Number number) {
                return new Integer[]{number.intValue()};
            }
            if (value instanceof String string) {
                return new Integer[]{string.length()};
            }
            return new Integer[]{0};
        }

        @Override
        public boolean resetReturnValue() {
            return true;
        }
    }

    public static class FunctionDefaultNumberExpression extends SimpleExpression<Integer> {

        @Override
        protected Integer @Nullable [] get(SkriptEvent event) {
            return new Integer[]{9};
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ParseResult parseResult
        ) {
            return expressions.length == 0;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "function default number";
        }
    }
}
