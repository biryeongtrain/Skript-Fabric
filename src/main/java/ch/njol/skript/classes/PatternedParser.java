package ch.njol.skript.classes;

import ch.njol.util.StringUtils;
import java.util.Arrays;

/**
 * Legacy parser base that exposes its literal patterns.
 *
 * @param <T> parsed type
 */
public abstract class PatternedParser<T> extends Parser<T> {

    public abstract String[] getPatterns();

    public String getCombinedPatterns() {
        return StringUtils.join(Arrays.asList(getPatterns()), ", ");
    }
}
