package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy Java-backed function base.
 */
@Deprecated(since = "2.13", forRemoval = true)
public abstract class JavaFunction<T> extends Function<T> {

    private @Nullable String[] returnedKeys;

    public JavaFunction(Signature<T> signature) {
        super(signature);
    }

    public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
        this(new Signature<>(null, name, parameters, false, returnType, single));
    }

    JavaFunction(String script, String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
        this(new Signature<>(script, name, parameters, true, returnType, single));
    }

    public @Nullable String[] returnedKeys() {
        return returnedKeys;
    }

    public void setReturnedKeys(@Nullable String[] keys) {
        if (isSingle()) {
            throw new IllegalStateException("Cannot return keys for a single return function");
        }
        this.returnedKeys = keys;
    }

    @Override
    public boolean resetReturnValue() {
        returnedKeys = null;
        return true;
    }
}
