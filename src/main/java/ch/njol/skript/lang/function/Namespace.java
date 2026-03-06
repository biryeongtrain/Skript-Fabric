package ch.njol.skript.lang.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * Contains a set of functions and signatures.
 */
public class Namespace {

    /**
     * Origin of functions in this namespace.
     */
    public enum Origin {
        JAVA,
        SCRIPT
    }

    /**
     * Key identifying a namespace.
     */
    public static class Key {

        private final Origin origin;
        private final @Nullable String scriptName;

        public Key(Origin origin, @Nullable String scriptName) {
            this.origin = origin;
            this.scriptName = scriptName;
        }

        public Origin getOrigin() {
            return origin;
        }

        public @Nullable String getScriptName() {
            return scriptName;
        }

        @Override
        public int hashCode() {
            int result = origin.hashCode();
            result = 31 * result + (scriptName != null ? scriptName.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Key other)) {
                return false;
            }
            return origin == other.origin && Objects.equals(scriptName, other.scriptName);
        }
    }

    private static final class Info {

        private final String name;
        private final boolean local;

        private Info(String name, boolean local) {
            this.name = name;
            this.local = local;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (local ? 1 : 0);
            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Info other)) {
                return false;
            }
            return local == other.local && name.equals(other.name);
        }
    }

    final Map<Info, Signature<?>> signatures = new HashMap<>();
    final Map<Info, Function<?>> functions = new HashMap<>();

    public @Nullable Signature<?> getSignature(String name, boolean local) {
        return signatures.get(new Info(name, local));
    }

    public @Nullable Signature<?> getSignature(String name) {
        Signature<?> signature = getSignature(name, true);
        return signature == null ? getSignature(name, false) : signature;
    }

    public void addSignature(Signature<?> signature) {
        Info info = new Info(signature.getName(), signature.isLocal());
        if (signatures.containsKey(info)) {
            throw new IllegalArgumentException("function name already used");
        }
        signatures.put(info, signature);
    }

    public boolean removeSignature(Signature<?> signature) {
        Info info = new Info(signature.getName(), signature.isLocal());
        if (signatures.get(info) != signature) {
            return false;
        }
        signatures.remove(info);
        return true;
    }

    public Collection<Signature<?>> getSignatures() {
        return signatures.values();
    }

    public @Nullable Function<?> getFunction(String name, boolean local) {
        return functions.get(new Info(name, local));
    }

    public @Nullable Function<?> getFunction(String name) {
        Function<?> function = getFunction(name, true);
        return function == null ? getFunction(name, false) : function;
    }

    public void addFunction(Function<?> function) {
        Info info = new Info(function.getName(), function.getSignature().isLocal());
        if (!signatures.containsKey(info)) {
            signatures.put(info, function.getSignature());
        }
        functions.put(info, function);
    }

    public Collection<Function<?>> getFunctions() {
        return functions.values();
    }
}
