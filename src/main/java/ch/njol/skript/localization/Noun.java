package ch.njol.skript.localization;

import ch.njol.util.NonNullPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class Noun extends Message {

    public static final String GENDERS_SECTION = "genders.";
    public static final int PLURAL = -2;
    public static final int NO_GENDER = -3;
    public static final String PLURAL_TOKEN = "x";
    public static final String NO_GENDER_TOKEN = "-";

    @Nullable
    private String singular;
    @Nullable
    private String plural;
    private int gender;

    static final HashMap<String, Integer> genders = new HashMap<>();
    static final List<String> indefiniteArticles = new ArrayList<>(3);
    static final List<String> definiteArticles = new ArrayList<>(3);
    static String definitePluralArticle = "";

    static {
        Language.addListener(() -> {
            genders.clear();
            indefiniteArticles.clear();
            definiteArticles.clear();
            for (int i = 0; i < 100; i++) {
                if (!Language.keyExistsDefault(GENDERS_SECTION + i + ".id")) {
                    break;
                }
                String genderId = Language.get(GENDERS_SECTION + i + ".id");
                if (genderId.equalsIgnoreCase(PLURAL_TOKEN) || genderId.equalsIgnoreCase(NO_GENDER_TOKEN)) {
                    continue;
                }
                genders.put(genderId, i);
                String indefinite = Language.get_(GENDERS_SECTION + i + ".indefinite article");
                indefiniteArticles.add(indefinite == null ? "" : indefinite);
                String definite = Language.get_(GENDERS_SECTION + i + ".definite article");
                definiteArticles.add(definite == null ? "" : definite);
            }
            if (genders.isEmpty()) {
                indefiniteArticles.add("");
                definiteArticles.add("");
            }
            String pluralArticle = Language.get_(GENDERS_SECTION + "plural.definite article");
            definitePluralArticle = pluralArticle == null ? "" : pluralArticle;
        }, Language.LanguageListenerPriority.EARLIEST);
    }

    public Noun(String key) {
        super(key);
    }

    @Override
    protected void onValueChange() {
        String value = getValue();
        if (value == null) {
            singular = key;
            plural = key;
            gender = 0;
            return;
        }
        int marker = value.lastIndexOf('@');
        if (marker != -1) {
            gender = getGender(value.substring(marker + 1).trim(), key);
            value = value.substring(0, marker).trim();
        } else {
            gender = 0;
        }
        PluralPair pair = parsePlural(value);
        singular = pair.singular();
        plural = pair.plural();
    }

    @Override
    public String toString() {
        validate();
        return String.valueOf(singular);
    }

    public String toString(boolean plural) {
        validate();
        return plural ? String.valueOf(this.plural) : String.valueOf(singular);
    }

    public String getIndefiniteArticle() {
        validate();
        if (gender == PLURAL || gender == NO_GENDER) {
            return "";
        }
        return gender >= 0 && gender < indefiniteArticles.size() ? indefiniteArticles.get(gender) : "";
    }

    public String getDefiniteArticle() {
        validate();
        if (gender == PLURAL) {
            return definitePluralArticle;
        }
        if (gender == NO_GENDER) {
            return "";
        }
        return gender >= 0 && gender < definiteArticles.size() ? definiteArticles.get(gender) : "";
    }

    public int getGender() {
        validate();
        return gender;
    }

    public static String getArticleWithSpace(int gender, int flags) {
        if (gender == PLURAL) {
            if ((flags & Language.F_DEFINITE_ARTICLE) != 0) {
                return definitePluralArticle + " ";
            }
            return "";
        }
        if (gender == NO_GENDER) {
            return "";
        }
        if ((flags & Language.F_DEFINITE_ARTICLE) != 0) {
            return gender >= 0 && gender < definiteArticles.size()
                    ? definiteArticles.get(gender) + " " : "";
        }
        if ((flags & Language.F_INDEFINITE_ARTICLE) != 0) {
            return gender >= 0 && gender < indefiniteArticles.size()
                    ? indefiniteArticles.get(gender) + " " : "";
        }
        return "";
    }

    public String getArticleWithSpace(int flags) {
        return getArticleWithSpace(getGender(), flags);
    }

    public String toString(int flags) {
        validate();
        return getArticleWithSpace(flags)
                + (((flags & Language.F_PLURAL) != 0) ? plural : singular);
    }

    public String withAmount(double amount) {
        validate();
        return formatAmount(amount) + " " + (amount == 1 ? singular : plural);
    }

    public String withIndefiniteArticle() {
        return toString(Language.F_INDEFINITE_ARTICLE);
    }

    public String withDefiniteArticle() {
        return toString(Language.F_DEFINITE_ARTICLE);
    }

    public String withDefiniteArticle(boolean plural) {
        return toString(Language.F_DEFINITE_ARTICLE | (plural ? Language.F_PLURAL : 0));
    }

    public String toString(Adjective adjective, int flags) {
        validate();
        return getArticleWithSpace(flags)
                + adjective.toString(gender, flags)
                + " "
                + (((flags & Language.F_PLURAL) != 0) ? plural : singular);
    }

    public String toString(Adjective[] adjectives, int flags, boolean and) {
        validate();
        if (adjectives.length == 0) {
            return toString(flags);
        }
        return getArticleWithSpace(flags)
                + Adjective.toString(adjectives, getGender(), flags, and)
                + " "
                + toString(flags);
    }

    public String getSingular() {
        validate();
        return String.valueOf(singular);
    }

    public String getPlural() {
        validate();
        return String.valueOf(plural);
    }

    @Deprecated(since = "2.14", forRemoval = true)
    public static NonNullPair<String, String> getPlural(String input) {
        PluralPair pair = parsePlural(input);
        return new NonNullPair<>(pair.singular(), pair.plural());
    }

    public static PluralPair parsePlural(String input) {
        StringBuilder singular = new StringBuilder();
        StringBuilder plural = new StringBuilder();
        int part = 3;
        int markerCount = count(input, '¦');
        int last = 0;
        int index = -1;
        while ((index = input.indexOf('¦', index + 1)) != -1) {
            String chunk = input.substring(last, index);
            if ((part & 1) != 0) {
                singular.append(chunk);
            }
            if ((part & 2) != 0) {
                plural.append(chunk);
            }
            part = markerCount >= 2 ? (part % 3) + 1 : (part == 2 ? 3 : 2);
            last = index + 1;
            markerCount--;
        }
        String tail = input.substring(last);
        if ((part & 1) != 0) {
            singular.append(tail);
        }
        if ((part & 2) != 0) {
            plural.append(tail);
        }
        return new PluralPair(singular.toString(), plural.toString());
    }

    public record PluralPair(String singular, String plural) {
    }

    public static String normalizePluralMarkers(String value) {
        int count = count(value, '¦');
        if (count % 3 == 0) {
            return value;
        }
        if (count % 3 == 2) {
            int genderIndex = value.lastIndexOf('@');
            return genderIndex == -1 ? value + "¦" : value.substring(0, genderIndex) + "¦" + value.substring(genderIndex);
        }
        int lastMarker = value.lastIndexOf('¦');
        int genderIndex = value.lastIndexOf('@');
        if (genderIndex == -1) {
            return value.substring(0, lastMarker) + "¦" + value.substring(lastMarker) + "¦";
        }
        return value.substring(0, lastMarker) + "¦" + value.substring(lastMarker, genderIndex) + "¦" + value.substring(genderIndex);
    }

    public static int getGender(String gender, String key) {
        if (gender.equalsIgnoreCase(PLURAL_TOKEN)) {
            return PLURAL;
        }
        if (gender.equalsIgnoreCase(NO_GENDER_TOKEN)) {
            return NO_GENDER;
        }
        Integer value = genders.get(gender);
        return value == null ? 0 : value;
    }

    @Nullable
    public static String getGenderID(int gender) {
        if (gender == PLURAL) {
            return PLURAL_TOKEN;
        }
        if (gender == NO_GENDER) {
            return NO_GENDER_TOKEN;
        }
        return Language.get_("genders." + gender + ".id");
    }

    public static NonNullPair<String, Integer> stripGender(String value, String key) {
        int index = value.lastIndexOf('@');
        int gender = -1;
        if (index != -1) {
            gender = getGender(value.substring(index + 1).trim(), key);
            value = value.substring(0, index).trim();
        }
        return new NonNullPair<>(value, gender);
    }

    public static String stripIndefiniteArticle(String value) {
        for (String article : indefiniteArticles) {
            if (startsWithIgnoreCase(value, article + " ")) {
                return value.substring(article.length() + 1);
            }
        }
        return value;
    }

    public static String stripDefiniteArticle(String value) {
        for (String article : definiteArticles) {
            if (startsWithIgnoreCase(value, article + " ")) {
                return value.substring(article.length() + 1);
            }
        }
        return value;
    }

    public static boolean isIndefiniteArticle(String value) {
        return indefiniteArticles.contains(value.toLowerCase());
    }

    public static boolean isDefiniteArticle(String value) {
        return definiteArticles.contains(value.toLowerCase()) || definitePluralArticle.equalsIgnoreCase(value);
    }

    public static String toString(String singular, String plural, int gender, int flags) {
        return getArticleWithSpace(gender, flags) + (((flags & Language.F_PLURAL) != 0) ? plural : singular);
    }

    private static boolean startsWithIgnoreCase(String input, String prefix) {
        return input.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static int count(String input, char needle) {
        int matches = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == needle) {
                matches++;
            }
        }
        return matches;
    }

    private static String formatAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return Long.toString((long) amount);
        }
        return Double.toString(amount);
    }
}
