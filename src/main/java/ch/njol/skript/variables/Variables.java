package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.Skript;

/**
 * Minimal variable storage bridge for legacy {@code ch.njol.skript.lang.Variable}.
 * Global and local variables are stored separately; local scope is keyed by event handle.
 */
public final class Variables {

    public static boolean caseInsensitiveVariables = true;
    static final ReadWriteLock variablesLock = new ReentrantReadWriteLock(true);
    static final List<VariablesStorage> STORAGES = new ArrayList<>();
    static final Queue<VariableChange> changeQueue = new ConcurrentLinkedQueue<>();
    private static final Map<Object, VariablesMap> LOCAL_VARIABLES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final VariablesMap GLOBAL_VARIABLES = new VariablesMap();

    private Variables() {
    }

    public static @Nullable Object getVariable(String name, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = normalizeName(name);
        if (local) {
            VariablesMap map = localMap(event, false);
            synchronized (map) {
                return map.getVariable(normalized);
            }
        }
        variablesLock.readLock().lock();
        try {
            if (!changeQueue.isEmpty()) {
                VariableChange latest = null;
                for (VariableChange change : changeQueue) {
                    if (change.name.equals(normalized)) {
                        latest = change;
                    }
                }
                if (latest != null) {
                    return latest.value;
                }
            }
            return GLOBAL_VARIABLES.getVariable(normalized);
        } finally {
            variablesLock.readLock().unlock();
        }
    }

    public static void setVariable(String name, @Nullable Object value, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return;
        }
        String normalized = normalizeName(name);
        if (normalized.endsWith(Variable.SEPARATOR + "*") && value == null) {
            removePrefix(normalized.substring(0, normalized.length() - 1), event, local);
            return;
        }
        if (local) {
            VariablesMap map = localMap(event, true);
            synchronized (map) {
                map.setVariable(normalized, value);
            }
            return;
        }
        setGlobalVariable(normalized, value);
    }

    public static void deleteVariable(String name, @Nullable SkriptEvent event, boolean local) {
        setVariable(name, null, event, local);
    }

    static Lock getReadLock() {
        return variablesLock.readLock();
    }

    static java.util.TreeMap<String, Object> getVariables() {
        return GLOBAL_VARIABLES.treeMap;
    }

    static void processChangeQueue() {
        while (true) {
            VariableChange change = changeQueue.poll();
            if (change == null) {
                return;
            }
            GLOBAL_VARIABLES.setVariable(change.name, change.value);
            saveVariableChange(change.name, change.value);
        }
    }

    static boolean variableLoaded(String name, @Nullable Object value, VariablesStorage source) {
        if (value == null || name == null || name.isBlank()) {
            return false;
        }
        String normalized = normalizeName(name);
        variablesLock.writeLock().lock();
        try {
            GLOBAL_VARIABLES.setVariable(normalized, value);
        } finally {
            variablesLock.writeLock().unlock();
        }
        if (STORAGES.isEmpty()) {
            return true;
        }
        for (VariablesStorage storage : STORAGES) {
            if (storage.accept(normalized)) {
                return true;
            }
        }
        return source.accept(normalized);
    }

    public static void removePrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        String normalized = normalizeName(prefix);
        VariablesMap map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        synchronized (map) {
            map.setVariable(normalized + "*", null);
        }
    }

    public static Map<String, Object> getVariablesWithPrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return Map.of();
        }
        String normalized = normalizeName(prefix);
        VariablesMap map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        Object rawValue;
        synchronized (map) {
            rawValue = map.getVariable(normalized + "*");
        }
        if (!(rawValue instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }
        Map<String, Object> matches = new java.util.TreeMap<>(VariablesMap.VARIABLE_NAME_COMPARATOR);
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof String childKey)) {
                continue;
            }
            Object childValue = entry.getValue();
            if (childValue instanceof Map<?, ?> sublist) {
                childValue = sublist.get(null);
            }
            if (childValue != null) {
                matches.put(normalized + childKey, childValue);
            }
        }
        return new LinkedHashMap<>(matches);
    }

    public static void clearAll() {
        variablesLock.writeLock().lock();
        try {
            GLOBAL_VARIABLES.clear();
            changeQueue.clear();
            STORAGES.clear();
        } finally {
            variablesLock.writeLock().unlock();
        }
        LOCAL_VARIABLES.clear();
    }

    public static void withLocalVariables(
            @Nullable SkriptEvent source,
            @Nullable SkriptEvent target,
            Runnable action
    ) {
        Object sourceKey = eventScopeKey(source);
        Object targetKey = eventScopeKey(target);
        VariablesMap sourceLocals;
        synchronized (LOCAL_VARIABLES) {
            sourceLocals = LOCAL_VARIABLES.get(sourceKey);
            if (sourceLocals == null
                    || (sourceLocals.hashMap.isEmpty() && sourceLocals.treeMap.isEmpty())) {
                LOCAL_VARIABLES.remove(targetKey);
            } else {
                LOCAL_VARIABLES.put(targetKey, sourceLocals.copy());
            }
        }
        try {
            action.run();
        } finally {
            synchronized (LOCAL_VARIABLES) {
                VariablesMap targetLocals = LOCAL_VARIABLES.get(targetKey);
                if (targetLocals == null
                        || (targetLocals.hashMap.isEmpty() && targetLocals.treeMap.isEmpty())) {
                    LOCAL_VARIABLES.remove(sourceKey);
                } else {
                    LOCAL_VARIABLES.put(sourceKey, targetLocals.copy());
                }
                LOCAL_VARIABLES.remove(targetKey);
            }
        }
    }

    private static VariablesMap localMap(@Nullable SkriptEvent event, boolean create) {
        Object key = eventScopeKey(event);
        synchronized (LOCAL_VARIABLES) {
            if (!create) {
                VariablesMap map = LOCAL_VARIABLES.get(key);
                return map == null ? new VariablesMap() : map;
            }
            return LOCAL_VARIABLES.computeIfAbsent(key, ignored -> new VariablesMap());
        }
    }

    public static String[] splitVariableName(String name) {
        return name.split(java.util.regex.Pattern.quote(Variable.SEPARATOR));
    }

    static void registerLoadedStorage(VariablesStorage storage) {
        if (!STORAGES.contains(storage)) {
            STORAGES.add(storage);
        }
    }

    static void unregisterLoadedStorage(VariablesStorage storage) {
        STORAGES.remove(storage);
    }

    private static Object eventScopeKey(@Nullable SkriptEvent event) {
        if (event == null) {
            return Variables.class;
        }
        return event.handle() != null ? event.handle() : event;
    }

    private static String normalizeName(String name) {
        if (!caseInsensitiveVariables) {
            return name;
        }
        return name.toLowerCase(Locale.ENGLISH);
    }

    private static void setGlobalVariable(String name, @Nullable Object value) {
        if (variablesLock.writeLock().tryLock()) {
            try {
                if (!changeQueue.isEmpty()) {
                    processChangeQueue();
                }
                GLOBAL_VARIABLES.setVariable(name, value);
                saveVariableChange(name, value);
            } finally {
                variablesLock.writeLock().unlock();
            }
            return;
        }
        changeQueue.add(new VariableChange(name, value));
    }

    private static void saveVariableChange(String name, @Nullable Object value) {
        if (STORAGES.isEmpty()) {
            return;
        }
        SerializedVariable.Value serializedValue = SerializedVariable.serialize(value);
        if (value != null && serializedValue == null) {
            return;
        }
        for (VariablesStorage storage : STORAGES) {
            if (!storage.accept(name)) {
                continue;
            }
            try {
                storage.save(new SerializedVariable(name, serializedValue));
            } catch (Exception ex) {
                Skript.exception(ex, "Error saving variable named " + name);
            }
            return;
        }
    }

    private record VariableChange(String name, @Nullable Object value) {
    }
}
