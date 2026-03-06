package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * Static function utilities and global namespace access.
 */
public abstract class Functions {

    private Functions() {
    }

    public static @Nullable Function<?> currentFunction = null;

    private static final Map<Namespace.Key, Namespace> namespaces = new HashMap<>();
    private static final Namespace javaNamespace;
    private static final Map<String, Namespace> globalFunctions = new HashMap<>();
    private static final Collection<FunctionReference<?>> toValidate = new ArrayList<>();

    public static boolean callFunctionEvents = false;

    static {
        javaNamespace = new Namespace();
        namespaces.put(new Namespace.Key(Namespace.Origin.JAVA, "unknown"), javaNamespace);
    }

    public static final String functionNamePattern = "[\\p{IsAlphabetic}_][\\p{IsAlphabetic}\\d_]*";

    public static Function<?> register(Function<?> function) {
        Skript.checkAcceptRegistrations();
        String name = function.getName();
        if (!name.matches(functionNamePattern)) {
            throw new SkriptAPIException("Invalid function name '" + name + "'");
        }
        javaNamespace.addSignature(function.getSignature());
        javaNamespace.addFunction(function);
        globalFunctions.put(name, javaNamespace);
        FunctionRegistry.getRegistry().register(null, function);
        return function;
    }

    public static @Nullable Signature<?> registerSignature(Signature<?> signature) {
        Parameter<?>[] parameters = signature.getParameters();
        Class<?>[] types = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].type();
        }
        FunctionRegistry.Retrieval<Signature<?>> existing = FunctionRegistry.getRegistry()
                .getExactSignature(signature.namespace(), signature.getName(), types);
        if (existing.result() == FunctionRegistry.RetrievalResult.EXACT
                && existing.retrieved() != null
                && existing.retrieved().isLocal() == signature.isLocal()) {
            return null;
        }

        Namespace.Key namespaceKey = new Namespace.Key(Namespace.Origin.SCRIPT, signature.namespace());
        Namespace namespace = namespaces.computeIfAbsent(namespaceKey, ignored -> new Namespace());
        if (namespace.getSignature(signature.getName()) == null) {
            namespace.addSignature(signature);
        }
        if (!signature.isLocal()) {
            globalFunctions.put(signature.getName(), namespace);
        }
        FunctionRegistry registry = FunctionRegistry.getRegistry();
        if (signature.isLocal()) {
            registry.register(signature.namespace(), signature);
        } else {
            registry.register(null, signature);
        }
        return signature;
    }

    public static @Nullable Function<?> getGlobalFunction(String name) {
        Namespace namespace = globalFunctions.get(name);
        if (namespace == null) {
            return null;
        }
        return namespace.getFunction(name, false);
    }

    public static @Nullable Signature<?> getGlobalSignature(String name) {
        Namespace namespace = globalFunctions.get(name);
        if (namespace == null) {
            return null;
        }
        return namespace.getSignature(name, false);
    }

    public static @Nullable Function<?> getLocalFunction(String name, String script) {
        Namespace namespace = getScriptNamespace(script);
        if (namespace == null) {
            return null;
        }
        return namespace.getFunction(name);
    }

    public static @Nullable Signature<?> getLocalSignature(String name, String script) {
        Namespace namespace = getScriptNamespace(script);
        if (namespace == null) {
            return null;
        }
        return namespace.getSignature(name);
    }

    public static @Nullable Function<?> getFunction(String name, @Nullable String script) {
        if (script != null) {
            Function<?> local = getLocalFunction(name, script);
            if (local != null) {
                return local;
            }
        }
        return getGlobalFunction(name);
    }

    public static @Nullable Signature<?> getSignature(String name, @Nullable String script) {
        if (script != null) {
            Signature<?> local = getLocalSignature(name, script);
            if (local != null) {
                return local;
            }
        }
        return getGlobalSignature(name);
    }

    public static @Nullable Namespace getScriptNamespace(String script) {
        return namespaces.get(new Namespace.Key(Namespace.Origin.SCRIPT, script));
    }

    public static boolean unregisterSignature(Signature<?> signature) {
        Namespace namespace = signature.isLocal()
                ? getScriptNamespace(signature.namespace())
                : globalFunctions.get(signature.getName());
        if (namespace == null) {
            return false;
        }
        return namespace.removeSignature(signature);
    }

    public static int clearFunctions(String script) {
        Namespace namespace = namespaces.remove(new Namespace.Key(Namespace.Origin.SCRIPT, script));
        if (namespace == null) {
            return 0;
        }
        globalFunctions.values().removeIf(candidate -> candidate == namespace);
        for (Signature<?> signature : namespace.getSignatures()) {
            for (Object call : signature.calls()) {
                if (call instanceof FunctionReference<?> reference && !script.equals(reference.namespace())) {
                    toValidate.add(reference);
                }
            }
        }
        return namespace.getSignatures().size();
    }

    public static void unregisterFunction(Signature<?> signature) {
        FunctionRegistry.getRegistry().remove(signature);
        Iterator<Namespace> iterator = namespaces.values().iterator();
        while (iterator.hasNext()) {
            Namespace namespace = iterator.next();
            if (!namespace.removeSignature(signature)) {
                continue;
            }
            if (!signature.isLocal()) {
                globalFunctions.remove(signature.getName());
            }
            if (namespace.getSignatures().isEmpty()) {
                iterator.remove();
            }
            break;
        }
        for (Object call : signature.calls()) {
            if (call instanceof FunctionReference<?> reference
                    && signature.namespace() != null
                    && !signature.namespace().equals(reference.namespace())) {
                toValidate.add(reference);
            }
        }
    }

    public static void validateFunctions() {
        for (FunctionReference<?> reference : toValidate) {
            reference.validate();
        }
        toValidate.clear();
    }

    public static Collection<Function<?>> getFunctions() {
        return javaNamespace.getFunctions();
    }

    public static void clear() {
        namespaces.clear();
        globalFunctions.clear();
        toValidate.clear();
        javaNamespace.signatures.clear();
        javaNamespace.functions.clear();
        FunctionRegistry.getRegistry().clear();
        namespaces.put(new Namespace.Key(Namespace.Origin.JAVA, "unknown"), javaNamespace);
    }
}
