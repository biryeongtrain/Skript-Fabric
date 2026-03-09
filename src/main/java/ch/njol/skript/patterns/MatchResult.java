package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import org.jetbrains.annotations.Nullable;

public record MatchResult(
        Expression<?>[] expressions,
        List<Matcher> regexes,
        List<String> tags,
        @Nullable Set<Integer> activeExpressionIndices,
        int mark
) {
}
