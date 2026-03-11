package ch.njol.skript.lang.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies overload disambiguation for function implementations mirrors signature retrieval:
 * when both an exact and a broader overload are available, prefer the exact implementation.
 */
class FunctionOverloadDisambiguationImplementationTest {

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
    }

    @Test
    void resolvesExactImplementationOverBroaderOverload() {
        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        ClassInfo<Number> numInfo = Classes.getSuperClassInfo(Number.class);
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);

        Signature<String> exactSig = new Signature<>(
                "script.sk",
                "choose",
                new Parameter[]{new Parameter<>("x", intInfo, true, null)},
                true,
                stringInfo,
                true
        );
        Signature<String> broadSig = new Signature<>(
                "script.sk",
                "choose",
                new Parameter[]{new Parameter<>("x", numInfo, true, null)},
                true,
                stringInfo,
                true
        );

        RecordingFunction exactFn = new RecordingFunction(exactSig, "exact");
        RecordingFunction broadFn = new RecordingFunction(broadSig, "broad");
        // Register as local script functions
        Functions.registerSignature(exactSig);
        Functions.registerSignature(broadSig);
        Namespace ns = Functions.getScriptNamespace("script.sk");
        assertNotNull(ns);
        ns.addFunction(exactFn);
        ns.addFunction(broadFn);
        FunctionRegistry.getRegistry().register("script.sk", exactFn);
        FunctionRegistry.getRegistry().register("script.sk", broadFn);

        Expression<?>[] args = new Expression[]{new SimpleLiteral<>(1, false)};
        FunctionReference<String> ref = new FunctionReference<>(
                "choose",
                "script.sk",
                new Class[]{String.class},
                args
        );

        assertTrue(ref.validateFunction(true));
        assertNotNull(ref.function());
        assertEquals(exactFn, ref.function());
        assertEquals(exactSig, ref.signature());
    }

    private static final class RecordingFunction extends Function<String> {

        private final String id;

        private RecordingFunction(Signature<String> signature, String id) {
            super(signature);
            this.id = id;
        }

        @Override
        public String[] execute(FunctionEvent<?> event, Object[][] params) {
            return new String[]{id};
        }

        @Override
        public boolean resetReturnValue() {
            return true;
        }
    }
}
