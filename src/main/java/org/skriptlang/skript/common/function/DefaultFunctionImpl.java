package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Signature;
import com.google.common.base.Preconditions;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;

final class DefaultFunctionImpl<T> extends ch.njol.skript.lang.function.Function<T> implements DefaultFunction<T> {

    private final SkriptAddon source;
    private final LinkedHashMap<String, Parameter<?>> parameters;
    private final java.util.function.Function<FunctionArguments, T> execute;
    private final List<String> description;
    private final List<String> since;
    private final List<String> examples;
    private final List<String> keywords;
    private final List<String> requires;

    DefaultFunctionImpl(
            SkriptAddon source,
            String name,
            LinkedHashMap<String, Parameter<?>> parameters,
            Class<T> returnType,
            boolean single,
            @Nullable ch.njol.skript.util.Contract contract,
            java.util.function.Function<FunctionArguments, T> execute,
            String[] description,
            String[] since,
            String[] examples,
            String[] keywords,
            String[] requires
    ) {
        super(new Signature<>(null, name, parameters.values().toArray(Parameter[]::new), returnType, single, contract));

        Preconditions.checkNotNull(source, "source cannot be null");
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(parameters, "parameters cannot be null");
        Preconditions.checkNotNull(returnType, "return type cannot be null");
        Preconditions.checkNotNull(execute, "execute cannot be null");

        this.source = source;
        this.parameters = parameters;
        this.execute = execute;
        this.description = description == null ? Collections.emptyList() : List.of(description);
        this.since = since == null ? Collections.emptyList() : List.of(since);
        this.examples = examples == null ? Collections.emptyList() : List.of(examples);
        this.keywords = keywords == null ? Collections.emptyList() : List.of(keywords);
        this.requires = requires == null ? Collections.emptyList() : List.of(requires);
    }

    @Override
    public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
        Map<String, Object> args = new LinkedHashMap<>();
        Parameter<?>[] arrayParameters = parameters.values().toArray(Parameter[]::new);
        int length = Math.min(arrayParameters.length, params.length);
        for (int i = 0; i < length; i++) {
            Object[] argument = params[i];
            Parameter<?> parameter = arrayParameters[i];
            if (argument == null || argument.length == 0) {
                if (parameter.hasModifier(Parameter.Modifier.OPTIONAL)) {
                    continue;
                }
                return null;
            }
            if (parameter.isSingle()) {
                args.put(parameter.name(), argument[0]);
            } else {
                args.put(parameter.name(), argument);
            }
        }

        T result = execute.apply(new FunctionArgumentsImpl(args));
        if (result == null) {
            return null;
        }
        if (result.getClass().isArray()) {
            @SuppressWarnings("unchecked")
            T[] cast = (T[]) result;
            return cast;
        }
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(result.getClass(), 1);
        array[0] = result;
        return array;
    }

    @Override
    public T execute(@NotNull FunctionEvent<?> event, @NotNull FunctionArguments arguments) {
        return execute.apply(arguments);
    }

    @Override
    public boolean resetReturnValue() {
        return true;
    }

    @Override
    public @NotNull String name() {
        return getName();
    }

    @Override
    public @Unmodifiable @NotNull List<String> description() {
        return description;
    }

    @Override
    public @Unmodifiable @NotNull List<String> since() {
        return since;
    }

    @Override
    public @Unmodifiable @NotNull List<String> examples() {
        return examples;
    }

    @Override
    public @Unmodifiable @NotNull List<String> keywords() {
        return keywords;
    }

    @Override
    public @Unmodifiable @NotNull List<String> requires() {
        return requires;
    }

    @Override
    public @NotNull SkriptAddon source() {
        return source;
    }

    static final class BuilderImpl<T> implements DefaultFunction.Builder<T> {
        private final SkriptAddon source;
        private final String name;
        private final Class<T> returnType;
        private final LinkedHashMap<String, Parameter<?>> parameters = new LinkedHashMap<>();
        private @Nullable ch.njol.skript.util.Contract contract;
        private String[] description;
        private String[] since;
        private String[] examples;
        private String[] keywords;
        private String[] requires;

        BuilderImpl(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
            this.source = Preconditions.checkNotNull(source, "source cannot be null");
            this.name = Preconditions.checkNotNull(name, "name cannot be null");
            this.returnType = Preconditions.checkNotNull(returnType, "returnType cannot be null");
        }

        @Override
        public Builder<T> contract(@NotNull ch.njol.skript.util.Contract contract) {
            this.contract = Preconditions.checkNotNull(contract, "contract cannot be null");
            return this;
        }

        @Override
        public Builder<T> description(@NotNull String @NotNull ... description) {
            this.description = copy(description, "description");
            return this;
        }

        @Override
        public Builder<T> since(@NotNull String @NotNull ... since) {
            this.since = copy(since, "since");
            return this;
        }

        @Override
        public Builder<T> examples(@NotNull String @NotNull ... examples) {
            this.examples = copy(examples, "examples");
            return this;
        }

        @Override
        public Builder<T> keywords(@NotNull String @NotNull ... keywords) {
            this.keywords = copy(keywords, "keywords");
            return this;
        }

        @Override
        public Builder<T> requires(@NotNull String @NotNull ... requires) {
            this.requires = copy(requires, "requires");
            return this;
        }

        @Override
        public Builder<T> parameter(
                @NotNull String name,
                @NotNull Class<?> type,
                Parameter.Modifier @NotNull ... modifiers
        ) {
            parameters.put(
                    Preconditions.checkNotNull(name, "name cannot be null"),
                    new DefaultParameter<>(name, Preconditions.checkNotNull(type, "type cannot be null"), Set.of(modifiers))
            );
            return this;
        }

        @Override
        public DefaultFunction<T> build(@NotNull java.util.function.Function<FunctionArguments, T> execute) {
            return new DefaultFunctionImpl<>(
                    source,
                    name,
                    parameters,
                    returnType,
                    !returnType.isArray(),
                    contract,
                    Preconditions.checkNotNull(execute, "execute cannot be null"),
                    description,
                    since,
                    examples,
                    keywords,
                    requires
            );
        }

        private static String[] copy(String[] values, String label) {
            Preconditions.checkNotNull(values, label + " cannot be null");
            for (String value : values) {
                Preconditions.checkNotNull(value, label + " contents cannot be null");
            }
            return values;
        }
    }

    private record DefaultParameter<T>(String name, Class<T> type, Set<Modifier> modifiers) implements Parameter<T> {
    }
}
