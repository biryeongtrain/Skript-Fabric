package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Legacy compatibility implementation of Skript variables.
 */
public class Variable<T> implements Expression<T>, KeyReceiverExpression<T>, KeyProviderExpression<T> {

    private static final String SINGLE_SEPARATOR_CHAR = ":";
    public static final String SEPARATOR = SINGLE_SEPARATOR_CHAR + SINGLE_SEPARATOR_CHAR;
    public static final String LOCAL_VARIABLE_TOKEN = "_";
    public static final String EPHEMERAL_VARIABLE_TOKEN = "-";
    private static final char[] RESERVED_TOKENS = {'~', '.', '+', '$', '!', '&', '^', '*'};

    private final VariableString name;
    private final Class<T> superType;
    private final Class<? extends T>[] types;

    private final boolean local;
    private final boolean ephemeral;
    private final boolean list;

    private final @Nullable Variable<?> source;
    private final Map<SkriptEvent, String[]> cache = java.util.Collections.synchronizedMap(new WeakHashMap<>());

    @SuppressWarnings("unchecked")
    private Variable(
            VariableString name,
            Class<? extends T>[] types,
            boolean local,
            boolean ephemeral,
            boolean list,
            @Nullable Variable<?> source
    ) {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("Variable types must not be empty");
        }
        this.name = name;
        this.types = Arrays.copyOf(types, types.length);
        this.superType = (Class<T>) Utils.getSuperType(types);
        this.local = local;
        this.ephemeral = ephemeral;
        this.list = list;
        this.source = source;
    }

    /**
     * Checks whether a string is a valid variable name.
     *
     * @param name name to test
     * @param allowListVariable whether list variables are allowed
     * @param printErrors whether parsing errors should be logged
     * @return true if valid
     */
    public static boolean isValidVariableName(String name, boolean allowListVariable, boolean printErrors) {
        if (name == null || name.isBlank()) {
            if (printErrors) {
                Skript.error("A variable's name must not be empty");
            }
            return false;
        }

        char first = name.charAt(0);
        for (char token : RESERVED_TOKENS) {
            if (first == token && printErrors) {
                Skript.warning("The character '" + token + "' is reserved at the start of variable names.");
            }
        }

        String strippedName = stripVariablePrefix(name).trim();
        if (strippedName.isEmpty()) {
            if (printErrors) {
                Skript.error("A variable's name must not be empty");
            }
            return false;
        }

        if (!allowListVariable && strippedName.contains(SEPARATOR)) {
            if (printErrors) {
                Skript.error("List variables are not allowed here (error in variable {" + strippedName + "})");
            }
            return false;
        }
        if (strippedName.startsWith(SEPARATOR) || strippedName.endsWith(SEPARATOR)) {
            if (printErrors) {
                Skript.error("A variable's name must neither start nor end with '" + SEPARATOR + "' (error in variable {"
                        + strippedName + "})");
            }
            return false;
        }
        int asteriskIndex = strippedName.indexOf('*');
        if (asteriskIndex >= 0) {
            boolean validListAsterisk = allowListVariable
                    && asteriskIndex == strippedName.length() - 1
                    && strippedName.endsWith(SEPARATOR + "*")
                    && strippedName.indexOf('*', asteriskIndex + 1) < 0;
            if (!validListAsterisk) {
                if (printErrors) {
                    Skript.error("A variable's name must not contain asterisks except as '"
                            + SEPARATOR + "*' at the end (error in variable {" + strippedName + "})");
                }
                return false;
            }
        }
        if (strippedName.contains(SEPARATOR + SEPARATOR)) {
            if (printErrors) {
                Skript.error("A variable's name must not contain '" + SEPARATOR + "' multiple times in a row (error in variable {"
                        + strippedName + "})");
            }
            return false;
        }
        if (strippedName.replace(SEPARATOR, "").contains(SINGLE_SEPARATOR_CHAR) && printErrors) {
            Skript.warning("If you meant to create a list variable, use '" + SEPARATOR + "' instead of a single ':'.");
        }
        return true;
    }

    /**
     * Creates a variable with logging enabled.
     */
    public static <T> @Nullable Variable<T> newInstance(String name, Class<? extends T>[] types) {
        String rawName = name == null ? "" : name.trim();
        if (!isValidVariableName(rawName, true, true)) {
            return null;
        }

        boolean isLocal = rawName.startsWith(LOCAL_VARIABLE_TOKEN);
        boolean isEphemeral = rawName.startsWith(EPHEMERAL_VARIABLE_TOKEN);
        boolean isPlural = rawName.endsWith(SEPARATOR + "*");

        String stripped = stripVariablePrefix(rawName).trim();
        VariableString variableString = VariableString.newInstance(stripped, StringMode.VARIABLE_NAME);
        if (variableString == null) {
            return null;
        }

        return new Variable<>(variableString, types, isLocal, isEphemeral, isPlural, null);
    }

    private static String stripVariablePrefix(String name) {
        if (name.startsWith(LOCAL_VARIABLE_TOKEN) || name.startsWith(EPHEMERAL_VARIABLE_TOKEN)) {
            return name.substring(1);
        }
        return name;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public boolean isList() {
        return list;
    }

    public VariableString getName() {
        return name;
    }

    @Override
    public boolean isSingle() {
        return !list;
    }

    @Override
    public Class<? extends T> getReturnType() {
        return superType;
    }

    @Override
    public Class<? extends T>[] possibleReturnTypes() {
        return Arrays.copyOf(types, types.length);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        StringBuilder stringBuilder = new StringBuilder().append("{");
        if (local) {
            stringBuilder.append(LOCAL_VARIABLE_TOKEN);
        } else if (ephemeral) {
            stringBuilder.append(EPHEMERAL_VARIABLE_TOKEN);
        }
        stringBuilder.append(name.toString(event, debug)).append("}");
        if (debug) {
            stringBuilder.append(" as ").append(superType.getName());
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return toString(null, false);
    }

    @Override
    public @Nullable T getSingle(SkriptEvent event) {
        if (list) {
            T[] values = getArray(event);
            return values.length == 0 ? null : values[0];
        }
        Object value = Variables.getVariable(name.toString(event), event, local);
        return convertValue(value);
    }

    @Override
    public T[] getArray(SkriptEvent event) {
        if (!list) {
            T single = getSingle(event);
            if (single == null) {
                return emptyArray();
            }
            T[] one = newArray(1);
            one[0] = single;
            return one;
        }
        String prefix = listPrefix(event);
        Map<String, Object> values = Variables.getVariablesWithPrefix(prefix, event, local);
        List<T> converted = new ArrayList<>(values.size());
        for (Object value : values.values()) {
            T parsed = convertValue(value);
            if (parsed != null) {
                converted.add(parsed);
            }
        }
        T[] array = newArray(converted.size());
        for (int i = 0; i < converted.size(); i++) {
            array[i] = converted.get(i);
        }
        return array;
    }

    @Override
    public T[] getAll(SkriptEvent event) {
        return getArray(event);
    }

    @Override
    public @NotNull String[] getArrayKeys(SkriptEvent event) throws IllegalStateException {
        if (!list) {
            throw new IllegalStateException("Non-list variables do not expose keys");
        }
        String[] cached = cache.get(event);
        if (cached != null) {
            return Arrays.copyOf(cached, cached.length);
        }
        String prefix = listPrefix(event);
        Map<String, Object> values = Variables.getVariablesWithPrefix(prefix, event, local);
        String[] keys = values.keySet().stream()
                .map(fullKey -> fullKey.substring(prefix.length()))
                .toArray(String[]::new);
        cache.put(event, keys);
        return Arrays.copyOf(keys, keys.length);
    }

    @Override
    public boolean canReturnKeys() {
        return list;
    }

    @Override
    public boolean areKeysRecommended() {
        return false;
    }

    @Override
    public Class<?>[] acceptChange(ChangeMode mode) {
        return new Class[]{Object.class};
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        cache.remove(event);
        if (list) {
            changeList(event, delta, mode, null);
            return;
        }
        changeSingle(event, delta, mode);
    }

    @Override
    public void change(SkriptEvent event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys) {
        cache.remove(event);
        if (!list) {
            change(event, delta, mode);
            return;
        }
        changeList(event, delta, mode, keys);
    }

    @Override
    public boolean setTime(int time) {
        return false;
    }

    @Override
    public int getTime() {
        return 0;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public Expression<?> getSource() {
        return source == null ? this : source;
    }

    @Override
    public Expression<T> simplify() {
        return this;
    }

    @Override
    public boolean isLoopOf(String input) {
        return list && input != null && input.equalsIgnoreCase("index");
    }

    private void changeSingle(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        String key = name.toString(event);
        Object current = Variables.getVariable(key, event, local);
        switch (mode) {
            case SET -> Variables.setVariable(key, convertDeltaValue(firstDelta(delta)), event, local);
            case ADD -> Variables.setVariable(key, addValues(current, firstDelta(delta)), event, local);
            case REMOVE -> Variables.setVariable(key, removeValues(current, firstDelta(delta)), event, local);
            case RESET, DELETE -> Variables.setVariable(key, null, event, local);
        }
    }

    private void changeList(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode, @Nullable String[] keys) {
        String prefix = listPrefix(event);
        if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            if (keys == null || keys.length == 0) {
                Variables.removePrefix(prefix, event, local);
            } else {
                for (String key : keys) {
                    Variables.setVariable(prefix + key, null, event, local);
                }
            }
            return;
        }

        if (keys != null && keys.length > 0) {
            for (int i = 0; i < keys.length; i++) {
                Object value = resolveIndexedDelta(delta, i);
                applyListKeyChange(event, prefix + keys[i], value, mode);
            }
            return;
        }

        Object[] values = nonNullDelta(delta);
        if (mode == ChangeMode.SET) {
            Variables.removePrefix(prefix, event, local);
        }
        if (values.length == 0) {
            return;
        }

        if (mode == ChangeMode.REMOVE) {
            Map<String, Object> existing = Variables.getVariablesWithPrefix(prefix, event, local);
            for (Map.Entry<String, Object> entry : existing.entrySet()) {
                for (Object value : values) {
                    if (Objects.equals(entry.getValue(), value)) {
                        Variables.setVariable(entry.getKey(), null, event, local);
                        break;
                    }
                }
            }
            return;
        }

        AtomicInteger nextIndex = new AtomicInteger(nextListIndex(prefix, event));
        for (Object value : values) {
            if (mode == ChangeMode.SET) {
                int index = nextIndex.getAndIncrement();
                Variables.setVariable(prefix + index, convertDeltaValue(value), event, local);
            } else if (mode == ChangeMode.ADD) {
                int index = nextIndex.getAndIncrement();
                Variables.setVariable(prefix + index, convertDeltaValue(value), event, local);
            }
        }
    }

    private void applyListKeyChange(SkriptEvent event, String fullKey, @Nullable Object delta, ChangeMode mode) {
        Object current = Variables.getVariable(fullKey, event, local);
        switch (mode) {
            case SET -> Variables.setVariable(fullKey, convertDeltaValue(delta), event, local);
            case ADD -> Variables.setVariable(fullKey, addValues(current, delta), event, local);
            case REMOVE -> Variables.setVariable(fullKey, removeValues(current, delta), event, local);
            case RESET, DELETE -> Variables.setVariable(fullKey, null, event, local);
        }
    }

    private int nextListIndex(String prefix, SkriptEvent event) {
        Map<String, Object> existing = Variables.getVariablesWithPrefix(prefix, event, local);
        int max = 0;
        for (String key : existing.keySet()) {
            String suffix = key.substring(prefix.length());
            try {
                max = Math.max(max, Integer.parseInt(suffix));
            } catch (NumberFormatException ignored) {
                // non-numeric keys are ignored for append indexing
            }
        }
        return max + 1;
    }

    private Object addValues(@Nullable Object current, @Nullable Object delta) {
        if (current == null) {
            return convertDeltaValue(delta);
        }
        if (delta == null) {
            return current;
        }
        if (current instanceof Number left && delta instanceof Number right) {
            if (current instanceof Double || current instanceof Float || delta instanceof Double || delta instanceof Float) {
                return left.doubleValue() + right.doubleValue();
            }
            return left.longValue() + right.longValue();
        }
        if (current instanceof String leftString) {
            return leftString + delta;
        }
        return current;
    }

    private @Nullable Object removeValues(@Nullable Object current, @Nullable Object delta) {
        if (current == null) {
            return null;
        }
        if (delta == null) {
            return null;
        }
        if (current instanceof Number left && delta instanceof Number right) {
            if (current instanceof Double || current instanceof Float || delta instanceof Double || delta instanceof Float) {
                return left.doubleValue() - right.doubleValue();
            }
            return left.longValue() - right.longValue();
        }
        if (Objects.equals(current, delta)) {
            return null;
        }
        return current;
    }

    private String listPrefix(SkriptEvent event) {
        String resolved = name.toString(event);
        if (resolved.endsWith(SEPARATOR + "*")) {
            return resolved.substring(0, resolved.length() - 1);
        }
        if (resolved.endsWith(SEPARATOR)) {
            return resolved;
        }
        return resolved + SEPARATOR;
    }

    private @Nullable Object resolveIndexedDelta(Object @Nullable [] delta, int index) {
        if (delta == null || delta.length == 0) {
            return null;
        }
        if (index < delta.length) {
            return delta[index];
        }
        return delta[0];
    }

    private Object[] nonNullDelta(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0) {
            return new Object[0];
        }
        List<Object> values = new ArrayList<>(delta.length);
        for (Object value : delta) {
            if (value != null) {
                values.add(value);
            }
        }
        return values.toArray();
    }

    private @Nullable Object firstDelta(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0) {
            return null;
        }
        return delta[0];
    }

    private @Nullable Object convertDeltaValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (superType == Object.class || superType.isInstance(value)) {
            return value;
        }
        return Converters.convert(value, superType);
    }

    private @Nullable T convertValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (superType.isInstance(value)) {
            return superType.cast(value);
        }
        T converted = Converters.convert(value, types);
        if (converted != null) {
            return converted;
        }
        return Converters.convert(value, superType);
    }

    @SuppressWarnings("unchecked")
    private T[] newArray(int size) {
        Class<?> component = superType == Object.class ? Classes.getSuperClassInfo((Class<T>) Object.class).getC() : superType;
        return (T[]) Array.newInstance(component, size);
    }

    private T[] emptyArray() {
        return newArray(0);
    }
}
