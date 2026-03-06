package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Registry;

/**
 * Registry for function signatures and implementations.
 */
public final class FunctionRegistry implements Registry<Function<?>> {

    private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile(Functions.functionNamePattern);
    private static FunctionRegistry registry;

    public static FunctionRegistry getRegistry() {
        if (registry == null) {
            registry = new FunctionRegistry();
        }
        return registry;
    }

    private final NamespaceIdentifier globalNamespace = new NamespaceIdentifier(null);
    private final Map<NamespaceIdentifier, NamespaceData> namespaces = new ConcurrentHashMap<>();

    private FunctionRegistry() {
    }

    @Override
    public @Unmodifiable @NotNull Collection<Function<?>> elements() {
        Set<Function<?>> functions = new HashSet<>();
        for (NamespaceData data : namespaces.values()) {
            functions.addAll(data.functions.values());
        }
        return Collections.unmodifiableSet(functions);
    }

    public void clear() {
        namespaces.clear();
    }

    public void remove(@NotNull Signature<?> signature) {
        Objects.requireNonNull(signature, "signature");
        NamespaceIdentifier namespaceId = namespaceId(signature.isLocal() ? signature.namespace() : null);
        FunctionIdentifier identifier = FunctionIdentifier.of(signature);
        NamespaceData data = namespaces.get(namespaceId);
        if (data == null) {
            return;
        }
        synchronized (data) {
            data.signatures.remove(identifier);
            data.functions.remove(identifier);
            Set<FunctionIdentifier> identifiersWithName = data.identifiers.get(identifier.name);
            if (identifiersWithName != null) {
                identifiersWithName.remove(identifier);
                if (identifiersWithName.isEmpty()) {
                    data.identifiers.remove(identifier.name);
                }
            }
            if (data.signatures.isEmpty() && data.functions.isEmpty() && data.identifiers.isEmpty()) {
                namespaces.remove(namespaceId);
            }
        }
    }

    public void register(@Nullable String namespace, @NotNull Signature<?> signature) {
        Objects.requireNonNull(signature, "signature");
        validateLocality(namespace, signature.isLocal(), "signature");
        NamespaceIdentifier namespaceId = namespaceId(namespace);
        NamespaceData data = namespaces.computeIfAbsent(namespaceId, ignored -> new NamespaceData());
        FunctionIdentifier identifier = FunctionIdentifier.of(signature);

        synchronized (data) {
            Set<FunctionIdentifier> identifiersWithName = data.identifiers.computeIfAbsent(identifier.name, ignored -> new HashSet<>());
            if (!identifiersWithName.add(identifier)) {
                alreadyRegisteredError(signature.getName(), identifier, namespaceId);
            }
            Signature<?> previous = data.signatures.putIfAbsent(identifier, signature);
            if (previous != null) {
                alreadyRegisteredError(signature.getName(), identifier, namespaceId);
            }
        }
        Skript.debug("Registered function signature: " + signature);
    }

    public void register(@Nullable String namespace, @NotNull Function<?> function) {
        Objects.requireNonNull(function, "function");
        Signature<?> signature = function.getSignature();
        validateLocality(namespace, signature.isLocal(), "function");

        if (!FUNCTION_NAME_PATTERN.matcher(function.getName()).matches()) {
            throw new SkriptAPIException("Invalid function name '" + function.getName() + "'");
        }

        NamespaceIdentifier namespaceId = namespaceId(namespace);
        FunctionIdentifier identifier = FunctionIdentifier.of(signature);
        if (!signatureExists(namespaceId, identifier)) {
            register(namespace, signature);
        }

        NamespaceData data = namespaces.computeIfAbsent(namespaceId, ignored -> new NamespaceData());
        Function<?> previous = data.functions.putIfAbsent(identifier, function);
        if (previous != null) {
            alreadyRegisteredError(function.getName(), identifier, namespaceId);
        }
        Skript.debug("Registered function implementation: " + function);
    }

    public Retrieval<Signature<?>> getExactSignature(@Nullable String namespace, String name, Class<?>... args) {
        NamespaceIdentifier namespaceId = namespaceId(namespace);
        FunctionIdentifier identifier = new FunctionIdentifier(name, args.length, Arrays.copyOf(args, args.length));

        Signature<?> local = getExactSignature(namespaceId, identifier);
        if (local != null) {
            return new Retrieval<>(RetrievalResult.EXACT, local, null);
        }
        if (namespace != null) {
            Signature<?> global = getExactSignature(globalNamespace, identifier);
            if (global != null) {
                return new Retrieval<>(RetrievalResult.EXACT, global, null);
            }
        }
        return new Retrieval<>(RetrievalResult.NOT_REGISTERED, null, null);
    }

    public Retrieval<Signature<?>> getSignature(@Nullable String namespace, String name, Class<?>... args) {
        List<Signature<?>> candidates = new ArrayList<>();
        collectMatchingSignatures(candidates, namespaceId(namespace), name, args);
        if (namespace != null) {
            collectMatchingSignatures(candidates, globalNamespace, name, args);
        }
        return resolveRetrieval(candidates);
    }

    public Retrieval<Function<?>> getExactFunction(@Nullable String namespace, String name, Class<?>... args) {
        NamespaceIdentifier namespaceId = namespaceId(namespace);
        FunctionIdentifier identifier = new FunctionIdentifier(name, args.length, Arrays.copyOf(args, args.length));

        Function<?> local = getExactFunction(namespaceId, identifier);
        if (local != null) {
            return new Retrieval<>(RetrievalResult.EXACT, local, null);
        }
        if (namespace != null) {
            Function<?> global = getExactFunction(globalNamespace, identifier);
            if (global != null) {
                return new Retrieval<>(RetrievalResult.EXACT, global, null);
            }
        }
        return new Retrieval<>(RetrievalResult.NOT_REGISTERED, null, null);
    }

    public Retrieval<Function<?>> getFunction(@Nullable String namespace, String name, Class<?>... args) {
        List<Function<?>> candidates = new ArrayList<>();
        collectMatchingFunctions(candidates, namespaceId(namespace), name, args);
        if (namespace != null) {
            collectMatchingFunctions(candidates, globalNamespace, name, args);
        }
        return resolveRetrieval(candidates);
    }

    private void collectMatchingSignatures(List<Signature<?>> candidates, NamespaceIdentifier namespace, String name, Class<?>[] args) {
        NamespaceData data = namespaces.get(namespace);
        if (data == null) {
            return;
        }
        Set<FunctionIdentifier> identifiers = data.identifiers.get(name);
        if (identifiers == null) {
            return;
        }
        for (FunctionIdentifier identifier : identifiers) {
            if (!matches(args, identifier)) {
                continue;
            }
            Signature<?> signature = data.signatures.get(identifier);
            if (signature != null) {
                candidates.add(signature);
            }
        }
    }

    private void collectMatchingFunctions(List<Function<?>> candidates, NamespaceIdentifier namespace, String name, Class<?>[] args) {
        NamespaceData data = namespaces.get(namespace);
        if (data == null) {
            return;
        }
        Set<FunctionIdentifier> identifiers = data.identifiers.get(name);
        if (identifiers == null) {
            return;
        }
        for (FunctionIdentifier identifier : identifiers) {
            if (!matches(args, identifier)) {
                continue;
            }
            Function<?> function = data.functions.get(identifier);
            if (function != null) {
                candidates.add(function);
            }
        }
    }

    private static <T> Retrieval<T> resolveRetrieval(List<T> candidates) {
        if (candidates.isEmpty()) {
            return new Retrieval<>(RetrievalResult.NOT_REGISTERED, null, null);
        }
        if (candidates.size() == 1) {
            return new Retrieval<>(RetrievalResult.EXACT, candidates.getFirst(), null);
        }
        Class<?>[][] conflicts = new Class<?>[candidates.size()][];
        for (int i = 0; i < candidates.size(); i++) {
            Object candidate = candidates.get(i);
            if (candidate instanceof Signature<?> signature) {
                conflicts[i] = Arrays.stream(signature.getParameters()).map(Parameter::type).toArray(Class[]::new);
            } else if (candidate instanceof Function<?> function) {
                conflicts[i] = Arrays.stream(function.getParameters()).map(Parameter::type).toArray(Class[]::new);
            } else {
                conflicts[i] = new Class<?>[0];
            }
        }
        return new Retrieval<>(RetrievalResult.AMBIGUOUS, null, conflicts);
    }

    private @Nullable Signature<?> getExactSignature(NamespaceIdentifier namespace, FunctionIdentifier identifier) {
        NamespaceData data = namespaces.get(namespace);
        return data == null ? null : data.signatures.get(identifier);
    }

    private @Nullable Function<?> getExactFunction(NamespaceIdentifier namespace, FunctionIdentifier identifier) {
        NamespaceData data = namespaces.get(namespace);
        return data == null ? null : data.functions.get(identifier);
    }

    private boolean signatureExists(@NotNull NamespaceIdentifier namespace, @NotNull FunctionIdentifier identifier) {
        NamespaceData data = namespaces.get(namespace);
        return data != null && data.signatures.containsKey(identifier);
    }

    private static boolean matches(Class<?>[] provided, FunctionIdentifier expected) {
        if (expected.args.length == 1 && expected.args[0].isArray()) {
            if (provided.length < expected.minArgCount) {
                return false;
            }
            Class<?> arrayType = component(expected.args[0]);
            for (Class<?> providedArg : provided) {
                Class<?> from = component(providedArg);
                if (from == null || arrayType == null || !Converters.converterExists(from, arrayType)) {
                    return false;
                }
            }
            return true;
        }
        if (provided.length > expected.args.length || provided.length < expected.minArgCount) {
            return false;
        }
        for (int i = 0; i < provided.length; i++) {
            Class<?> from = component(provided[i]);
            Class<?> to = component(expected.args[i]);
            if (from == null || to == null) {
                return false;
            }
            if (to.isAssignableFrom(from)) {
                continue;
            }
            if (!Converters.converterExists(from, to)) {
                return false;
            }
        }
        return true;
    }

    private static @Nullable Class<?> component(@Nullable Class<?> type) {
        if (type == null) {
            return null;
        }
        return type.isArray() ? type.getComponentType() : type;
    }

    private NamespaceIdentifier namespaceId(@Nullable String namespace) {
        return namespace == null ? globalNamespace : new NamespaceIdentifier(namespace);
    }

    private static void validateLocality(@Nullable String namespace, boolean local, String type) {
        if (local && namespace == null) {
            throw new IllegalArgumentException("Cannot register a local " + type + " in the global namespace");
        }
        if (!local && namespace != null) {
            throw new IllegalArgumentException("Cannot register a global " + type + " in a local namespace");
        }
    }

    private static void alreadyRegisteredError(String name, FunctionIdentifier identifier, NamespaceIdentifier namespace) {
        String[] args = Arrays.stream(identifier.args).map(Class::getSimpleName).toArray(String[]::new);
        throw new SkriptAPIException("Function '" + name + "' with parameters " + Arrays.toString(args)
                + " is already registered in " + namespace);
    }

    public enum RetrievalResult {
        NOT_REGISTERED,
        AMBIGUOUS,
        EXACT
    }

    public record Retrieval<T>(
            @NotNull RetrievalResult result,
            @Nullable T retrieved,
            @Nullable Class<?>[][] conflictingArgs
    ) {
    }

    private static final class NamespaceData {
        private final Map<FunctionIdentifier, Signature<?>> signatures = new HashMap<>();
        private final Map<FunctionIdentifier, Function<?>> functions = new HashMap<>();
        private final Map<String, Set<FunctionIdentifier>> identifiers = new HashMap<>();
    }

    static final class NamespaceIdentifier {
        private final @Nullable String name;

        private NamespaceIdentifier(@Nullable String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof NamespaceIdentifier other)) {
                return false;
            }
            return Objects.equals(name, other.name);
        }

        @Override
        public String toString() {
            return name == null ? "<global>" : "'" + name + "'";
        }
    }

    static final class FunctionIdentifier {
        private final String name;
        private final int minArgCount;
        private final Class<?>[] args;

        private FunctionIdentifier(String name, int minArgCount, Class<?>[] args) {
            this.name = name;
            this.minArgCount = minArgCount;
            this.args = args;
        }

        static FunctionIdentifier of(Signature<?> signature) {
            Class<?>[] args = Arrays.stream(signature.getParameters())
                    .map(Parameter::type)
                    .toArray(Class[]::new);
            return new FunctionIdentifier(signature.getName(), signature.getMinParameters(), args);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + Arrays.hashCode(args);
            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof FunctionIdentifier other)) {
                return false;
            }
            return name.equals(other.name) && Arrays.equals(args, other.args);
        }
    }
}
