package ch.njol.skript.classes.registry;

import java.io.StreamCorruptedException;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lightweight registry id serializer helper kept in Lane A until the full
 * Yggdrasil-backed serializer surface is restored.
 */
public class RegistrySerializer<R> {

    private final Registry<R> registry;

    public RegistrySerializer(Registry<R> registry) {
        this.registry = registry;
    }

    public @NotNull String serialize(R object) throws StreamCorruptedException {
        Identifier key = registry.getKey(object);
        if (key == null) {
            throw new StreamCorruptedException("Unregistered object: " + object);
        }
        return key.toString();
    }

    public @Nullable R deserializeOrNull(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Identifier id;
        try {
            id = name.contains(":") ? Identifier.parse(name) : Identifier.withDefaultNamespace(name);
        } catch (RuntimeException ex) {
            return null;
        }
        R value = registry.getValue(id);
        Identifier actual = value == null ? null : registry.getKey(value);
        return id.equals(actual) ? value : null;
    }

    public @NotNull R deserialize(String name) throws StreamCorruptedException {
        R value = deserializeOrNull(name);
        if (value == null) {
            throw new StreamCorruptedException("Invalid object from registry: " + name);
        }
        return value;
    }
}
