package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.lang.parser.ParseStackOverflowException;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.registration.SyntaxInfo;

public class SkriptParser {

    public static final int PARSE_EXPRESSIONS = 1 << 0;
    public static final int PARSE_LITERALS = 1 << 1;
    public static final int ALL_FLAGS = PARSE_EXPRESSIONS | PARSE_LITERALS;

    private final String input;
    private final int flags;
    private final ParseContext context;

    public SkriptParser(String input, int flags, ParseContext context) {
        this.input = input;
        this.flags = flags;
        this.context = context;
    }

    public static class ParseResult {
        public String expr = "";
        public Expression<?>[] exprs = new Expression<?>[0];
    }

    public static class ExprInfo {
        public int flagMask = ALL_FLAGS;
        public boolean[] isPlural = new boolean[0];
        public int time;
    }

    public static String notOfType(Class<?>[] types) {
        StringBuilder builder = new StringBuilder("not of type ");
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(types[i].getSimpleName());
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable Expression<? extends T> parseExpression(Class<? extends T>[] returnTypes) {
        String expression = input == null ? "" : input.trim();
        if (expression.isEmpty() || returnTypes == null || returnTypes.length == 0) {
            return null;
        }
        if ((flags & PARSE_EXPRESSIONS) != 0) {
            FunctionReference<? extends T> reference = parseFunctionReference(returnTypes);
            if (reference != null && reference.validateFunction(true)) {
                return new ExprFunctionCall<>(reference, returnTypes);
            }
            Expression<? extends T> registered = parseRegisteredExpression(returnTypes);
            if (registered != null) {
                return registered;
            }
            Expression<? extends T> timed = parseTimedExpression(expression, returnTypes);
            if (timed != null) {
                return timed;
            }
        }
        if ((flags & PARSE_LITERALS) != 0) {
            Literal<? extends T> literal = new UnparsedLiteral(expression).getConvertedExpression(context, returnTypes);
            if (literal != null) {
                return literal;
            }
            Object fallback = parseUntypedLiteral(expression, returnTypes);
            if (fallback != null) {
                return (Expression<? extends T>) new SimpleLiteral<>(fallback, false);
            }
        }
        return null;
    }

    private <T> @Nullable Expression<? extends T> parseTimedExpression(String expression, Class<? extends T>[] returnTypes) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        String normalized = normalizeWhitespace(expression);
        String lowerCase = normalized.toLowerCase(Locale.ENGLISH);
        String prefix = timedPrefix(lowerCase);
        if (prefix == null) {
            return null;
        }
        String remainder = normalized.substring(prefix.length()).trim();
        if (remainder.isEmpty()) {
            return null;
        }
        Expression<? extends T> parsed = new SkriptParser(remainder, flags, context).parseExpression(returnTypes);
        if (parsed == null || !parsed.setTime(1)) {
            return null;
        }
        return parsed;
    }

    private @Nullable String timedPrefix(String lowerCase) {
        if (lowerCase.startsWith("past ")) {
            return "past ";
        }
        if (lowerCase.startsWith("future ")) {
            return "future ";
        }
        return null;
    }

    public @Nullable FunctionReference<?> parseFunctionReference() {
        return parseFunctionReference(null);
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable FunctionReference<? extends T> parseFunctionReference(@Nullable Class<? extends T>[] returnTypes) {
        String script = null;
        var currentScript = ch.njol.skript.lang.parser.ParserInstance.get().getCurrentScript();
        if (currentScript != null && currentScript.getConfig() != null) {
            script = currentScript.getConfig().getFileName();
        }
        return (FunctionReference<? extends T>) FunctionReference.parse(input, script, returnTypes);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> @Nullable Expression<? extends T> parseRegisteredExpression(Class<? extends T>[] returnTypes) {
        Iterable<SyntaxInfo.Expression<?, ?>> registered =
                (Iterable<SyntaxInfo.Expression<?, ?>>) (Iterable<?>) Skript.instance().syntaxRegistry().syntaxes(
                        org.skriptlang.skript.registration.SyntaxRegistry.EXPRESSION
                );

        List<SyntaxInfo.Expression<?, ?>> candidates = new ArrayList<>();
        for (SyntaxInfo.Expression<?, ?> info : registered) {
            if (canReturn(info.returnType(), returnTypes)) {
                candidates.add(info);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        Expression<?> expression = (Expression<?>) parseModern(
                input,
                (Iterator) candidates.iterator(),
                context,
                null
        );
        if (expression == null) {
            return null;
        }
        return (Expression<? extends T>) expression;
    }

    @SuppressWarnings("unchecked")
    public static <E extends SyntaxElement> @Nullable E parseStatic(
            String expr,
            Iterator<? extends SyntaxElementInfo<? extends E>> iterator,
            ParseContext context,
            @Nullable String defaultError
    ) {
        String input = expr == null ? "" : expr.trim();
        ParsingStack parsingStack = ParserInstance.get().getParsingStack();
        while (iterator.hasNext()) {
            SyntaxElementInfo<? extends E> info = iterator.next();
            String[] patterns = info.getPatterns();
            for (int matchedPattern = 0; patterns != null && matchedPattern < patterns.length; matchedPattern++) {
                ParsingStack.Element stackElement = new ParsingStack.Element(legacySyntaxInfo(info), matchedPattern);
                E element = null;
                PatternMatch matched = null;
                try {
                    parsingStack.push(stackElement);
                    matched = match(input, patterns[matchedPattern], context, PARSE_LITERALS);
                    if (matched != null) {
                        element = instantiate(info.getElementClass());
                    }
                } catch (StackOverflowError error) {
                    throw new ParseStackOverflowException(error, new ParsingStack(parsingStack));
                } finally {
                    ParsingStack.Element popped = parsingStack.pop();
                    assert popped.syntaxElementInfo().type() == stackElement.syntaxElementInfo().type()
                            && popped.patternIndex() == stackElement.patternIndex();
                }
                if (matched == null || element == null) {
                    continue;
                }
                ParseResult parseResult = new ParseResult();
                parseResult.expr = input;
                parseResult.exprs = matched.expressions();
                try {
                    if (element.init(matched.expressions(), matchedPattern, Kleenean.FALSE, parseResult)) {
                        return element;
                    }
                } catch (StackOverflowError error) {
                    throw new ParseStackOverflowException(error, new ParsingStack(parsingStack));
                }
            }
        }
        if (defaultError != null && !defaultError.isBlank()) {
            Skript.error(defaultError);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <E extends SyntaxElement> @Nullable E parseModern(
            String expr,
            Iterator<? extends SyntaxInfo<? extends E>> iterator,
            ParseContext context,
            @Nullable String defaultError
    ) {
        String input = expr == null ? "" : expr.trim();
        ParsingStack parsingStack = ParserInstance.get().getParsingStack();
        while (iterator.hasNext()) {
            SyntaxInfo<? extends E> info = iterator.next();
            String[] patterns = info.patterns();
            for (int matchedPattern = 0; patterns != null && matchedPattern < patterns.length; matchedPattern++) {
                ParsingStack.Element stackElement = new ParsingStack.Element(rawSyntaxInfo(info), matchedPattern);
                E element = null;
                PatternMatch matched = null;
                try {
                    parsingStack.push(stackElement);
                    matched = match(input, patterns[matchedPattern], context, ALL_FLAGS);
                    if (matched != null) {
                        element = instantiate(info.type());
                    }
                } catch (StackOverflowError error) {
                    throw new ParseStackOverflowException(error, new ParsingStack(parsingStack));
                } finally {
                    ParsingStack.Element popped = parsingStack.pop();
                    assert popped.syntaxElementInfo().type() == stackElement.syntaxElementInfo().type()
                            && popped.patternIndex() == stackElement.patternIndex();
                }
                if (matched == null || element == null) {
                    continue;
                }
                ParseResult parseResult = new ParseResult();
                parseResult.expr = input;
                parseResult.exprs = matched.expressions();
                try {
                    if (element.init(matched.expressions(), matchedPattern, Kleenean.FALSE, parseResult)) {
                        return element;
                    }
                } catch (StackOverflowError error) {
                    throw new ParseStackOverflowException(error, new ParsingStack(parsingStack));
                }
            }
        }
        if (defaultError != null && !defaultError.isBlank()) {
            Skript.error(defaultError);
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends SyntaxElement> SyntaxInfo<E> legacySyntaxInfo(SyntaxElementInfo<? extends E> info) {
        return new SyntaxInfo((Class<E>) info.getElementClass(), info.getPatterns(), info.getOriginClassPath());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends SyntaxElement> SyntaxInfo<E> rawSyntaxInfo(SyntaxInfo<? extends E> info) {
        return (SyntaxInfo) info;
    }

    private static <E extends SyntaxElement> @Nullable E instantiate(Class<? extends E> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException error) {
            Throwable cause = error.getCause();
            if (cause instanceof StackOverflowError stackOverflowError) {
                throw stackOverflowError;
            }
            return null;
        }
    }

    private record Placeholder(Class<?>[] returnTypes) {
    }

    private record CompiledPattern(String regex, Placeholder[] placeholders) {
    }

    private record PatternMatch(Expression<?>[] expressions) {
    }

    private static @Nullable PatternMatch match(String expr, String pattern, ParseContext context, int flags) {
        if (pattern == null || pattern.isBlank()) {
            return null;
        }
        CompiledPattern compiled = compilePattern(pattern);
        if (compiled == null || compiled.regex().isEmpty()) {
            return null;
        }
        Matcher matcher = Pattern.compile("^" + compiled.regex() + "$", Pattern.CASE_INSENSITIVE)
                .matcher(normalizeWhitespace(expr));
        if (!matcher.matches()) {
            return null;
        }
        Placeholder[] placeholders = compiled.placeholders();
        Expression<?>[] expressions = new Expression<?>[placeholders.length];
        for (int i = 0; i < placeholders.length; i++) {
            String captured = matcher.group(i + 1);
            if (captured == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Expression<?> parsed = new SkriptParser(captured.trim(), flags, context)
                    .parseExpression((Class<? extends Object>[]) placeholders[i].returnTypes());
            if (parsed == null) {
                return null;
            }
            expressions[i] = parsed;
        }
        return new PatternMatch(expressions);
    }

    private static @Nullable CompiledPattern compilePattern(String pattern) {
        String normalizedPattern = normalizePattern(pattern);
        StringBuilder regex = new StringBuilder();
        List<Placeholder> placeholders = new ArrayList<>();

        int optionalDepth = 0;
        for (int i = 0; i < normalizedPattern.length(); i++) {
            char ch = normalizedPattern.charAt(i);
            if (ch == '%') {
                int end = normalizedPattern.indexOf('%', i + 1);
                if (end < 0) {
                    return null;
                }
                String placeholder = normalizedPattern.substring(i + 1, end).trim();
                regex.append("(.+?)");
                placeholders.add(new Placeholder(resolvePlaceholderTypes(placeholder)));
                i = end;
                continue;
            }
            if (Character.isWhitespace(ch)) {
                regex.append("\\s+");
                while (i + 1 < normalizedPattern.length() && Character.isWhitespace(normalizedPattern.charAt(i + 1))) {
                    i++;
                }
                continue;
            }
            if (ch == '[') {
                regex.append("(?:");
                optionalDepth++;
                continue;
            }
            if (ch == ']') {
                if (optionalDepth > 0) {
                    regex.append(")?");
                    optionalDepth--;
                }
                continue;
            }
            if (ch == '(') {
                regex.append("(?:");
                continue;
            }
            if (ch == ')' || ch == '|') {
                regex.append(ch);
                continue;
            }
            regex.append(Pattern.quote(String.valueOf(ch)));
        }
        while (optionalDepth-- > 0) {
            regex.append(")?");
        }
        return new CompiledPattern(regex.toString(), placeholders.toArray(Placeholder[]::new));
    }

    private static String normalizePattern(String pattern) {
        String normalized = normalizeWhitespace(pattern.trim());
        normalized = normalized.replaceAll("\\d+¦", "");
        normalized = normalized.replaceAll("\\[([^\\]]+)]\\s+", "[$1 ]");
        normalized = normalized.replaceAll("([\\p{L}]+)/(\\p{L}+)", "($1|$2)");
        return normalized;
    }

    private static String normalizeWhitespace(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static Class<?>[] resolvePlaceholderTypes(String placeholder) {
        if (placeholder == null || placeholder.isBlank()) {
            return new Class[]{Object.class};
        }
        String raw = placeholder.strip().toLowerCase(Locale.ENGLISH);
        raw = raw.replace("-", "").replace("*", "").replace("~", "");
        String[] parts = raw.split("/");
        List<Class<?>> resolved = new ArrayList<>(parts.length);
        for (String part : parts) {
            String typeName = part.trim();
            if (typeName.endsWith("s") && typeName.length() > 1) {
                typeName = typeName.substring(0, typeName.length() - 1);
            }
            Class<?> type = switch (typeName) {
                case "string", "text" -> String.class;
                case "integer", "int", "number" -> Integer.class;
                case "decimal", "double" -> Double.class;
                case "boolean", "bool" -> Boolean.class;
                case "potioncause", "potioneffectcause" -> FabricPotionEffectCause.class;
                case "object", "value", "any", "expression" -> Object.class;
                default -> Object.class;
            };
            resolved.add(type);
        }
        return resolved.toArray(Class[]::new);
    }

    private static @Nullable Object parseUntypedLiteral(String expression, Class<?>[] returnTypes) {
        for (Class<?> returnType : returnTypes) {
            Object parsed = parseLiteralForType(expression, returnType);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static @Nullable Object parseLiteralForType(String expression, Class<?> returnType) {
        if (returnType == Object.class) {
            Object parsed = parseLiteralForType(expression, Integer.class);
            if (parsed != null) {
                return parsed;
            }
            parsed = parseLiteralForType(expression, Double.class);
            if (parsed != null) {
                return parsed;
            }
            parsed = parseLiteralForType(expression, Boolean.class);
            if (parsed != null) {
                return parsed;
            }
            return parseLiteralForType(expression, String.class);
        }
        if (returnType == String.class) {
            if (expression.length() >= 2 && expression.startsWith("\"") && expression.endsWith("\"")) {
                return expression.substring(1, expression.length() - 1);
            }
            return expression;
        }
        return Classes.parse(expression, returnType, ParseContext.DEFAULT);
    }

    private static boolean containsOnlyObject(Class<?>[] returnTypes) {
        if (returnTypes.length == 0) {
            return false;
        }
        for (Class<?> returnType : returnTypes) {
            if (returnType != Object.class) {
                return false;
            }
        }
        return true;
    }

    private static boolean canReturn(Class<?> actualType, Class<?>[] desiredTypes) {
        for (Class<?> desiredType : desiredTypes) {
            if (desiredType == Object.class || desiredType.isAssignableFrom(actualType)) {
                return true;
            }
        }
        return false;
    }
}
