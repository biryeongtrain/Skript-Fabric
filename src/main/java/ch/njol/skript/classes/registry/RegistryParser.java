package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.PatternedParser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftRegistryLookup;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

/**
 * Compatibility parser for Minecraft registries on the local Fabric runtime.
 *
 * <p>This keeps the upstream registry-backed class-info surface available
 * without depending on Bukkit's {@code Keyed}/{@code Registry} types.
 */
public class RegistryParser<R> extends PatternedParser<R> {

    private final Registry<R> registry;
    private final String languageNode;
    private final Map<R, String> names = new LinkedHashMap<>();
    private final Map<String, R> parseMap = new LinkedHashMap<>();
    private String[] patterns = new String[0];

    public RegistryParser(Registry<R> registry, String languageNode) {
        if (languageNode.isEmpty() || languageNode.endsWith(".")) {
            throw new IllegalArgumentException("Invalid language node: " + languageNode);
        }
        this.registry = registry;
        this.languageNode = languageNode;
        refresh();
        Language.addListener(this::refresh);
    }

    private void refresh() {
        names.clear();
        parseMap.clear();
        for (R registryObject : registry) {
            ResourceLocation key = registry.getKey(registryObject);
            if (key == null) {
                continue;
            }

            String namespace = key.getNamespace();
            String path = key.getPath();
            String pathWithSpaces = path.replace('_', ' ');
            String languageKey;

            parseMap.put(key.toString(), registryObject);

            if ("minecraft".equalsIgnoreCase(namespace)) {
                parseMap.put(pathWithSpaces, registryObject);
                languageKey = languageNode + "." + path;
            } else {
                languageKey = key.toString();
            }

            String[] options = Language.getList(languageKey);
            if (options.length == 1 && options[0].equals(languageKey.toLowerCase(Locale.ENGLISH))) {
                names.put(registryObject, "minecraft".equalsIgnoreCase(namespace) ? pathWithSpaces : key.toString());
            } else {
                for (String option : options) {
                    String normalized = option.toLowerCase(Locale.ENGLISH);
                    NonNullPair<String, Integer> strippedOption = Noun.stripGender(normalized, languageKey);
                    String display = strippedOption.first();
                    int gender = strippedOption.second();

                    names.putIfAbsent(registryObject, display);
                    parseMap.put(display, registryObject);
                    if (gender != -1) {
                        parseMap.put(
                                Noun.getArticleWithSpace(gender, Language.F_INDEFINITE_ARTICLE) + display,
                                registryObject
                        );
                    }
                }
            }
        }

        patterns = parseMap.keySet().stream()
                .filter(pattern -> !pattern.startsWith("minecraft:"))
                .sorted()
                .toArray(String[]::new);
    }

    @Override
    public @Nullable R parse(String input, @NotNull ParseContext context) {
        if (input == null || input.isBlank()) {
            return null;
        }
        R direct = parseMap.get(input.toLowerCase(Locale.ENGLISH));
        if (direct != null) {
            return direct;
        }
        return MinecraftRegistryLookup.lookup(input, id -> {
            R value = registry.getValue(id);
            ResourceLocation actual = value == null ? null : registry.getKey(value);
            return id.equals(actual) ? value : null;
        });
    }

    @Override
    public @NotNull String toString(R object, int flags) {
        String name = names.get(object);
        if (name != null) {
            return name;
        }
        ResourceLocation key = registry.getKey(object);
        return key == null ? String.valueOf(object) : MinecraftResourceParser.display(key);
    }

    @Override
    public @NotNull String toVariableNameString(R object) {
        return toString(object, 0);
    }

    @Override
    public String[] getPatterns() {
        return patterns.clone();
    }
}
