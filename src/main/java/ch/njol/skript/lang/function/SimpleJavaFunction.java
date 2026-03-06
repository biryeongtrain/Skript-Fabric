package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy helper for Java functions that require non-null first arguments.
 */
@Deprecated(since = "2.13", forRemoval = true)
public abstract class SimpleJavaFunction<T> extends JavaFunction<T> {

    public SimpleJavaFunction(Signature<T> signature) {
        super(signature);
    }

    public SimpleJavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
        super(name, parameters, returnType, single);
    }

    SimpleJavaFunction(String script, String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
        super(script, name, parameters, returnType, single);
    }

    @Override
    public final T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
        for (Object[] param : params) {
            if (param == null || param.length == 0 || param[0] == null) {
                return null;
            }
        }
        return executeSimple(params);
    }

    public abstract T @Nullable [] executeSimple(Object[][] params);
}
