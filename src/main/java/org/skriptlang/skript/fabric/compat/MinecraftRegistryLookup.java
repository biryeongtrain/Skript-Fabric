package org.skriptlang.skript.fabric.compat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class MinecraftRegistryLookup {

    private MinecraftRegistryLookup() {
    }

    public static <T> @Nullable T lookup(@Nullable String raw, Function<Identifier, T> resolver) {
        for (Identifier id : candidateIds(raw)) {
            T value = resolver.apply(id);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static List<Identifier> candidateIds(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        Set<Identifier> ids = new LinkedHashSet<>();
        for (String candidate : textCandidates(raw)) {
            addId(ids, candidate, false, false);
            addId(ids, candidate, false, true);
            addId(ids, candidate, true, false);
            addId(ids, candidate, true, true);
        }
        return List.copyOf(ids);
    }

    public static Set<String> candidateLookupKeys(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        Set<String> keys = new LinkedHashSet<>();
        for (String candidate : textCandidates(raw)) {
            String normalized = normalizeAlias(candidate);
            if (!normalized.isBlank()) {
                keys.add(normalized);
            }
        }
        return Set.copyOf(keys);
    }

    public static String normalizeAlias(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return stripQuotes(raw)
                .trim()
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", "");
    }

    private static Set<String> textCandidates(String raw) {
        Set<String> candidates = new LinkedHashSet<>();
        String normalized = stripQuotes(raw).trim().toLowerCase(Locale.ENGLISH);
        addTextCandidate(candidates, normalized);
        String withoutArticle = stripLeadingArticle(normalized);
        addTextCandidate(candidates, withoutArticle);
        addTextCandidate(candidates, singularizePhrase(withoutArticle));
        return candidates;
    }

    private static void addTextCandidate(Set<String> candidates, @Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        candidates.add(raw.trim().toLowerCase(Locale.ENGLISH));
    }

    private static void addId(Set<Identifier> ids, String raw, boolean collapseSpaces, boolean collapseHyphens) {
        String normalized = normalizeId(raw, collapseSpaces, collapseHyphens);
        if (normalized == null || normalized.isBlank()) {
            return;
        }
        try {
            ids.add(normalized.indexOf(':') >= 0
                    ? Identifier.parse(normalized)
                    : Identifier.withDefaultNamespace(normalized));
        } catch (RuntimeException ignored) {
        }
    }

    private static @Nullable String normalizeId(String raw, boolean collapseSpaces, boolean collapseHyphens) {
        String trimmed = raw.trim().toLowerCase(Locale.ENGLISH);
        if (trimmed.isBlank()) {
            return null;
        }
        int separator = trimmed.indexOf(':');
        String namespace = separator >= 0 ? sanitizeNamespace(trimmed.substring(0, separator)) : "";
        String path = separator >= 0 ? trimmed.substring(separator + 1) : trimmed;
        path = sanitizePath(path, collapseSpaces, collapseHyphens);
        if (path.isBlank()) {
            return null;
        }
        return namespace.isBlank() ? path : namespace + ":" + path;
    }

    private static String sanitizeNamespace(String raw) {
        return raw.replaceAll("[^a-z0-9_.-]", "");
    }

    private static String sanitizePath(String raw, boolean collapseSpaces, boolean collapseHyphens) {
        String normalized = raw
                .replaceAll("[^a-zA-Z0-9_./\\-\\s]", "")
                .trim()
                .toLowerCase(Locale.ENGLISH);
        normalized = collapseSpaces
                ? normalized.replaceAll("\\s+", "")
                : normalized.replaceAll("\\s+", "_");
        if (collapseHyphens) {
            normalized = collapseSpaces
                    ? normalized.replace("-", "")
                    : normalized.replace("-", "_");
        }
        return normalized;
    }

    private static String stripQuotes(String raw) {
        String normalized = raw == null ? "" : raw.trim();
        while (normalized.length() >= 2) {
            if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                    || (normalized.startsWith("'") && normalized.endsWith("'"))) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
                continue;
            }
            break;
        }
        return normalized;
    }

    private static String stripLeadingArticle(String raw) {
        if (raw.startsWith("a ")) {
            return raw.substring(2).trim();
        }
        if (raw.startsWith("an ")) {
            return raw.substring(3).trim();
        }
        if (raw.startsWith("the ")) {
            return raw.substring(4).trim();
        }
        return raw;
    }

    private static String singularizePhrase(String raw) {
        int separator = raw.indexOf(':');
        String namespace = separator >= 0 ? raw.substring(0, separator + 1) : "";
        String path = separator >= 0 ? raw.substring(separator + 1) : raw;
        int lastBreak = Math.max(path.lastIndexOf(' '), Math.max(path.lastIndexOf('_'), path.lastIndexOf('-')));
        if (lastBreak < 0) {
            return namespace + singularizeWord(path);
        }
        return namespace + path.substring(0, lastBreak + 1) + singularizeWord(path.substring(lastBreak + 1));
    }

    private static String singularizeWord(String raw) {
        if (raw.endsWith("ies") && raw.length() > 3) {
            return raw.substring(0, raw.length() - 3) + "y";
        }
        if ((raw.endsWith("ches") || raw.endsWith("shes") || raw.endsWith("sses")
                || raw.endsWith("xes") || raw.endsWith("zes")) && raw.length() > 2) {
            return raw.substring(0, raw.length() - 2);
        }
        if (raw.endsWith("s") && !raw.endsWith("ss") && raw.length() > 1) {
            return raw.substring(0, raw.length() - 1);
        }
        return raw;
    }
}
