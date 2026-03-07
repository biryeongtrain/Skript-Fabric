package org.skriptlang.skript.fabric.runtime;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public enum FabricPotionEffectAction {
    ADDED("added"),
    CHANGED("changed"),
    REMOVED("removed"),
    CLEARED("cleared");

    private final String skriptName;

    FabricPotionEffectAction(String skriptName) {
        this.skriptName = skriptName;
    }

    public String skriptName() {
        return skriptName;
    }

    public static @Nullable FabricPotionEffectAction parse(@Nullable Object value) {
        if (value instanceof FabricPotionEffectAction action) {
            return action;
        }
        if (value == null) {
            return null;
        }
        String normalized = normalize(String.valueOf(value));
        return switch (normalized) {
            case "add", "added", "addition" -> ADDED;
            case "change", "changed", "modify", "modified", "modification" -> CHANGED;
            case "remove", "removed", "removal" -> REMOVED;
            case "clear", "cleared", "clearing" -> CLEARED;
            default -> null;
        };
    }

    private static String normalize(String raw) {
        return raw.trim()
                .toLowerCase(Locale.ENGLISH)
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ");
    }
}
