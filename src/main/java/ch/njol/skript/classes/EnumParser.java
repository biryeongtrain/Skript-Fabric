package ch.njol.skript.classes;

import ch.njol.skript.lang.ParseContext;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

/**
 * Compatibility parser for enum-backed class infos.
 *
 * <p>The upstream parser is localization-backed. The local port currently keeps
 * the fallback name mapping based on enum constant names so upstream enum
 * class-info imports can land before the localization layer is closed.
 */
public class EnumParser<E extends Enum<E>> extends PatternedParser<E> implements Converter<String, E> {

    private final Class<E> enumClass;
    private final String languageNode;
    private final Map<String, E> parseMap = new LinkedHashMap<>();
    private String[] names = new String[0];
    private String[] patterns = new String[0];

    public EnumParser(Class<E> enumClass, String languageNode) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException(enumClass.getName() + " is not an enum");
        }
        if (languageNode.isEmpty() || languageNode.endsWith(".")) {
            throw new IllegalArgumentException("Invalid language node: " + languageNode);
        }
        this.enumClass = enumClass;
        this.languageNode = languageNode;
        refresh();
    }

    void refresh() {
        E[] constants = enumClass.getEnumConstants();
        names = new String[constants.length];
        parseMap.clear();
        for (E constant : constants) {
            String normalized = constant.name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
            names[constant.ordinal()] = normalized;
            parseMap.put(normalized, constant);

            String singular = normalized.endsWith("s") && normalized.length() > 1
                    ? normalized.substring(0, normalized.length() - 1)
                    : normalized;
            parseMap.putIfAbsent(singular, constant);

            String fallback = constant.name().toLowerCase(Locale.ENGLISH);
            parseMap.putIfAbsent(fallback, constant);
            parseMap.putIfAbsent(languageNode + " " + singular, constant);
        }
        patterns = parseMap.keySet().toArray(String[]::new);
    }

    @Override
    public @Nullable E parse(String input, ParseContext context) {
        return parseMap.get(input.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public @Nullable E convert(String input) {
        return parse(input, ParseContext.DEFAULT);
    }

    @Override
    public String toVariableNameString(E object) {
        return object.name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String[] getPatterns() {
        return patterns.clone();
    }

    @Override
    public String toString(E object, int flags) {
        return names[object.ordinal()];
    }
}
