package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import org.jetbrains.annotations.Nullable;

public final class SkriptPattern {

    private final String source;
    private final PatternCompiler.CompiledPattern compiled;

    SkriptPattern(String source, PatternCompiler.CompiledPattern compiled) {
        this.source = source == null ? "" : source;
        this.compiled = compiled;
    }

    public @Nullable MatchResult match(String text, int flags, ParseContext parseContext) {
        Matcher matcher = compiled.regex().matcher(PatternCompiler.normalizeInput(text));
        if (!matcher.matches()) {
            return null;
        }

        List<Expression<?>> expressions = new ArrayList<>();
        List<Matcher> regexes = new ArrayList<>();
        Set<String> tags = new LinkedHashSet<>();
        int mark = 0;

        List<PatternCompiler.CaptureSpec> captures = compiled.captures();
        for (int i = 0; i < captures.size(); i++) {
            String captured = matcher.group(i + 1);
            PatternCompiler.CaptureSpec capture = captures.get(i);
            switch (capture.kind()) {
                case EXPRESSION -> {
                    if (captured == null) {
                        expressions.add(null);
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Expression<?> parsed = new SkriptParser(captured.trim(), flags, parseContext)
                            .parseExpression((Class<? extends Object>[]) capture.returnTypes());
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

    @Override
    public String toString() {
        return source;
    }
}
