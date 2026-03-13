package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ClassInfo.Parser;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.util.ClassInfoReference;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Parse")
@Description({
        "Parses text as a given type, or as a given pattern.",
        "If parsing fails, this expression returns nothing and the parse error expression may be set."
})
@Example("set {var} to line 1 parsed as number")
@Example("set {parsed::*} to message parsed as \"buying %items% for %money%\"")
@Since("2.0, Fabric")
public class ExprParse extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprParse.class, Object.class,
                "%string% parsed as (%-*classinfo%|\"<.*>\")");
    }

    static @Nullable String lastError;

    private Expression<String> text;
    private @Nullable SkriptPattern pattern;
    private @Nullable NonNullPair<ClassInfo<?>, Boolean>[] patternExpressions;
    private boolean single = true;
    public boolean flatten = true;
    private @Nullable ClassInfo<?> classInfo;

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        text = (Expression<String>) exprs[0];
        if (exprs[1] == null) {
            String rawPattern = parseResult.regexes.get(0).group();
            String validatedPattern = unquote(rawPattern);
            NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> validated = SkriptParser.validatePattern(validatedPattern);
            if (validated == null) {
                return false;
            }

            String normalizedPattern = validated.first();
            patternExpressions = validated.second();
            for (NonNullPair<ClassInfo<?>, Boolean> patternExpression : patternExpressions) {
                if (!canParse(patternExpression.first())) {
                    return false;
                }
                if (patternExpression.second()) {
                    single = false;
                }
            }

            normalizedPattern = escapeParseTags(normalizedPattern);
            try {
                pattern = PatternCompiler.compile(normalizedPattern);
            } catch (MalformedPatternException exception) {
                Skript.error("Malformed pattern: " + exception.getMessage());
                return false;
            }
            if (single) {
                single = pattern.countNonNullTypes() <= 1;
            }
            return true;
        }

        Literal<ClassInfoReference> reference =
                (Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) exprs[1]);
        ClassInfoReference wrapped = reference.getSingle(null);
        classInfo = wrapped == null ? null : wrapped.getClassInfo();
        if (classInfo == null) {
            return false;
        }
        if (classInfo.getC() == String.class) {
            Skript.error("Parsing as text is useless as only things that are already text may be parsed");
            return false;
        }
        return canParse(classInfo);
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        String sourceText = text.getSingle(event);
        if (sourceText == null) {
            return null;
        }

        ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler();
        try {
            lastError = null;

            if (classInfo != null) {
                Parser<?> parser = classInfo.getParser();
                if (parser == null) {
                    return null;
                }
                Object value = parser.parse(sourceText, ParseContext.PARSE);
                if (value != null) {
                    Object[] valueArray = (Object[]) Array.newInstance(classInfo.getC(), 1);
                    valueArray[0] = value;
                    return valueArray;
                }
            } else if (pattern != null && patternExpressions != null) {
                MatchResult matchResult = pattern.match(sourceText, SkriptParser.PARSE_LITERALS, ParseContext.PARSE);
                if (matchResult != null) {
                    Expression<?>[] expressions = matchResult.expressions();
                    if (flatten) {
                        List<Object> values = new ArrayList<>();
                        for (int index = 0; index < expressions.length; index++) {
                            Expression<?> expression = expressions[index];
                            if (expression == null) {
                                continue;
                            }
                            if (patternExpressions[index].second()) {
                                values.addAll(Arrays.asList(expression.getArray(null)));
                            } else {
                                values.add(expression.getSingle(null));
                            }
                        }
                        return values.toArray();
                    }

                    int nonNullCount = 0;
                    for (Expression<?> expression : expressions) {
                        if (expression != null) {
                            nonNullCount++;
                        }
                    }

                    Object[] values = new Object[nonNullCount];
                    int valueIndex = 0;
                    for (int index = 0; index < expressions.length; index++) {
                        Expression<?> expression = expressions[index];
                        if (expression == null) {
                            continue;
                        }
                        values[valueIndex++] = patternExpressions[index].second()
                                ? expression.getArray(null)
                                : expression.getSingle(null);
                    }
                    return values;
                }
            }

            LogEntry error = parseLogHandler.getError();
            if (error != null) {
                lastError = error.getMessage();
            } else if (classInfo != null) {
                lastError = sourceText + " could not be parsed as " + indefiniteName(classInfo);
            } else {
                lastError = sourceText + " could not be parsed as \"" + pattern + "\"";
            }
            return null;
        } finally {
            parseLogHandler.clear();
            parseLogHandler.printLog();
        }
    }

    @Override
    public boolean isSingle() {
        return single;
    }

    @Override
    public Class<?> getReturnType() {
        if (classInfo != null) {
            return classInfo.getC();
        }
        if (patternExpressions != null && patternExpressions.length == 1) {
            return patternExpressions[0].first().getC();
        }
        return Object.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return text.toString(event, debug) + " parsed as "
                + (classInfo != null ? indefiniteName(classInfo) : pattern);
    }

    private static boolean canParse(ClassInfo<?> classInfo) {
        Parser<?> parser = classInfo.getParser();
        if (parser == null || !parser.canParse(ParseContext.PARSE)) {
            Skript.error("Text cannot be parsed as " + indefiniteName(classInfo));
            return false;
        }
        return true;
    }

    private static String indefiniteName(ClassInfo<?> classInfo) {
        try {
            return classInfo.getName().withIndefiniteArticle();
        } catch (RuntimeException exception) {
            return classInfo.getCodeName();
        }
    }

    private static String unquote(String value) {
        if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String escapeParseTags(String pattern) {
        StringBuilder builder = new StringBuilder(pattern.length());
        for (int index = 0; index < pattern.length(); index++) {
            char character = pattern.charAt(index);
            if (character == '\u00A6' || character == ':') {
                builder.append('\\');
            }
            builder.append(character);
        }
        return builder.toString();
    }
}
