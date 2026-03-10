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

    public static double numberAfter(final CharSequence input, final int index) {
        return numberAt(input, index, true);
    }

    public static double numberBefore(final CharSequence input, final int index) {
        return numberAt(input, index, false);
    }

    public static double numberAt(final CharSequence input, final int index, final boolean forward) {
        if (input == null || index < 0 || index >= input.length()) {
            return -1;
        }
        final int direction = forward ? 1 : -1;
        boolean stillWhitespace = true;
        boolean hasDot = false;
        int first = -1;
        int last = -1;
        for (int i = index; i >= 0 && i < input.length(); i += direction) {
            final char current = input.charAt(i);
            if (Character.isDigit(current)) {
                if (first == -1) {
                    first = last = i;
                } else {
                    first += direction;
                }
                stillWhitespace = false;
            } else if (current == '.') {
                if (hasDot) {
                    break;
                }
                if (first == -1) {
                    first = last = i;
                } else {
                    first += direction;
                }
                hasDot = true;
                stillWhitespace = false;
            } else if (Character.isWhitespace(current)) {
                if (stillWhitespace) {
                    continue;
                }
                break;
            } else {
                break;
            }
        }
        if (first == -1) {
            return -1;
        }
        if (input.charAt(Math.min(first, last)) == '.') {
            return -1;
        }
        int boundary = first + direction;
        if (boundary > 0 && boundary < input.length() && !Character.isWhitespace(input.charAt(boundary))) {
            return -1;
        }
        return Double.parseDouble(input.subSequence(Math.min(first, last), Math.max(first, last) + 1).toString());
    }
}
