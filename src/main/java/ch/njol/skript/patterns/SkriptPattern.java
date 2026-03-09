package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import org.jetbrains.annotations.Nullable;

public final class SkriptPattern {

    private final String source;
    private final PatternCompiler.CompiledPattern compiled;
    private final @Nullable PatternElement first;
    private final int expressionAmount;
    private final Keyword[] keywords;

    SkriptPattern(String source, PatternCompiler.CompiledPattern compiled) {
        this.source = source == null ? "" : source;
        this.compiled = compiled;
        this.first = compiled.first();
        this.expressionAmount = compiled.expressionAmount();
        this.keywords = first == null ? new Keyword[0] : Keyword.buildKeywords(first);
    }

    public @Nullable MatchResult match(String text, int flags, ParseContext parseContext) {
        String rawText = text == null ? "" : text;
        String lowerExpr = rawText.toLowerCase(Locale.ENGLISH);
        for (Keyword keyword : keywords) {
            if (!keyword.isPresent(lowerExpr)) {
                return null;
            }
        }
        String trimmedText = rawText.trim();
        Matcher matcher = compiled.regex().matcher(trimmedText);
        if (!matcher.matches()) {
            return null;
        }

        List<Expression<?>> expressions = new ArrayList<>();
        List<Matcher> regexes = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        int mark = 0;

        List<PatternCompiler.CaptureSpec> captures = compiled.captures();
        for (int i = 0; i < captures.size(); i++) {
            String captured = matcher.group(i + 1);
            PatternCompiler.CaptureSpec capture = captures.get(i);
            switch (capture.kind()) {
                case EXPRESSION -> {
                    TypePatternElement typePattern = capture.typePattern();
                    if (typePattern == null) {
                        return null;
                    }
                    if (captured == null) {
                        expressions.add(null);
                        continue;
                    }
                    Expression<?> parsed = parseCapturedExpression(captured, flags, parseContext, typePattern);
                    if (parsed == null) {
                        return null;
                    }
                    expressions.add(parsed);
                }
                case REGEX -> {
                    if (captured == null) {
                        continue;
                    }
                    if (capture.regexPattern() == null) {
                        return null;
                    }
                    Matcher rawMatcher = capture.regexPattern().matcher(captured.trim());
                    if (!rawMatcher.matches()) {
                        return null;
                    }
                    regexes.add(rawMatcher);
                }
                case METADATA -> {
                    if (captured == null) {
                        continue;
                    }
                    if (capture.tag() != null && !capture.tag().isBlank()) {
                        tags.add(capture.tag());
                    }
                    mark ^= capture.mark();
                }
            }
        }

        return new MatchResult(expressions.toArray(Expression<?>[]::new), regexes, tags, mark);
    }

    public @Nullable MatchResult match(String text) {
        return match(text, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
    }

    public int countTypes() {
        return expressionAmount;
    }

    public int countNonNullTypes() {
        return countNonNullTypes(first);
    }

    public Set<Integer> getActiveExpressionIndices(Expression<?>[] expressions) {
        if (first == null) {
            return Set.of();
        }

        Set<Integer> present = new LinkedHashSet<>();
        for (int i = 0; i < expressions.length; i++) {
            if (expressions[i] != null) {
                present.add(i);
            }
        }

        List<Set<Integer>> candidates = collectActiveExpressionIndexSets(first);
        if (present.isEmpty()) {
            int bestSize = Integer.MAX_VALUE;
            LinkedHashSet<Integer> best = new LinkedHashSet<>();
            for (Set<Integer> candidate : candidates) {
                if (candidate.size() < bestSize) {
                    bestSize = candidate.size();
                    best.clear();
                    best.addAll(candidate);
                    continue;
                }
                if (candidate.size() == bestSize) {
                    best.addAll(candidate);
                }
            }
            return best;
        }
        int bestSize = Integer.MAX_VALUE;
        LinkedHashSet<Integer> best = new LinkedHashSet<>();
        for (Set<Integer> candidate : candidates) {
            if (!candidate.containsAll(present)) {
                continue;
            }
            if (candidate.size() < bestSize) {
                bestSize = candidate.size();
                best.clear();
                best.addAll(candidate);
                continue;
            }
            if (candidate.size() == bestSize) {
                best.addAll(candidate);
            }
        }
        return best.isEmpty() ? present : best;
    }

    public <T extends PatternElement> List<T> getElements(Class<T> type) {
        if (first == null) {
            return List.of();
        }
        List<T> elements = new ArrayList<>();
        collectElements(type, first, elements);
        return Collections.unmodifiableList(elements);
    }

    @Override
    public String toString() {
        return first == null ? source : first.toFullString();
    }

    private static int countNonNullTypes(@Nullable PatternElement element) {
        int count = 0;
        while (element != null) {
            if (element instanceof ChoicePatternElement choice) {
                int max = 0;
                for (PatternElement branch : choice.getPatternElements()) {
                    max = Math.max(max, countNonNullTypes(branch));
                }
                count += max;
            } else if (element instanceof GroupPatternElement group) {
                count += countNonNullTypes(group.getPatternElement());
            } else if (element instanceof OptionalPatternElement optional) {
                count += countNonNullTypes(optional.getPatternElement());
            } else if (element instanceof TypePatternElement) {
                count++;
            }
            element = element.getOriginalNext();
        }
        return count;
    }

    private static List<Set<Integer>> collectActiveExpressionIndexSets(@Nullable PatternElement element) {
        List<Set<Integer>> activeIndexSets = new ArrayList<>();
        activeIndexSets.add(new LinkedHashSet<>());

        while (element != null) {
            List<Set<Integer>> elementSets = switch (element) {
                case TypePatternElement type -> List.of(new LinkedHashSet<>(Set.of(type.expressionIndex())));
                case ChoicePatternElement choice -> {
                    List<Set<Integer>> branchSets = new ArrayList<>();
                    for (PatternElement branch : choice.getPatternElements()) {
                        branchSets.addAll(collectActiveExpressionIndexSets(branch));
                    }
                    yield branchSets.isEmpty() ? List.of(new LinkedHashSet<>()) : branchSets;
                }
                case GroupPatternElement group -> collectActiveExpressionIndexSets(group.getPatternElement());
                case OptionalPatternElement optional -> collectActiveExpressionIndexSets(optional.getPatternElement());
                default -> List.of(new LinkedHashSet<>());
            };

            List<Set<Integer>> combined = new ArrayList<>();
            for (Set<Integer> prefix : activeIndexSets) {
                for (Set<Integer> suffix : elementSets) {
                    LinkedHashSet<Integer> merged = new LinkedHashSet<>(prefix);
                    merged.addAll(suffix);
                    combined.add(merged);
                }
            }
            activeIndexSets = combined;
            element = element.getOriginalNext();
        }

        return activeIndexSets;
    }

    private static <T extends PatternElement> void collectElements(
            Class<T> type,
            @Nullable PatternElement element,
            List<T> elements
    ) {
        while (element != null) {
            if (type.isInstance(element)) {
                elements.add(type.cast(element));
            }
            if (element instanceof ChoicePatternElement choice) {
                for (PatternElement branch : choice.getPatternElements()) {
                    collectElements(type, branch, elements);
                }
            } else if (element instanceof GroupPatternElement group) {
                collectElements(type, group.getPatternElement(), elements);
            } else if (element instanceof OptionalPatternElement optional) {
                collectElements(type, optional.getPatternElement(), elements);
            }
            element = element.getOriginalNext();
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Expression<?> parseCapturedExpression(
            String captured,
            int flags,
            ParseContext parseContext,
            TypePatternElement typePattern
    ) {
        Expression<?> parsed = new SkriptParser(
                captured.trim(),
                flags & typePattern.flagMask(),
                parseContext
        ).parseExpression((Class<? extends Object>[]) typePattern.returnTypes());
        if (parsed == null) {
            return null;
        }
        if (typePattern.time() != 0 && !parsed.setTime(typePattern.time())) {
            return null;
        }
        return parsed;
    }
}
