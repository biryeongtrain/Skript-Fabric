package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.util.common.AnyNamed;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Validated;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Partial reference to a function that can be validated/resolved lazily.
 */
public class DynamicFunctionReference<Result> implements AnyNamed, Validated {

    private final @NotNull String name;
    private final @Nullable Script source;
    private final @Nullable String sourceName;
    private final Reference<Function<? extends Result>> function;
    private final @Nullable Signature<? extends Result> signature;
    private final Map<Input, Expression<?>> checkedInputs = new HashMap<>();
    private final boolean resolved;
    private boolean valid = true;

    public DynamicFunctionReference(Function<? extends Result> function) {
        this.resolved = true;
        this.function = new WeakReference<>(function);
        this.name = function.getName();
        this.signature = function.getSignature();
        this.source = null;
        this.sourceName = this.signature.namespace();
    }

    public DynamicFunctionReference(@NotNull String name) {
        this(name, null);
    }

    @SuppressWarnings("unchecked")
    public DynamicFunctionReference(@NotNull String name, @Nullable Script source) {
        this(name, source == null ? null : source.getConfig().getFileName(), source);
    }

    @SuppressWarnings("unchecked")
    private DynamicFunctionReference(@NotNull String name, @Nullable String sourceName, @Nullable Script source) {
        this.name = name;
        Function<? extends Result> function;
        if (sourceName != null) {
            function = (Function<? extends Result>) Functions.getFunction(name, sourceName);
        } else {
            function = (Function<? extends Result>) Functions.getFunction(name, null);
        }
        this.resolved = function != null;
        this.function = new WeakReference<>(function);
        this.signature = function != null ? function.getSignature() : null;
        this.source = source;
        this.sourceName = sourceName != null ? sourceName : (this.signature == null ? null : this.signature.namespace());
    }

    public @Nullable Script source() {
        return source;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    public boolean isSingle(Expression<?>... arguments) {
        if (!resolved || signature == null) {
            return true;
        }
        return signature.isSingle();
    }

    public @Nullable Class<?> getReturnType(Expression<?>... arguments) {
        if (!resolved || signature == null) {
            return Object.class;
        }
        return signature.returnType();
    }

    public Result @Nullable [] execute(SkriptEvent event, Object... arguments) {
        if (!valid()) {
            return null;
        }
        Function<? extends Result> function = this.function.get();
        if (function == null) {
            return null;
        }
        Object[][] consigned = FunctionReference.consign(arguments);
        try {
            return function.execute(consigned);
        } finally {
            function.resetReturnValue();
        }
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public boolean valid() {
        return resolved && valid && function.get() != null && (source == null || source.valid());
    }

    @Override
    public String toString() {
        if (source != null) {
            return name + "() from " + source.nameAndPath();
        }
        if (sourceName != null && !sourceName.isBlank()) {
            return name + "() from " + sourceName;
        }
        return name + "()";
    }

    public @Nullable Expression<?> validate(Expression<?>[] parameters) {
        Input input = new Input(parameters);
        return validate(input);
    }

    public @Nullable Expression<?> validate(Input input) {
        if (checkedInputs.containsKey(input)) {
            return checkedInputs.get(input);
        }
        checkedInputs.put(input, null);
        if (signature == null) {
            return null;
        }
        boolean varArgs = signature.getMaxParameters() == 1 && !signature.getParameter(0).isSingle();
        Expression<?>[] inputParameters = input.parameters();

        if (inputParameters.length > signature.getMaxParameters() && !varArgs) {
            return null;
        }
        if (inputParameters.length < signature.getMinParameters()) {
            return null;
        }
        Expression<?>[] checked = new Expression[inputParameters.length];
        for (int i = 0; i < inputParameters.length; i++) {
            Parameter<?> parameter = signature.getParameters()[varArgs ? 0 : i];
            Class<?> target = component(parameter.type());
            Expression<?> expression = inputParameters[i].getConvertedExpression(target);
            if (expression == null) {
                return null;
            }
            if (parameter.isSingle() && !expression.isSingle()) {
                return null;
            }
            checked[i] = expression;
        }
        ExpressionList<?> result = new ExpressionList<>(checked, Object.class, true);
        checkedInputs.put(input, result);
        return result;
    }

    private static Class<?> component(Class<?> type) {
        return type.isArray() ? type.getComponentType() : type;
    }

    public static @Nullable DynamicFunctionReference<?> parseFunction(String name) {
        if (name.contains(") from ")) {
            String source = name.substring(name.lastIndexOf(" from ") + 6).trim();
            return resolveFunction(name.substring(0, name.lastIndexOf(" from ")).trim(), source);
        }
        return resolveFunction(name, null);
    }

    public static @Nullable DynamicFunctionReference<?> resolveFunction(String name, @Nullable String sourceScript) {
        String clean = name;
        if (clean.contains("(") && clean.contains(")")) {
            clean = clean.replaceAll("\\(.*\\).*", "").trim();
        }
        String resolvedSource = normalizeSourceScript(sourceScript);
        Script source = getScript(resolvedSource);
        DynamicFunctionReference<Object> reference = resolvedSource == null
                ? new DynamicFunctionReference<>(clean)
                : new DynamicFunctionReference<>(clean, resolvedSource, source);
        return reference.valid() ? reference : null;
    }

    private static @Nullable String normalizeSourceScript(@Nullable String sourceScript) {
        if (sourceScript == null || sourceScript.isBlank()) {
            return null;
        }
        return Functions.getScriptNamespace(sourceScript) != null ? sourceScript : null;
    }

    private static @Nullable Script getScript(@Nullable String sourceScript) {
        if (sourceScript == null || sourceScript.isBlank()) {
            return null;
        }
        return Functions.getScript(sourceScript);
    }

    public static final class Input {
        private final Class<?>[] types;
        private final transient Expression<?>[] parameters;

        public Input(Expression<?>... parameters) {
            this.parameters = parameters;
            this.types = new Class<?>[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                types[i] = parameters[i].getReturnType();
            }
        }

        private Expression<?>[] parameters() {
            return parameters;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Input input)) {
                return false;
            }
            return Arrays.equals(parameters, input.parameters) && Arrays.equals(types, input.types);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(types), Arrays.hashCode(parameters));
        }
    }
}
