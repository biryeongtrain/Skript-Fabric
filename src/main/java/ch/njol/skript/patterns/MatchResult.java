package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import java.util.List;
import java.util.regex.Matcher;

public record MatchResult(
        Expression<?>[] expressions,
        List<Matcher> regexes,
        List<String> tags,
        int mark
) {
}
