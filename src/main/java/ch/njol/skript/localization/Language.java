package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public final class Language {

    public static final int F_PLURAL = 1;
    public static final int F_DEFINITE_ARTICLE = 2;
    public static final int F_INDEFINITE_ARTICLE = 4;
    public static final int NO_ARTICLE_MASK = ~(F_DEFINITE_ARTICLE | F_INDEFINITE_ARTICLE);

    private static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*,\\s*");
    private static final HashMap<String, String> defaultLanguage = new HashMap<>();
    private static final List<LanguageChangeListener> listeners = new ArrayList<>();
    private static final int[] priorityStartIndices = new int[LanguageListenerPriority.values().length];

    private static @Nullable HashMap<String, String> localizedLanguage;
    private static String name = "english";

    private Language() {
    }

    public enum LanguageListenerPriority {
        EARLIEST, NORMAL, LATEST
    }

    public static String getName() {
        return name;
    }

    public static void clear() {
        defaultLanguage.clear();
        localizedLanguage = null;
        name = "english";
    }

    public static void loadDefault(Map<String, String> entries) {
        defaultLanguage.clear();
        entries.forEach((key, value) -> defaultLanguage.put(key.toLowerCase(Locale.ENGLISH), value));
        fireListeners();
    }

    public static void load(String languageName, Map<String, String> entries) {
        localizedLanguage = new HashMap<>();
        entries.forEach((key, value) -> localizedLanguage.put(key.toLowerCase(Locale.ENGLISH), value));
        name = languageName.toLowerCase(Locale.ENGLISH);
        fireListeners();
    }

    public static String get(String key) {
        String normalized = key.toLowerCase(Locale.ENGLISH);
        String value = get_i(normalized);
        return value == null ? normalized : value;
    }

    public static @Nullable String get_(String key) {
        return get_i(key.toLowerCase(Locale.ENGLISH));
    }

    public static void missingEntryError(String key) {
        Skript.error("Missing entry '" + key.toLowerCase(Locale.ENGLISH)
                + "' in the default/english language file");
    }

    public static String format(String key, Object... args) {
        String normalized = key.toLowerCase(Locale.ENGLISH);
        String value = get_i(normalized);
        if (value == null) {
            return normalized;
        }
        try {
            return String.format(value, args);
        } catch (Exception e) {
            Skript.error("Invalid format string at '" + normalized + "' in the "
                    + getName() + " language file: " + value);
            return normalized;
        }
    }

    public static String getSpaced(String key) {
        String value = get(key);
        return value.isEmpty() ? " " : " " + value + " ";
    }

    public static String[] getList(String key) {
        String value = get_i(key.toLowerCase(Locale.ENGLISH));
        return value == null ? new String[]{key.toLowerCase(Locale.ENGLISH)} : LIST_SPLIT_PATTERN.split(value);
    }

    public static boolean keyExists(String key) {
        String normalized = key.toLowerCase(Locale.ENGLISH);
        return defaultLanguage.containsKey(normalized)
                || (localizedLanguage != null && localizedLanguage.containsKey(normalized));
    }

    public static boolean keyExistsDefault(String key) {
        return defaultLanguage.containsKey(key.toLowerCase(Locale.ENGLISH));
    }

    public static boolean isInitialized() {
        return !defaultLanguage.isEmpty();
    }

    public static void addListener(LanguageChangeListener listener) {
        addListener(listener, LanguageListenerPriority.NORMAL);
    }

    public static void addListener(LanguageChangeListener listener, LanguageListenerPriority priority) {
        listeners.add(priorityStartIndices[priority.ordinal()], listener);
        for (int i = priority.ordinal() + 1; i < LanguageListenerPriority.values().length; i++) {
            priorityStartIndices[i]++;
        }
        if (isInitialized()) {
            listener.onLanguageChange();
        }
    }

    private static @Nullable String get_i(String key) {
        String value = defaultLanguage.get(key);
        if (value != null) {
            return value;
        }
        if (localizedLanguage != null) {
            return localizedLanguage.get(key);
        }
        return null;
    }

    private static void fireListeners() {
        for (LanguageChangeListener listener : listeners) {
            listener.onLanguageChange();
        }
    }
}
