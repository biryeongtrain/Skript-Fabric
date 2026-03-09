package ch.njol.skript.variables;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SerializedVariableCompatibilityTest {

    @Test
    void serializedVariableRetainsNameAndValuePayload() {
        byte[] payload = new byte[]{1, 2, 3};
        SerializedVariable.Value value = new SerializedVariable.Value("number", payload);

        SerializedVariable variable = new SerializedVariable("test::value", value);

        assertEquals("test::value", variable.name);
        assertEquals("number", variable.value.type);
        assertArrayEquals(payload, variable.value.data);
    }

    @Test
    void serializedVariableAllowsDeletionSentinel() {
        SerializedVariable variable = new SerializedVariable("test::value", null);

        assertEquals("test::value", variable.name);
        assertNull(variable.value);
    }
}
