package ch.njol.skript.classes;

import ch.njol.skript.lang.util.common.AnyProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Compatibility helper for upstream Any* class infos.
 *
 * @see AnyProvider
 * @deprecated Use {@link org.skriptlang.skript.lang.properties.Property} instead.
 */
@Deprecated(since = "2.13", forRemoval = true)
public class AnyInfo<Type extends AnyProvider> extends ClassInfo<Type> {

    public AnyInfo(Class<Type> type, String codeName) {
        super(type, codeName);
        this.user("(any )?" + codeName + " (thing|object)s?");
    }

    @Override
    public ClassInfo<Type> user(String... userInputPatterns) throws PatternSyntaxException {
        if (this.userInputPatterns == null) {
            return super.user(userInputPatterns);
        }
        List<Pattern> patterns = new ArrayList<>(List.of(this.userInputPatterns));
        for (String pattern : userInputPatterns) {
            patterns.add(Pattern.compile(pattern));
        }
        this.userInputPatterns = patterns.toArray(Pattern[]::new);
        return this;
    }
}
