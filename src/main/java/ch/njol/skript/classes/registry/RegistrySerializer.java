package ch.njol.skript.classes.registry;

import java.io.StreamCorruptedException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
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
        ResourceLocation key = registry.getKey(object);
        if (key == null) {
            throw new StreamCorruptedException("Unregistered object: " + object);
        }
        return key.toString();
    }

    public @Nullable R deserializeOrNull(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        ResourceLocation id;
        try {
            id = name.contains(":") ? ResourceLocation.parse(name) : ResourceLocation.withDefaultNamespace(name);
        } catch (RuntimeException ex) {
            return null;
        }
        R value = registry.getValue(id);
        ResourceLocation actual = value == null ? null : registry.getKey(value);
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
