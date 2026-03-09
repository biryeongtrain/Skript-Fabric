package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link ch.njol.skript.classes.EnumParser} instead.
 */
@Deprecated(since = "2.12", forRemoval = true)
public final class EnumUtils<E extends Enum<E>> {

    private final Class<E> enumClass;
    private final String languageNode;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private String[] names;
    private final HashMap<String, E> parseMap = new HashMap<>();

    public EnumUtils(Class<E> enumClass, String languageNode) {
        assert enumClass.isEnum() : enumClass;
        assert !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;

        this.enumClass = enumClass;
        this.languageNode = languageNode;

        refresh();
        Language.addListener(this::refresh);
    }

    void refresh() {
        E[] constants = enumClass.getEnumConstants();
        names = new String[constants.length];
        parseMap.clear();
        for (E constant : constants) {
            String key = languageNode + "." + constant.name();
            int ordinal = constant.ordinal();

            String[] options = Language.getList(key);
            for (String option : options) {
                option = option.toLowerCase(Locale.ENGLISH);
                if (options.length == 1 && option.equals(key.toLowerCase(Locale.ENGLISH))) {
                    String[] splitKey = key.split("\\.");
                    String newKey = splitKey[1].replace('_', ' ').toLowerCase(Locale.ENGLISH) + " " + splitKey[0];
                    parseMap.put(newKey, constant);
                    Skript.debug("Missing lang enum constant for '" + key + "'. Using '" + newKey + "' for now.");
                    continue;
                }

                NonNullPair<String, Integer> strippedOption = Noun.stripGender(option, key);
                String first = strippedOption.first();
                Integer second = strippedOption.second();

                if (names[ordinal] == null) {
                    names[ordinal] = first;
                }

                parseMap.put(first, constant);
                if (second != -1) {
                    parseMap.put(Noun.getArticleWithSpace(second, Language.F_INDEFINITE_ARTICLE) + first, constant);
                }
            }
        }
    }

    @Nullable
    public E parse(String input) {
        return parseMap.get(input.toLowerCase(Locale.ENGLISH));
    }

    public String toString(E enumerator, int flags) {
        String name = names[enumerator.ordinal()];
        return name != null ? name : enumerator.name();
    }

    public String toString(E enumerator, StringMode flag) {
        return toString(enumerator, flag.ordinal());
    }

    public String getAllNames() {
        return StringUtils.join(new ArrayList<>(parseMap.keySet()), ", ");
    }
}
