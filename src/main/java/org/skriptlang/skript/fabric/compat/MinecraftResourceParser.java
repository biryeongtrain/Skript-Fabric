package org.skriptlang.skript.fabric.compat;

import net.minecraft.resources.ResourceLocation;

public final class MinecraftResourceParser {

    private MinecraftResourceParser() {
    }

    public static ResourceLocation parse(String rawId) {
        String normalized = normalize(rawId);
        try {
            return normalized.indexOf(':') >= 0
                    ? ResourceLocation.parse(normalized)
                    : ResourceLocation.withDefaultNamespace(normalized);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "Unable to parse resource id. raw=" + printable(rawId) + ", normalized=" + printable(normalized),
                    ex
            );
        }
    }

    public static String display(ResourceLocation id) {
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    public static String normalize(String rawId) {
        String normalized = rawId == null ? "" : rawId.trim();
        while (normalized.length() >= 2) {
            if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                    || (normalized.startsWith("'") && normalized.endsWith("'"))) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
                continue;
            }
            if ((normalized.startsWith("\\\"") && normalized.endsWith("\\\""))
                    || (normalized.startsWith("\\'") && normalized.endsWith("\\'"))) {
                normalized = normalized.substring(2, normalized.length() - 2).trim();
                continue;
            }
            break;
        }
        return normalized
                .replace("\\\"", "")
                .replace("\\'", "")
                .replaceAll("\\s+", "")
                .replaceAll("[^A-Za-z0-9_:/.-]", "");
    }

    private static String printable(String value) {
        return value == null ? "null" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
