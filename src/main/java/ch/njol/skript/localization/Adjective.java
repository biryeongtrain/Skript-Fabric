package ch.njol.skript.localization;

import java.util.HashMap;
import org.jetbrains.annotations.Nullable;

public class Adjective extends Message {

    private static final int DEFINITE_ARTICLE = -100;
    private static final String DEFINITE_ARTICLE_TOKEN = "+";

    private final HashMap<Integer, String> genders = new HashMap<>();
    @Nullable
    String def;

    public Adjective(String key) {
        super(key);
    }

    @Override
    protected void onValueChange() {
        genders.clear();
        String value = getValue();
        def = value;
        if (value == null) {
            return;
        }
        int start = value.indexOf('@');
        int end = value.lastIndexOf('@');
        if (start == -1 || start == end) {
            return;
        }
        def = value.substring(0, start) + value.substring(end + 1);
        int cursor = start;
        do {
            int next = value.indexOf('@', cursor + 1);
            int colon = value.indexOf(':', cursor + 1);
            if (colon == -1 || colon > next) {
                return;
            }
            String gender = value.substring(cursor + 1, colon);
            int genderId = gender.equals(DEFINITE_ARTICLE_TOKEN)
                    ? DEFINITE_ARTICLE
                    : Noun.getGender(gender, key);
            genders.putIfAbsent(genderId, value.substring(0, start) + value.substring(colon + 1, next) + value.substring(end + 1));
            cursor = next;
        } while (cursor < end);
    }

    @Override
    public String toString() {
        validate();
        return String.valueOf(def);
    }

    public String toString(int gender, int flags) {
        validate();
        if ((flags & Language.F_DEFINITE_ARTICLE) != 0 && genders.containsKey(DEFINITE_ARTICLE)) {
            gender = DEFINITE_ARTICLE;
        } else if ((flags & Language.F_PLURAL) != 0) {
            gender = Noun.PLURAL;
        }
        return genders.getOrDefault(gender, String.valueOf(def));
    }

    public static String toString(Adjective[] adjectives, int gender, int flags, boolean and) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < adjectives.length; i++) {
            if (i != 0) {
                builder.append(i == adjectives.length - 1 ? " " + (and ? GeneralWords.and : GeneralWords.or) + " " : ", ");
            }
            builder.append(adjectives[i].toString(gender, flags));
        }
        return builder.toString();
    }

    public String toString(Noun noun, int flags) {
        return noun.toString(this, flags);
    }
}
