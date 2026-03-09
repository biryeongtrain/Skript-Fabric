package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * A helper class useful when an expression/condition/effect/etc. needs to associate additional data with each pattern.
 */
public class Patterns<T> {

    private final String[] patterns;
    private final Object[] types;
    private final Map<Object, List<Integer>> matchedPatterns = new HashMap<>();

    public Patterns(Object[][] info) {
        patterns = new String[info.length];
        types = new Object[info.length];
        for (int i = 0; i < info.length; i++) {
            if (info[i].length != 2 || !(info[i][0] instanceof String)) {
                throw new IllegalArgumentException("given array is not like {{String, T}, {String, T}, ...}");
            }
            patterns[i] = (String) info[i][0];
            types[i] = info[i][1];
            matchedPatterns.computeIfAbsent(info[i][1], list -> new ArrayList<>()).add(i);
        }
    }

    public String[] getPatterns() {
        return patterns;
    }

    @SuppressWarnings("unchecked")
    public T getInfo(int matchedPattern) {
        Object object = types[matchedPattern];
        if (object == null) {
            return null;
        }
        return (T) object;
    }

    public Integer @Nullable [] getMatchedPatterns(@Nullable T type) {
        if (matchedPatterns.containsKey(type)) {
            return matchedPatterns.get(type).toArray(Integer[]::new);
        }
        return null;
    }

    public Optional<Integer> getMatchedPattern(@Nullable T type, int arrayIndex) {
        Integer[] patternIndices = getMatchedPatterns(type);
        if (patternIndices == null || patternIndices.length < arrayIndex + 1) {
            return Optional.empty();
        }
        return Optional.of(patternIndices[arrayIndex]);
    }
}
