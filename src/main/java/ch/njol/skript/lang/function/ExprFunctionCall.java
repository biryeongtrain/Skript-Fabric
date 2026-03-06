package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Expression wrapper for function calls.
 */
public class ExprFunctionCall<T> extends SimpleExpression<T> implements KeyProviderExpression<T> {

    private final FunctionReference<?> reference;
    private final Class<? extends T>[] returnTypes;
    private final Class<T> returnType;
    private final Map<SkriptEvent, String[]> cache = Collections.synchronizedMap(new WeakHashMap<>());

    public ExprFunctionCall(FunctionReference<T> function) {
        this(function, array(function.signature() != null && function.signature().returnType() != null
                ? (Class<? extends T>) Utils.getComponentType(function.signature().returnType())
                : (Class<? extends T>) Object.class));
    }

    @SafeVarargs
    private static <T> Class<? extends T>[] array(Class<? extends T>... values) {
        return values;
    }

    @SuppressWarnings("unchecked")
    public ExprFunctionCall(FunctionReference<?> reference, Class<? extends T>[] expectedReturnTypes) {
        this.reference = reference;
        Class<?> functionReturnType = reference.signature() != null ? reference.signature().returnType() : null;
        Class<?> component = functionReturnType != null ? Utils.getComponentType(functionReturnType) : Object.class;
        if (containsSuper(expectedReturnTypes, component)) {
            this.returnTypes = new Class[]{(Class<? extends T>) component};
            this.returnType = (Class<T>) component;
        } else {
            this.returnTypes = expectedReturnTypes;
            this.returnType = (Class<T>) Utils.getSuperType(expectedReturnTypes);
        }
    }

    @Override
    protected T @Nullable [] get(SkriptEvent event) {
        Object execute = reference.execute(event);
        Object[] values;
        if (execute == null) {
            values = new Object[0];
        } else if (!execute.getClass().isArray()) {
            values = new Object[]{execute};
        } else {
            values = (Object[]) execute;
        }

        String[] keys = reference.function() != null ? reference.function().returnedKeys() : null;
        if (reference.function() != null) {
            reference.function().resetReturnValue();
        }

        @SuppressWarnings("unchecked")
        T[] converted = (T[]) Array.newInstance(returnType, values.length);
        if (values.length == 0) {
            cache.put(event, new String[0]);
            return converted;
        }

        Converters.convert(values, converted, returnTypes);
        List<T> nonNullValues = new ArrayList<>(converted.length);
        List<String> resolvedKeys = new ArrayList<>(converted.length);
        for (int i = 0; i < converted.length; i++) {
            T value = converted[i];
            if (value == null) {
                continue;
            }
            nonNullValues.add(value);
            if (keys != null && i < keys.length) {
                resolvedKeys.add(keys[i]);
            } else {
                resolvedKeys.add(String.valueOf(i));
            }
        }
        cache.put(event, resolvedKeys.toArray(String[]::new));
        @SuppressWarnings("unchecked")
        T[] result = nonNullValues.toArray((T[]) Array.newInstance(returnType, nonNullValues.size()));
        return result;
    }

    @Override
    public @NotNull String @NotNull [] getArrayKeys(SkriptEvent event) throws IllegalStateException {
        if (!cache.containsKey(event)) {
            throw new IllegalStateException();
        }
        return cache.remove(event);
    }

    @Override
    public boolean areKeysRecommended() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
        if (containsSuper(to, getReturnType())) {
            return (Expression<? extends R>) this;
        }
        Class<?> returns = reference.signature() != null ? reference.signature().returnType() : Object.class;
        Class<?> converterType = Utils.getComponentType(returns);
        if (Converters.converterExists(converterType, to)) {
            return new ExprFunctionCall<>(reference, to);
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return reference.isSingle();
    }

    @Override
    public Class<? extends T> getReturnType() {
        return returnType;
    }

    @Override
    public Class<? extends T>[] possibleReturnTypes() {
        return Arrays.copyOf(returnTypes, returnTypes.length);
    }

    @Override
    public boolean isLoopOf(String input) {
        return KeyProviderExpression.super.isLoopOf(input);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return reference.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return false;
    }

    private static boolean containsSuper(Class<?>[] candidates, Class<?> type) {
        for (Class<?> candidate : candidates) {
            if (candidate.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }
}
