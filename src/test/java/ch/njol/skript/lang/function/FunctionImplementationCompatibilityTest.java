package ch.njol.skript.lang.function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.io.File;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

class FunctionImplementationCompatibilityTest {

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
        ch.njol.skript.variables.Variables.clearAll();
        ParserInstance.get().setCurrentScript(null);
    }

    @Test
    void simpleJavaFunctionRejectsMissingSingleParams() {
        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                null,
                "inc",
                new Parameter[]{new Parameter<>("x", intInfo, true, null)},
                false,
                intInfo,
                true
        );
        SimpleJavaFunction<Integer> function = new SimpleJavaFunction<>(signature) {
            @Override
            public Integer[] executeSimple(Object[][] params) {
                return new Integer[]{((Number) params[0][0]).intValue() + 1};
            }
        };

        assertNull(function.execute(new Object[][]{{null}}));
        assertArrayEquals(new Integer[]{2}, function.execute(new Object[][]{{1}}));
    }

    @Test
    void javaFunctionReturnedKeysCanBeManagedForPluralReturns() {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Signature<String> plural = new Signature<>(
                null,
                "keys",
                new Parameter[]{new Parameter<>("x", stringInfo, true, null)},
                false,
                stringInfo,
                false
        );
        JavaFunction<String> function = new JavaFunction<>(plural) {
            @Override
            public String[] execute(FunctionEvent<?> event, Object[][] params) {
                return new String[]{"a", "b"};
            }
        };

        function.setReturnedKeys(new String[]{"k1", "k2"});
        assertArrayEquals(new String[]{"k1", "k2"}, function.returnedKeys());
        assertTrue(function.resetReturnValue());
        assertNull(function.returnedKeys());

        Signature<String> single = new Signature<>(
                null,
                "single",
                new Parameter[]{new Parameter<>("x", stringInfo, true, null)},
                false,
                stringInfo,
                true
        );
        JavaFunction<String> singleFunction = new JavaFunction<>(single) {
            @Override
            public String[] execute(FunctionEvent<?> event, Object[][] params) {
                return new String[]{"x"};
            }
        };
        assertThrows(IllegalStateException.class, () -> singleFunction.setReturnedKeys(new String[]{"bad"}));
    }

    @Test
    void scriptFunctionBuildsTriggerAndSupportsReturnHandlerApi() {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Signature<String> signature = new Signature<>(
                null,
                "scriptFunc",
                new Parameter[]{new Parameter<>("x", stringInfo, true, null)},
                false,
                stringInfo,
                true
        );

        SectionNode node = new SectionNode("function scriptFunc:");
        ScriptFunction<String> function = new ScriptFunction<>(signature, node);

        assertNull(function.execute(new Object[][]{{"value"}}));
        function.returnValues(SkriptEvent.EMPTY, new SimpleLiteral<>("done", false));
        assertEquals(String.class, function.returnValueType());
        assertNull(function.returnedKeys());
        assertTrue(function.resetReturnValue());
    }

    @Test
    void functionExecuteRejectsOverArityEvenForSinglePluralParameter() {
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);
        Signature<String> signature = new Signature<>(
                null,
                "collect",
                new Parameter[]{new Parameter<>("value", stringInfo, false, null)},
                false,
                stringInfo,
                false
        );
        SimpleJavaFunction<String> function = new SimpleJavaFunction<>(signature) {
            @Override
            public String[] executeSimple(Object[][] params) {
                return (String[]) params[0];
            }
        };

        assertNull(function.execute(new Object[][]{{"a"}, {"b"}}));
    }

    @Test
    void scriptFunctionStoresUnkeyedPluralParameterValuesUsingOneBasedIndices() {
        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                null,
                "listDefault",
                new Parameter[]{new Parameter<>("xs", intInfo, false, null)},
                false,
                intInfo,
                false
        );
        ScriptFunction<Integer> function = new ScriptFunction<>(signature, new SectionNode("function listDefault:"));
        FunctionEvent<Integer> event = new FunctionEvent<>(function);
        SkriptEvent callContext = new SkriptEvent(event, null, null, null);

        function.execute(event, new Object[][]{{1, 7}});

        assertEquals(1, Variables.getVariable("xs::1", callContext, true));
        assertEquals(7, Variables.getVariable("xs::2", callContext, true));
        assertNull(Variables.getVariable("xs::0", callContext, true));
    }

    @Test
    void scriptFunctionPublishesSingleParameterHintsWhileLoadingBody() {
        ParserInstance.get().setCurrentScript(new Script(new Config("function", "function.sk", new File("function.sk")), List.of()));
        Skript.registerEffect(CaptureFunctionIntegerHintEffect.class, "lane d capture function integer %integer%");
        CaptureFunctionIntegerHintEffect.reset();

        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                null,
                "hintedSingle",
                new Parameter[]{new Parameter<>("_value", intInfo, true, null)},
                false,
                intInfo,
                true
        );

        ScriptFunction<Integer> function = new ScriptFunction<>(
                signature,
                functionNode("function hintedSingle:", "lane d capture function integer {_value}")
        );

        assertNotNull(function);
        assertEquals(Integer.class, CaptureFunctionIntegerHintEffect.lastReturnType);
    }

    @Test
    void scriptFunctionPublishesListParameterHintsWhileLoadingBody() {
        ParserInstance.get().setCurrentScript(new Script(new Config("function", "function.sk", new File("function.sk")), List.of()));
        Skript.registerEffect(CaptureFunctionIntegerHintEffect.class, "lane d capture function integer %integer%");
        CaptureFunctionIntegerHintEffect.reset();

        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                null,
                "hintedList",
                new Parameter[]{new Parameter<>("_values", intInfo, false, null)},
                false,
                intInfo,
                false
        );

        ScriptFunction<Integer> function = new ScriptFunction<>(
                signature,
                functionNode("function hintedList:", "lane d capture function integer {_values::1}")
        );

        assertNotNull(function);
        assertEquals(Integer.class, CaptureFunctionIntegerHintEffect.lastReturnType);
    }

    @Test
    void functionsLoadFunctionBuildsAndRegistersScriptImplementation() {
        Script script = new Script(new Config("loader", "loader.sk", new File("loader.sk")), List.of());
        ParserInstance.get().setCurrentScript(script);

        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        Signature<Integer> signature = new Signature<>(
                "loader.sk",
                "loadedLocal",
                new Parameter[]{new Parameter<>("x", intInfo, true, null)},
                true,
                intInfo,
                true
        );
        assertNotNull(Functions.registerSignature(signature));

        Function<?> function = Functions.loadFunction(script, functionNode("function loadedLocal:"), signature);

        assertNotNull(function);
        assertTrue(function instanceof ScriptFunction<?>);
        assertEquals(function, Functions.getLocalFunction("loadedLocal", "loader.sk"));
        assertEquals(
                FunctionRegistry.RetrievalResult.EXACT,
                FunctionRegistry.getRegistry().getExactFunction("loader.sk", "loadedLocal", Integer.class).result()
        );
    }

    private static SectionNode functionNode(String key, String... bodyLines) {
        SectionNode node = new SectionNode(key);
        for (String bodyLine : bodyLines) {
            node.add(new SimpleNode(bodyLine));
        }
        return node;
    }

    public static class CaptureFunctionIntegerHintEffect extends Effect {

        private static @Nullable Class<?> lastReturnType;

        static void reset() {
            lastReturnType = null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            lastReturnType = expressions[0].getReturnType();
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "capture function integer hint";
        }
    }
}
