package org.skriptlang.skript.common.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.registrations.Classes;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.event.SkriptEvent;

class CommonFunctionCompatibilityTest {

    private java.util.List<ClassInfo<?>> savedClassInfos = java.util.List.of();

    @BeforeEach
    void resetClassInfos() {
        savedClassInfos = java.util.List.copyOf(Classes.getClassInfos());
        Classes.clearClassInfos();
    }

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
        Classes.clearClassInfos();
        for (ClassInfo<?> classInfo : savedClassInfos) {
            Classes.registerClassInfo(classInfo);
        }
        savedClassInfos = java.util.List.of();
    }

    @Test
    void functionArgumentsSupportTypedAccessors() {
        FunctionArguments arguments = new FunctionArgumentsImpl(Map.of("count", 3, "label", "ok"));

        assertEquals(3, arguments.<Integer>get("count"));
        assertEquals("ok", arguments.getOrDefault("label", "fallback"));
        assertEquals("fallback", arguments.getOrDefault("missing", () -> "fallback"));
    }

    @Test
    void commonFunctionParserDelegatesToLegacySignatureParser() {
        Signature<?> signature = (Signature<?>) FunctionParser.parse("script.sk", "sum", "values*: numbers = 1", "number", false);

        assertNotNull(signature);
        assertEquals("sum", signature.getName());
        assertEquals(Integer[].class, signature.getParameter(0).type());
        assertEquals(Integer.class, signature.returnType());
    }

    @Test
    void legacySignatureExposesCommonParametersView() {
        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                null,
                "echo",
                new Parameter[]{new Parameter<>("value", intInfo, true, null)},
                false,
                intInfo,
                true
        );

        Parameters parameters = signature.parameters();

        assertEquals(1, parameters.size());
        assertEquals("value", parameters.getFirst().name());
        assertEquals(Integer.class, parameters.getFirst().type());
    }

    @Test
    void commonFunctionReferenceExecutesRegisteredLegacyFunction() {
        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                null,
                "incCommon",
                new Parameter[]{new Parameter<>("value", intInfo, true, null)},
                false,
                intInfo,
                true
        );

        JavaFunction<Integer> function = new JavaFunction<>(signature) {
            @Override
            public Integer[] execute(ch.njol.skript.lang.function.FunctionEvent<?> event, Object[][] params) {
                return new Integer[]{((Number) params[0][0]).intValue() + 1};
            }
        };
        Functions.register(function);

        @SuppressWarnings("unchecked")
        FunctionReference.Argument<ch.njol.skript.lang.Expression<?>>[] arguments =
                new FunctionReference.Argument[]{
                        new FunctionReference.Argument<>(
                                FunctionReference.ArgumentType.NAMED,
                                "value",
                                new ch.njol.skript.lang.util.SimpleLiteral<>(4, false)
                        )
                };
        FunctionReference<Integer> reference = new FunctionReference<>(null, "incCommon", signature, arguments);

        assertTrue(reference.validate());
        assertEquals(5, reference.execute(SkriptEvent.EMPTY));
        assertSame(function, reference.function());
    }

    @Test
    void commonFunctionReferenceInvalidatesCache() {
        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                null,
                "cached",
                new Parameter[]{new Parameter<>("value", intInfo, true, null)},
                false,
                intInfo,
                true
        );
        JavaFunction<Integer> function = new JavaFunction<>(signature) {
            @Override
            public Integer[] execute(ch.njol.skript.lang.function.FunctionEvent<?> event, Object[][] params) {
                return new Integer[]{1};
            }
        };
        Functions.register(function);

        @SuppressWarnings("unchecked")
        FunctionReference.Argument<ch.njol.skript.lang.Expression<?>>[] arguments =
                new FunctionReference.Argument[]{
                        new FunctionReference.Argument<>(
                                FunctionReference.ArgumentType.NAMED,
                                "value",
                                new ch.njol.skript.lang.util.SimpleLiteral<>(0, false)
                        )
                };
        FunctionReference<Integer> reference = new FunctionReference<>(null, "cached", signature, arguments);
        assertSame(function, reference.function());

        reference.invalidateCache();

        assertSame(function, reference.function());
    }

    @Test
    void defaultFunctionBuilderRegistersDocumentedJavaFunction() {
        DefaultFunction<Integer> function = DefaultFunction.builder(new TestAddon(), "builtDefault", Integer.class)
                .description("adds one")
                .since("lane-d")
                .examples("builtDefault(1)")
                .keywords("math")
                .requires("none")
                .parameter("value", Integer.class)
                .build(args -> args.<Integer>get("value") + 1);

        assertSame(function, Functions.register(function));
        assertEquals("builtDefault", function.name());
        assertEquals("adds one", function.description().getFirst());
        assertEquals(2, function.execute(new ch.njol.skript.lang.function.FunctionEvent<>(
                (ch.njol.skript.lang.function.Function<Integer>) function), new FunctionArgumentsImpl(Map.of("value", 1))));
    }

    private static final class TestAddon implements SkriptAddon {
        @Override
        public String name() {
            return "test-addon";
        }

        @Override
        public <T> T registry(Class<T> type) {
            return null;
        }
    }
}
