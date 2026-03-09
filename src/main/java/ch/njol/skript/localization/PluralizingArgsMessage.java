package ch.njol.skript.localization;

public class PluralizingArgsMessage extends Message {

    public PluralizingArgsMessage(String key) {
        super(key);
    }

    public String toString(Object... args) {
        String value = getValue();
        if (value == null) {
            return key;
        }
        return format(String.format(value, args));
    }

    public static String format(String input) {
        StringBuilder builder = new StringBuilder();
        int last = 0;
        boolean plural = false;
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (Character.isDigit(current)) {
                plural = Math.abs(numberAfter(input, i)) != 1;
            } else if (current == '¦') {
                int first = input.indexOf('¦', i + 1);
                int second = first == -1 ? -1 : input.indexOf('¦', first + 1);
                if (first == -1 || second == -1) {
                    break;
                }
                builder.append(input, last, i);
                builder.append(plural ? input.substring(first + 1, second) : input.substring(i + 1, first));
                i = second;
                last = second + 1;
                plural = false;
            }
        }
        if (last == 0) {
            return input;
        }
        builder.append(input.substring(last));
        return builder.toString();
    }

    private static double numberAfter(String input, int index) {
        int end = index + 1;
        while (end < input.length() && (Character.isDigit(input.charAt(end)) || input.charAt(end) == '.')) {
            end++;
        }
        return Double.parseDouble(input.substring(index, end));
    }
}
