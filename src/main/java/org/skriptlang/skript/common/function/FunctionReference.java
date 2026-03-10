package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class FunctionReference<T> implements Debuggable {

    private final String namespace;
    private final String name;
    private final Signature<T> signature;
    private final Argument<Expression<?>>[] arguments;

    private Function<T> cachedFunction;
    private LinkedHashMap<String, ArgInfo> cachedArguments;

    private record ArgInfo(Expression<?> expression, Class<?> type, Set<Parameter.Modifier> modifiers) {
    }

    public FunctionReference(
            @Nullable String namespace,
            @NotNull String name,
            @NotNull Signature<T> signature,
            @NotNull Argument<Expression<?>>[] arguments
    ) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(signature, "signature cannot be null");
        Preconditions.checkNotNull(arguments, "arguments cannot be null");
        this.namespace = namespace;
        this.name = name;
        this.signature = signature;
        this.arguments = arguments;
    }

    public void invalidateCache() {
        cachedFunction = null;
    }

    public boolean validate() {
        if (cachedArguments == null) {
            cachedArguments = new LinkedHashMap<>();
            Parameter<?>[] targets = signature.parameters().all();
            for (int i = 0; i < arguments.length; i++) {
                Argument<Expression<?>> argument = arguments[i];
                Parameter<?> target = targets[i];

                Expression<?> converted = argument.value == null ? null : argument.value.getConvertedExpression(Utils.getComponentType(target.type()));
                if (!validateArgument(target, argument.value, converted)) {
                    return false;
                }

                if (converted != null && KeyProviderExpression.areKeysRecommended(converted)) {
                    converted.returnNestedStructures(true);
                }

                cachedArguments.put(target.name(), new ArgInfo(converted, target.type(), target.modifiers()));
            }
        }

        signature.addCall(this);
        return true;
    }

    private boolean validateArgument(Parameter<?> target, @Nullable Expression<?> original, @Nullable Expression<?> converted) {
        if (original == null) {
            return target.hasModifier(Parameter.Modifier.OPTIONAL);
        }
        if (converted == null) {
            if (LiteralUtils.hasUnparsedLiteral(original)) {
                Skript.error("Can't understand this expression: " + original);
            } else {
                Skript.error("Expected type " + getName(target.type(), target.isSingle())
                        + " for argument '" + target.name() + "', but " + original
                        + " is of type " + getName(original.getReturnType(), original.isSingle()) + ".");
            }
            return false;
        }
        if (target.isSingle() && !converted.isSingle()) {
            Skript.error("Expected type " + getName(target.type(), target.isSingle())
                    + " for argument '" + target.name() + "', but " + converted
                    + " is of type " + getName(converted.getReturnType(), converted.isSingle()) + ".");
            return false;
        }
        return true;
    }

    private String getName(Class<?> clazz, boolean single) {
        return single
                ? Classes.getSuperClassInfo(clazz).getName().getSingular()
                : Classes.getSuperClassInfo(Utils.getComponentType(clazz)).getName().getPlural();
    }

    public T execute(SkriptEvent event) {
        if (!validate()) {
            Skript.error("Failed to verify function " + name + " before execution.");
            return null;
        }

        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        cachedArguments.forEach((key, value) -> {
            if (value.modifiers().contains(Parameter.Modifier.KEYED)) {
                args.put(key, evaluateKeyed(value.expression(), event));
            } else if (!value.type().isArray()) {
                args.put(key, value.expression().getSingle(event));
            } else {
                args.put(key, value.expression().getArray(event));
            }
        });

        Function<T> function = function();
        if (function == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        ch.njol.skript.lang.function.Function<? extends T> legacy = (ch.njol.skript.lang.function.Function<? extends T>) function;
        FunctionEvent<?> functionEvent = new FunctionEvent<>(legacy, event);
        return function.execute(functionEvent, new FunctionArgumentsImpl(args));
    }

    private KeyedValue<?>[] evaluateKeyed(Expression<?> expression, SkriptEvent event) {
        if (expression instanceof ExpressionList<?> list) {
            return evaluateSingleListParameter(list.getExpressions(), event);
        }
        return evaluateParameter(expression, event);
    }

    private KeyedValue<?>[] evaluateSingleListParameter(Expression<?>[] values, SkriptEvent event) {
        List<Object> copiedValues = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        int keyIndex = 1;
        for (Expression<?> value : values) {
            Object[] valuesArray = value.getArray(event);
            String[] keysArray = KeyProviderExpression.areKeysRecommended(value)
                    ? ((KeyProviderExpression<?>) value).getArrayKeys(event)
                    : null;

            for (int i = 0; i < valuesArray.length; i++) {
                if (keysArray == null) {
                    while (keys.contains(String.valueOf(keyIndex))) {
                        keyIndex++;
                    }
                    keys.add(String.valueOf(keyIndex++));
                } else if (!keys.add(keysArray[i])) {
                    continue;
                }
                copiedValues.add(Classes.clone(valuesArray[i]));
            }
        }
        return KeyedValue.zip(copiedValues.toArray(), keys.toArray(new String[0]));
    }

    private KeyedValue<?>[] evaluateParameter(Expression<?> argument, SkriptEvent event) {
        Object[] values = argument.getArray(event);
        for (int i = 0; i < values.length; i++) {
            values[i] = Classes.clone(values[i]);
        }
        if (!(argument instanceof KeyProviderExpression<?> provider)) {
            return KeyedValue.zip(values, null);
        }
        String[] keys = KeyProviderExpression.areKeysRecommended(argument) ? provider.getArrayKeys(event) : null;
        return KeyedValue.zip(values, keys);
    }

    public Function<T> function() {
        if (cachedFunction == null) {
            Class<?>[] parameters = Arrays.stream(signature.parameters().all()).map(Parameter::type).toArray(Class[]::new);
            FunctionRegistry.Retrieval<ch.njol.skript.lang.function.Function<?>> retrieval =
                    FunctionRegistry.getRegistry().getFunction(namespace, name, parameters);
            if (retrieval.result() == FunctionRegistry.RetrievalResult.EXACT) {
                @SuppressWarnings("unchecked")
                Function<T> cast = (Function<T>) retrieval.retrieved();
                cachedFunction = cast;
            }
        }
        return cachedFunction;
    }

    public Signature<T> signature() {
        return signature;
    }

    public String namespace() {
        return namespace;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull Argument<Expression<?>>[] arguments() {
        return arguments;
    }

    public boolean isSingle() {
        if (signature.contract() != null) {
            Expression<?>[] values = Arrays.stream(arguments).map(Argument::value).toArray(Expression[]::new);
            return signature.contract().isSingle(values);
        }
        return signature.isSingle();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append("(");
        java.util.StringJoiner joiner = new java.util.StringJoiner(", ");
        for (Argument<Expression<?>> argument : arguments) {
            joiner.add(argument.name + ": " + argument.value.toString(event, debug));
        }
        builder.append(joiner).append(")");
        return builder.toString();
    }

    public record Argument<T>(ArgumentType type, String name, T value, @Nullable String raw) {
        public Argument(ArgumentType type, String name, T value) {
            this(type, name, value, null);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Argument<?> other)) {
                return false;
            }
            return Objects.equals(value, other.value) && Objects.equals(name, other.name) && type == other.type;
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(type);
            result = 31 * result + Objects.hashCode(name);
            result = 31 * result + Objects.hashCode(value);
            return result;
        }
    }

    public enum ArgumentType {
        NAMED,
        UNNAMED
    }
}
