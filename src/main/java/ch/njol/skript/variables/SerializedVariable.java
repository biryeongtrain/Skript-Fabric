package ch.njol.skript.variables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public static @Nullable Value serialize(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                stream.writeObject(value);
            }
            return new Value(value.getClass().getName(), output.toByteArray());
        } catch (IOException exception) {
            return null;
        }
    }

    public static @Nullable Object deserialize(String type, byte[] data) {
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object value = stream.readObject();
            return value;
        } catch (IOException | ClassNotFoundException exception) {
            return null;
        }
    }

    public static @Nullable Object deserialize(String type, String data) {
        return deserialize(type, data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
