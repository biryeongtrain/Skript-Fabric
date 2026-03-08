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
 * Regression: when multiple overloads are convertible matches,
 * prefer the overload whose parameter types exactly match provided types
 * at non-Object positions (mirrors upstream tie-breaker in candidates()).
 */
class FunctionOverloadDisambiguationTest {

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
    }

    @Test
    void choosesExactMatchOverConvertibleOverload() {
        ClassInfo<Integer> intInfo = Classes.getSuperClassInfo(Integer.class);
        ClassInfo<String> stringInfo = Classes.getSuperClassInfo(String.class);

        // Two overloads: over(Integer) and over(Number)
        Signature<String> intSig = new Signature<>(
                null,
                "over",
                new Parameter[]{new Parameter<>("x", intInfo, true, null)},
                false,
                stringInfo,
                true
        );
        ClassInfo<Number> numberInfo = Classes.getSuperClassInfo(Number.class);
        Signature<String> numSig = new Signature<>(
                null,
                "over",
                new Parameter[]{new Parameter<>("x", numberInfo, true, null)},
                false,
                stringInfo,
                true
        );

        // Only register signatures to simulate overloads at the registry level.
        FunctionRegistry.getRegistry().register(null, intSig);
        FunctionRegistry.getRegistry().register(null, numSig);

        // Call with an Integer literal: should resolve to over(Integer), not be ambiguous
        Expression<?>[] args = new Expression[]{new SimpleLiteral<>(1, false)};
        FunctionReference<String> ref = new FunctionReference<>(
                "over",
                null,
                new Class[]{String.class},
                args
        );

        assertTrue(ref.validateFunction(true), "overload should resolve to exact integer overload");
        assertNotNull(ref.getRegisteredSignature());
        assertEquals(intSig, ref.getRegisteredSignature());
    }
}
