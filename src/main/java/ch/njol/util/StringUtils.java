package ch.njol.util;

import java.util.Iterator;
import java.util.List;

public final class StringUtils {

    private StringUtils() {
    }

    public static String join(List<?> values, String delimiter) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Iterator<?> iterator = values.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            builder.append(next);
            if (iterator.hasNext()) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    public static String join(List<?> values, String delimiter, String lastDelimiter) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        if (values.size() == 1) {
            return String.valueOf(values.getFirst());
        }
        if (values.size() == 2) {
            return values.get(0) + lastDelimiter + values.get(1);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            builder.append(values.get(i));
            if (i < values.size() - 2) {
                builder.append(delimiter);
            } else if (i == values.size() - 2) {
                builder.append(lastDelimiter);
            }
        }
        return builder.toString();
    }

    public static String multiply(String input, int count) {
        if (count <= 0 || input == null || input.isEmpty()) {
            return "";
        }
        return input.repeat(count);
    }
}
