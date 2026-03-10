package ch.njol.skript.util;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class Utils {

    private Utils() {
    }

    public static Class<?> getSuperType(Class<?>... types) {
        if (types == null || types.length == 0) {
            return Object.class;
        }
        Class<?> candidate = types[0] == null ? Object.class : types[0];
        for (int i = 1; i < types.length; i++) {
            Class<?> current = types[i];
            if (current == null) {
                return Object.class;
            }
            if (candidate.isAssignableFrom(current)) {
                continue;
            }
            if (current.isAssignableFrom(candidate)) {
                candidate = current;
                continue;
            }
            return Object.class;
        }
        return candidate;
    }

    public static Class<?> getSuperType(Class<?> first, Class<?> second) {
        return getSuperType(new Class<?>[]{first, second});
    }

    public static Class<?> getComponentType(Class<?> type) {
        return type != null && type.isArray() ? type.getComponentType() : type;
    }

    public static int parseInt(String value) {
        if (value == null || !value.matches("-?\\d+")) {
            throw new IllegalArgumentException("Not an integer: " + value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return value.startsWith("-") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
    }

<<<<<<< HEAD
    public static String replaceChatStyles(String value) {
        return value;
    }

    public static @Nullable String parseHexColor(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.startsWith("#") ? value.substring(1) : value;
        if (normalized.length() != 6) {
            return null;
        }
        for (int i = 0; i < normalized.length(); i++) {
            if (Character.digit(normalized.charAt(i), 16) == -1) {
                return null;
            }
        }
        return "#" + normalized.toLowerCase(Locale.ENGLISH);
    }
}
