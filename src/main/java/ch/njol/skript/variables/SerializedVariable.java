package ch.njol.skript.variables;

import org.jetbrains.annotations.Nullable;

/**
 * An instance of a serialized variable, containing the variable name and value payload.
 */
public class SerializedVariable {

    public final String name;
    public final @Nullable Value value;

    public SerializedVariable(String name, @Nullable Value value) {
        this.name = name;
        this.value = value;
    }

    public static final class Value {

        public final String type;
        public final byte[] data;

        public Value(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }
}
