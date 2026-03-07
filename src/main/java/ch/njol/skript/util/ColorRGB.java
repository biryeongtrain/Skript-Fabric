package ch.njol.skript.util;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public record ColorRGB(int red, int green, int blue) implements Color {

    public ColorRGB {
        red = clamp(red);
        green = clamp(green);
        blue = clamp(blue);
    }

    public static ColorRGB fromRgb(int rgb) {
        return new ColorRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    public static @Nullable ColorRGB parse(@Nullable Object value) {
        if (value instanceof ColorRGB color) {
            return color;
        }
        if (value instanceof Number number) {
            return fromRgb(number.intValue());
        }
        if (!(value instanceof String string)) {
            return null;
        }
        String normalized = string.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.startsWith("#")) {
            try {
                return fromRgb(Integer.parseInt(normalized.substring(1), 16));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        String[] split = normalized.split(",");
        if (split.length == 3) {
            try {
                return new ColorRGB(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return switch (normalized) {
            case "black" -> new ColorRGB(0, 0, 0);
            case "white" -> new ColorRGB(255, 255, 255);
            case "red" -> new ColorRGB(255, 0, 0);
            case "green" -> new ColorRGB(0, 255, 0);
            case "blue" -> new ColorRGB(0, 0, 255);
            case "yellow" -> new ColorRGB(255, 255, 0);
            case "cyan" -> new ColorRGB(0, 255, 255);
            case "magenta" -> new ColorRGB(255, 0, 255);
            case "orange" -> new ColorRGB(255, 165, 0);
            case "gray", "grey" -> new ColorRGB(128, 128, 128);
            default -> null;
        };
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    @Override
    public String toString() {
        return String.format("#%02x%02x%02x", red, green, blue);
    }
}
