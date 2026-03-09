package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Legacy function reference/call binding.
 */
public class FunctionReference<T> {

    final String functionName;
    private @Nullable Signature<? extends T> signature;
    private @Nullable Function<? extends T> function;
    private final Expression<?>[] parameters;
    private boolean single;
    private boolean singleListParam;
    private final @Nullable Class<? extends T>[] returnTypes;
    public final @Nullable String script;

    public FunctionReference(
            String functionName,
            @Nullable String script,
            @Nullable Class<? extends T>[] returnTypes,
            Expression<?>[] params
    ) {
        this.functionName = functionName;
        this.script = script;
        this.returnTypes = returnTypes;
        this.parameters = params == null ? new Expression<?>[0] : params;
    }

    public boolean validateFunction(boolean first) {
        FunctionRegistry registry = FunctionRegistry.getRegistry();
        Class<?>[] parameterTypes = parameterTypes();
        FunctionRegistry.Retrieval<Signature<?>> retrieval = registry.getSignature(script, functionName, parameterTypes);
        if (retrieval.result() != FunctionRegistry.RetrievalResult.EXACT || retrieval.retrieved() == null) {
            return false;
        }
        Signature<?> found = retrieval.retrieved();
        singleListParam = found.getMaxParameters() == 1 && !found.getParameter(0).isSingle();
        if (!singleListParam && parameters.length > found.getMaxParameters()) {
            return false;
        }
        if (parameters.length < found.getMinParameters()) {
            return false;
        }

        if (returnTypes != null && returnTypes.length > 0) {
            Class<?> candidate = found.returnType();
            if (candidate == null || !Converters.converterExists(candidate, returnTypes)) {
                return false;
            }
        }
        for (int i = 0; i < parameters.length; i++) {
            Parameter<?> parameter = found.getParameters()[singleListParam ? 0 : i];
            Class<?> targetType = component(parameter.type());
            Expression<?> converted = parameters[i].getConvertedExpression(targetType);
            if (converted == null) {
                return false;
            }
            if (parameter.isSingle() && !converted.isSingle()) {
                return false;
            }
            parameters[i] = converted;
        }
        @SuppressWarnings("unchecked")
        Signature<? extends T> cast = (Signature<? extends T>) found;
        signature = cast;
        signature.addCall(this);
        single = found.isSingle();

        FunctionRegistry.Retrieval<Function<?>> functionRetrieval = registry.getFunction(script, functionName, parameterTypes);
        if (functionRetrieval.result() == FunctionRegistry.RetrievalResult.EXACT && functionRetrieval.retrieved() != null) {
            @SuppressWarnings("unchecked")
            Function<? extends T> typed = (Function<? extends T>) functionRetrieval.retrieved();
            function = typed;
        } else {
            function = null;
        }
        return true;
    }

    public boolean validate() {
        return validateFunction(false);
    }

    public @Nullable String namespace() {
        return script;
    }

    public @Nullable Signature<? extends T> getRegisteredSignature() {
        return signature;
    }

    public @Nullable Function<? extends T> function() {
        return function;
    }

    public @Nullable Signature<? extends T> signature() {
        return signature;
    }

    public boolean isSingle() {
        return single;
    }

    public @Nullable T[] execute(SkriptEvent event) {
        if (function == null && !validateFunction(script == null)) {
            return null;
        }
        Function<? extends T> bound = function;
        if (bound == null) {
            return null;
        }

        Object[][] values = new Object[singleListParam ? 1 : parameters.length][];
        if (singleListParam) {
            values[0] = evaluateSingleListParameter(bound.getParameter(0), parameters, event);
        } else {
            for (int i = 0; i < parameters.length; i++) {
                values[i] = bound.getParameter(i).evaluate(parameters[i], event);
            }
        }

        T[] result = bound.execute(values);
        if (result == null || returnTypes == null || returnTypes.length == 0) {
            return result;
        }
        @SuppressWarnings("unchecked")
        T[] converted = (T[]) Converters.convert(result, returnTypes, (Class<T>) commonType(returnTypes));
        return converted;
    }

    private Object @Nullable [] evaluateSingleListParameter(
            Parameter<?> parameter,
            Expression<?>[] arguments,
            SkriptEvent event
    ) {
        if (arguments.length == 0) {
            return null;
        }
        if (!parameter.hasModifier(Parameter.Modifier.KEYED)) {
            if (arguments.length == 1) {
                return parameter.evaluate(arguments[0], event);
            }
            List<Object> values = new ArrayList<>();
            for (Expression<?> argument : arguments) {
                values.addAll(Arrays.asList(argument.getArray(event)));
            }
            return values.toArray();
        }

        List<Object> values = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        int generatedKey = 1;
        for (Expression<?> argument : arguments) {
            Object[] argumentValues = argument.getArray(event);
            String[] argumentKeys = KeyProviderExpression.areKeysRecommended(argument)
                    ? ((KeyProviderExpression<?>) argument).getArrayKeys(event)
                    : null;
            for (int i = 0; i < argumentValues.length; i++) {
                String key;
                if (argumentKeys == null || i >= argumentKeys.length || argumentKeys[i] == null) {
                    do {
                        key = String.valueOf(generatedKey++);
                    } while (!keys.add(key));
                } else {
                    key = argumentKeys[i];
                    if (!keys.add(key)) {
                        continue;
                    }
                }
                values.add(Classes.clone(argumentValues[i]));
            }
        }
        return KeyedValue.zip(values.toArray(), keys.toArray(String[]::new));
    }

    private static Class<?> commonType(Class<?>[] types) {
        if (types.length == 0) {
            return Object.class;
        }
        Class<?> candidate = types[0];
        for (int i = 1; i < types.length; i++) {
            Class<?> next = types[i];
            if (candidate.isAssignableFrom(next)) {
                continue;
            }
            if (next.isAssignableFrom(candidate)) {
                candidate = next;
                continue;
            }
            return Object.class;
        }
        return candidate;
    }

    private Class<?>[] parameterTypes() {
        Class<?>[] types = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Expression<?> parameter = parameters[i];
            Class<?> returnType = parameter.getReturnType();
            types[i] = parameter.isSingle() ? returnType : returnType.arrayType();
        }
        return types;
    }

    private static Class<?> component(Class<?> type) {
        return type.isArray() ? type.getComponentType() : type;
    }

    public static Object[][] consign(Object... arguments) {
        Object[][] result = new Object[arguments.length][];
        for (int i = 0; i < arguments.length; i++) {
            Object argument = arguments[i];
            if (argument == null) {
                result[i] = null;
            } else if (argument instanceof Object[]) {
                result[i] = (Object[]) argument;
            } else {
                result[i] = new Object[]{argument};
            }
        }
        return result;
    }

    public static @Nullable FunctionReference<?> parse(
            String input,
            @Nullable String script,
            @Nullable Class<?>[] returnTypes
    ) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String expression = input.trim();
        int open = expression.indexOf('(');
        int close = expression.lastIndexOf(')');
        String name;
        String argsPart;
        if (open < 0 && close < 0) {
            name = expression;
            argsPart = "";
        } else {
            if (open < 0 || close < open || !expression.substring(close + 1).trim().isEmpty()) {
                return null;
            }
            name = expression.substring(0, open).trim();
            argsPart = expression.substring(open + 1, close).trim();
        }
        if (name.isBlank()) {
            return null;
        }

        List<Expression<?>> args = new ArrayList<>();
        if (!argsPart.isBlank()) {
            for (String rawArg : splitArguments(argsPart)) {
                Expression<?> parsed = parseLiteralArgument(rawArg);
                if (parsed == null) {
                    return null;
                }
                args.add(parsed);
            }
        }
        @SuppressWarnings("unchecked")
        Class<? extends Object>[] cast = returnTypes == null ? null : Arrays.copyOf(returnTypes, returnTypes.length, Class[].class);
        return new FunctionReference<>(name, script, cast, args.toArray(Expression[]::new));
    }

    private static List<String> splitArguments(String value) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuotes = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"' && inQuotes && i + 1 < value.length() && value.charAt(i + 1) == '"') {
                current.append(ch).append(value.charAt(i + 1));
                i++;
                continue;
            }
            if (ch == '"' && (i == 0 || value.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                current.append(ch);
                continue;
            }
            if (!inQuotes) {
                if (ch == '(') {
                    depth++;
                } else if (ch == ')' && depth > 0) {
                    depth--;
                } else if (ch == ',' && depth == 0) {
                    args.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(ch);
        }
        if (!current.isEmpty()) {
            args.add(current.toString().trim());
        }
        return args;
    }

    private static @Nullable Expression<?> parseLiteralArgument(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            String string = value.substring(1, value.length() - 1).replace("\"\"", "\"");
            return new SimpleLiteral<>(string, false);
        }
        String lower = value.toLowerCase(Locale.ENGLISH);
        if ("true".equals(lower) || "false".equals(lower)) {
            return new SimpleLiteral<>(Boolean.valueOf(lower), false);
        }
        Integer integer = Classes.parse(value, Integer.class, ParseContext.DEFAULT);
        if (integer != null) {
            return new SimpleLiteral<>(integer, false);
        }
        Double dbl = Classes.parse(value, Double.class, ParseContext.DEFAULT);
        if (dbl != null) {
            return new SimpleLiteral<>(dbl, false);
        }

        Expression<?> parsed = new SkriptParser(value, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{Object.class});
        if (parsed != null) {
            return parsed;
        }
        return new SimpleLiteral<>(value, false);
    }

    public String toString(@Nullable SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder(functionName).append("(");
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameters[i].toString(event, debug));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String toString() {
        return toString(null, false);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof FunctionReference<?> other)) {
            return false;
        }
        return functionName.equals(other.functionName)
                && Objects.equals(script, other.script)
                && Arrays.equals(parameters, other.parameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(functionName, script);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }
}
