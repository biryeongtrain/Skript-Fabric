package ch.njol.skript.update;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import org.jetbrains.annotations.Nullable;

/**
 * Describes a Skript release.
 */
public class ReleaseManifest {

    public static ReleaseManifest load(String json) throws JsonParseException {
        return new GsonBuilder()
                .registerTypeAdapter(Class.class, new ClassSerializer())
                .create()
                .fromJson(json, ReleaseManifest.class);
    }

    public final String id;
    public final String date;
    public final String flavor;
    public final Class<? extends UpdateChecker> updateCheckerType;
    public final String updateSource;
    public final @Nullable String downloadSource;

    public ReleaseManifest(
            String id,
            String date,
            String flavor,
            Class<? extends UpdateChecker> updateCheckerType,
            String updateSource,
            @Nullable String downloadSource
    ) {
        this.id = id;
        this.date = date;
        this.flavor = flavor;
        this.updateCheckerType = updateCheckerType;
        this.updateSource = updateSource;
        this.downloadSource = downloadSource;
    }

    static class ClassSerializer implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public @Nullable Class<?> deserialize(
                @Nullable JsonElement json,
                @Nullable Type typeOfT,
                @Nullable JsonDeserializationContext context
        ) throws JsonParseException {
            try {
                assert json != null;
                return Class.forName(json.getAsJsonPrimitive().getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("class not found");
            }
        }

        @Override
        public JsonElement serialize(
                @Nullable Class<?> src,
                @Nullable Type typeOfSrc,
                @Nullable JsonSerializationContext context
        ) {
            assert src != null;
            return new JsonPrimitive(src.getName());
        }
    }

    public UpdateChecker createUpdateChecker() {
        try {
            return updateCheckerType.getConstructor().newInstance();
        } catch (InstantiationException
                 | IllegalAccessException
                 | IllegalArgumentException
                 | InvocationTargetException
                 | NoSuchMethodException
                 | SecurityException e) {
            throw new IllegalStateException("updater class cannot be created", e);
        }
    }
}
