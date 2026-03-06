package ch.njol.skript.lang.function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class FunctionImplementationCompatibilityTest {

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
        ch.njol.skript.variables.Variables.clearAll();
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
}
