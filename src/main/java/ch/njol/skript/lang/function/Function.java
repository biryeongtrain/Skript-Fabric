package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.KeyedValue;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy function abstraction for script and Java function calls.
 */
public abstract class Function<T> {

    /**
     * Execute functions even when some parameters are missing.
     */
    public static boolean executeWithNulls = true;

    private final Signature<T> signature;

    public Function(Signature<T> signature) {
        this.signature = signature;
    }

    public Signature<T> getSignature() {
        return signature;
    }

    public String getName() {
        return signature.getName();
    }

    public Parameter<?>[] getParameters() {
        return signature.getParameters();
    }

    public Parameter<?> getParameter(int index) {
        return signature.getParameter(index);
    }

    public boolean isSingle() {
        return signature.isSingle();
    }

    public @Nullable ClassInfo<T> getReturnType() {
        return signature.getReturnType();
    }

    public @Nullable Class<T> type() {
        return signature.returnType();
    }

    public final T @Nullable [] execute(Object[][] params) {
        FunctionEvent<? extends T> event = new FunctionEvent<>(this);
        Parameter<?>[] parameters = signature.getParameters();
        if (params.length > parameters.length) {
            return null;
        }

        Object[][] values = params.length < parameters.length ? Arrays.copyOf(params, parameters.length) : params;
        for (int i = 0; i < parameters.length; i++) {
            Parameter<?> parameter = parameters[i];
            Object[] parameterValue = i < values.length ? values[i] : null;

            // Apply defaults according to upstream semantics:
            // - If a keyed (plural) parameter uses a default expression and it yields exactly one value,
            //   zip that single value into a KeyedValue[] (key "1").
            // - If it yields multiple values, leave them unkeyed.
            // - For provided arguments (non-default path), convert keyed parameters to KeyedValue[].
            if (parameterValue == null || parameterValue.length == 0) {
                if (parameter.getDefaultExpression() != null) {
                    Object[] defaultValue = parameter.evaluate(parameter.getDefaultExpression(), event.getContext());
                    if (parameter.hasModifier(Parameter.Modifier.KEYED)) {
                        if (defaultValue != null && defaultValue.length == 1) {
                            parameterValue = convertToKeyed(defaultValue);
                        } else {
                            parameterValue = defaultValue;
                        }
                    } else {
                        parameterValue = defaultValue;
                    }
                }
            } else if (parameter.hasModifier(Parameter.Modifier.KEYED)) {
                parameterValue = convertToKeyed(parameterValue);
            }

            if (!executeWithNulls && (parameterValue == null || parameterValue.length == 0)) {
                return null;
            }
            values[i] = parameterValue;
        }

        T[] returned = execute(event, values);
        if (signature.getReturnType() == null) {
            return null;
        }
        if (returned != null && returned.length == 0) {
            return null;
        }
        return returned;
    }

    private KeyedValue<Object> @Nullable [] convertToKeyed(Object[] values) {
        if (values == null) {
            return null;
        }
        if (values.length == 0) {
            @SuppressWarnings("unchecked")
            KeyedValue<Object>[] empty = new KeyedValue[0];
            return empty;
        }
        if (values instanceof KeyedValue[] keyedValues) {
            @SuppressWarnings("unchecked")
            KeyedValue<Object>[] cast = (KeyedValue<Object>[]) keyedValues;
            return cast;
        }
        return KeyedValue.zip(values, null);
    }

    public abstract T @Nullable [] execute(FunctionEvent<?> event, Object[][] params);

    public @Nullable String[] returnedKeys() {
        return null;
    }

    public abstract boolean resetReturnValue();

    @Override
    public String toString() {
        return (signature.isLocal() ? "local " : "") + "function " + signature.getName();
    }
}
