package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Tracks local variable type hints during parse-time scope transitions.
 */
public final class HintManager {

    private record Scope(Map<String, Set<Class<?>>> hints, boolean section) {
    }

    private final LinkedList<Scope> scopes = new LinkedList<>();
    private boolean active;

    public HintManager(boolean active) {
        this.active = active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active && ParserInstance.get().isActive();
    }

    public void enterScope(boolean section) {
        scopes.push(new Scope(new HashMap<>(), section));
        if (scopes.size() > 1) {
            mergeScope(1, 0, false);
        }
    }

    public void exitScope() {
        if (scopes.isEmpty()) {
            return;
        }
        if (scopes.size() > 1) {
            mergeScope(0, 1, false);
        }
        scopes.pop();
    }

    public void clearScope(int level, boolean sectionOnly) {
        if (!sectionOnly) {
            scopeAt(level).hints().clear();
            return;
        }
        scopeAt(level, true).hints().clear();
    }

    public void mergeScope(int from, int to, boolean sectionOnly) {
        Scope fromScope = sectionOnly ? scopeAt(from, true) : scopeAt(from);
        Scope toScope = sectionOnly ? scopeAt(to, true) : scopeAt(to);
        mergeHints(fromScope.hints(), toScope.hints());
    }

    public void set(Variable<?> variable, Class<?>... hints) {
        checkCanUseHints(variable);
        set(variable.getName().toString(null), hints);
    }

    public void set(String variableName, Class<?>... hints) {
        if (hintsUnavailable()) {
            return;
        }
        deleteInternal(variableName);
        if (hints.length > 0) {
            addInternal(variableName, Set.of(hints));
        }
    }

    public void add(Variable<?> variable, Class<?>... hints) {
        checkCanUseHints(variable);
        add(variable.getName().toString(null), hints);
    }

    public void add(String variableName, Class<?>... hints) {
        if (hintsUnavailable()) {
            return;
        }
        addInternal(variableName, Set.of(hints));
    }

    public void delete(Variable<?> variable) {
        checkCanUseHints(variable);
        delete(variable.getName().toString(null));
    }

    public void delete(String variableName) {
        if (hintsUnavailable()) {
            return;
        }
        deleteInternal(variableName);
    }

    public void remove(Variable<?> variable, Class<?>... hints) {
        checkCanUseHints(variable);
        remove(variable.getName().toString(null), hints);
    }

    public void remove(String variableName, Class<?>... hints) {
        if (hintsUnavailable()) {
            return;
        }
        String normalized = normalize(variableName);
        Set<Class<?>> current = scopes.getFirst().hints().get(normalized);
        if (current == null) {
            return;
        }
        for (Class<?> hint : hints) {
            current.remove(hint);
        }
        if (current.isEmpty()) {
            deleteInternal(normalized);
        }
    }

    public @Unmodifiable Set<Class<?>> get(Variable<?> variable) {
        checkCanUseHints(variable);
        return get(variable.getName().toString(null));
    }

    public @Unmodifiable Set<Class<?>> get(String variableName) {
        if (hintsUnavailable()) {
            return Set.of();
        }
        Set<Class<?>> hints = scopes.getFirst().hints().get(normalize(variableName));
        return hints == null ? Set.of() : Set.copyOf(hints);
    }

    public static boolean canUseHints(Variable<?> variable) {
        return variable.isLocal() && variable.getName().isSimple();
    }

    private static void checkCanUseHints(Variable<?> variable) {
        if (!canUseHints(variable)) {
            throw new IllegalArgumentException("Variables must be local and have a simple name to use hints");
        }
    }

    private void addInternal(String variableName, Set<Class<?>> hints) {
        String normalized = normalize(variableName);
        scopes.getFirst().hints().computeIfAbsent(normalized, key -> new HashSet<>()).addAll(hints);

        if (!normalized.isEmpty() && normalized.charAt(normalized.length() - 1) != '*') {
            int listEnd = normalized.lastIndexOf(Variable.SEPARATOR);
            if (listEnd != -1) {
                String listVariableName = normalized.substring(0, listEnd + Variable.SEPARATOR.length()) + "*";
                scopes.getFirst().hints().computeIfAbsent(listVariableName, key -> new HashSet<>()).addAll(hints);
            }
        }
    }

    private void deleteInternal(String variableName) {
        String normalized = normalize(variableName);
        scopes.getFirst().hints().remove(normalized);
        if (normalized.endsWith(Variable.SEPARATOR + "*")) {
            String prefix = normalized.substring(0, normalized.length() - 1);
            scopes.getFirst().hints().keySet().removeIf(key -> key.startsWith(prefix));
        }
    }

    private boolean hintsUnavailable() {
        return !isActive() || scopes.isEmpty();
    }

    private Scope scopeAt(int level) {
        if (level < 0 || level >= scopes.size()) {
            throw new IndexOutOfBoundsException("Scope level " + level + " is out of bounds");
        }
        return scopes.get(level);
    }

    private Scope scopeAt(int level, boolean sectionOnly) {
        if (!sectionOnly) {
            return scopeAt(level);
        }
        int sectionLevel = 0;
        for (Scope scope : scopes) {
            if (!scope.section()) {
                continue;
            }
            if (sectionLevel == level) {
                return scope;
            }
            sectionLevel++;
        }
        throw new IndexOutOfBoundsException("Section scope level " + level + " is out of bounds");
    }

    private static void mergeHints(Map<String, Set<Class<?>>> from, Map<String, Set<Class<?>>> to) {
        for (Map.Entry<String, Set<Class<?>>> entry : from.entrySet()) {
            to.computeIfAbsent(entry.getKey(), key -> new HashSet<>()).addAll(entry.getValue());
        }
    }

    private static String normalize(String variableName) {
        if (!Variables.caseInsensitiveVariables) {
            return variableName;
        }
        return variableName.toLowerCase(Locale.ENGLISH);
    }
}
