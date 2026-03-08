package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprInput;
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
import java.util.Set;
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
        public List<Matcher> regexes = List.of();
        public Set<String> tags = Set.of();

        public boolean hasTag(String tag) {
            return tags.contains(tag);
        }
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

    public static boolean validateLine(String line) {
        if (line == null) {
            return false;
        }
        boolean inQuotes = false;
        List<Character> brackets = new ArrayList<>();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                continue;
            }
            if (inQuotes) {
                continue;
            }
            if (ch == '(' || ch == '[' || ch == '{') {
                brackets.add(ch);
                continue;
            }
            if (ch != ')' && ch != ']' && ch != '}') {
                continue;
            }
            if (brackets.isEmpty() || !bracketsMatch(brackets.remove(brackets.size() - 1), ch)) {
                Skript.error("Unmatched brackets in line: " + line);
                return false;
            }
        }
        if (inQuotes) {
            Skript.error("Unmatched double quotes in line: " + line);
            return false;
        }
        if (!brackets.isEmpty()) {
            Skript.error("Unmatched brackets in line: " + line);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable Expression<? extends T> parseExpression(Class<? extends T>[] returnTypes) {
        String expression = input == null ? "" : input.trim();
        if (expression.isEmpty() || returnTypes == null || returnTypes.length == 0) {
            return null;
        }
        if ((flags & PARSE_EXPRESSIONS) != 0) {
            Expression<? extends T> inputExpression = parseInputExpression(expression, returnTypes);
            if (inputExpression != null) {
                return inputExpression;
            }
            Expression<? extends T> variableExpression = parseVariableExpression(expression, returnTypes);
            if (variableExpression != null) {
                return variableExpression;
            }
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
            String quotedString = parseQuotedStringLiteral(expression);
            if (quotedString != null) {
                if (canReturnQuotedString(returnTypes)) {
                    return (Expression<? extends T>) LiteralString.of(quotedString);
                }
                return null;
            }
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

    @SuppressWarnings("unchecked")
    private <T> @Nullable Expression<? extends T> parseVariableExpression(String expression, Class<? extends T>[] returnTypes) {
        if (expression == null || expression.length() < 3 || expression.charAt(0) != '{'
                || expression.charAt(expression.length() - 1) != '}') {
            return null;
        }
        String variableName = expression.substring(1, expression.length() - 1).trim();
        if (variableName.isEmpty()) {
            return null;
        }
        return (Expression<? extends T>) Variable.newInstance(variableName, returnTypes);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> @Nullable Expression<? extends T> parseInputExpression(String expression, Class<? extends T>[] returnTypes) {
        InputSource inputSource = ParserInstance.get().getData(InputSource.InputData.class).getSource();
        if (inputSource == null || expression == null) {
            return null;
        }
        String normalized = normalizeWhitespace(expression);
        String lowerCase = normalized.toLowerCase(Locale.ENGLISH);
        ExprInput<?> parsed;
        if ("input".equals(lowerCase)) {
            parsed = new ExprInput<>();
        } else if ("input index".equals(lowerCase)) {
            if (!inputSource.hasIndices()) {
                return null;
            }
            parsed = ExprInput.inputIndex();
        } else {
            parsed = parseTypedInputExpression(normalized, lowerCase);
            if (parsed == null) {
                return null;
            }
        }
        return (Expression<? extends T>) parsed.getConvertedExpression((Class[]) returnTypes);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @Nullable ExprInput<?> parseTypedInputExpression(String normalizedExpression, String lowerCaseExpression) {
        if (!lowerCaseExpression.endsWith(" input") || lowerCaseExpression.length() <= " input".length()) {
            return null;
        }
        String typeText = normalizedExpression.substring(0, normalizedExpression.length() - " input".length()).trim();
        if (typeText.isEmpty()) {
            return null;
        }

        ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(typeText);
        if (classInfo == null) {
            return null;
        }
        if (Classes.isPluralClassInfoUserInput(typeText, classInfo)) {
            Skript.error("An input can only be a single value! Please use a singular type.");
            return null;
        }
        return ExprInput.typed(classInfo.getCodeName(), (Class) classInfo.getC());
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
                parseResult.regexes = List.of(matched.regexes());
                parseResult.tags = matched.tags();
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
                parseResult.regexes = List.of(matched.regexes());
                parseResult.tags = matched.tags();
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

    private record Capture(CaptureType type, @Nullable Class<?>[] returnTypes, @Nullable String regex) {
        private static Capture expression(Class<?>[] returnTypes) {
            return new Capture(CaptureType.EXPRESSION, returnTypes, null);
        }

        private static Capture regex(String pattern) {
            return new Capture(CaptureType.REGEX, null, pattern);
        }
    }

    private enum CaptureType {
        EXPRESSION,
        REGEX
    }

    private record CompiledPattern(String regex, Capture[] captures, Set<String> tags) {
    }

    private record PatternMatch(Expression<?>[] expressions, Matcher[] regexes, Set<String> tags) {
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
        List<Expression<?>> expressions = new ArrayList<>();
        List<Matcher> regexes = new ArrayList<>();
        Capture[] captures = compiled.captures();
        for (int i = 0; i < captures.length; i++) {
            String captured = matcher.group(i + 1);
            Capture capture = captures[i];
            if (capture.type() == CaptureType.EXPRESSION) {
                if (captured == null) {
                    expressions.add(null);
                    continue;
                }
                @SuppressWarnings("unchecked")
                Expression<?> parsed = new SkriptParser(captured.trim(), flags, context)
                        .parseExpression((Class<? extends Object>[]) capture.returnTypes());
                if (parsed == null) {
                    return null;
                }
                expressions.add(parsed);
                continue;
            }

            if (captured == null) {
                return null;
            }
            String regex = capture.regex();
            if (regex == null) {
                return null;
            }
            Matcher rawMatcher = Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE)
                    .matcher(captured.trim());
            if (!rawMatcher.matches()) {
                return null;
            }
            regexes.add(rawMatcher);
        }
        return new PatternMatch(
                expressions.toArray(Expression<?>[]::new),
                regexes.toArray(Matcher[]::new),
                compiled.tags()
        );
    }

    private static @Nullable CompiledPattern compilePattern(String pattern) {
        Set<String> tags = Set.of();
        String normalizedPattern = normalizePattern(pattern);
        if (normalizedPattern.startsWith("implicit:")) {
            tags = Set.of("implicit");
            normalizedPattern = normalizedPattern.substring("implicit:".length()).trim();
        }
        List<Capture> captures = new ArrayList<>();
        String regex;
        try {
            regex = compileSequence(normalizedPattern, captures);
        } catch (IllegalArgumentException exception) {
            return null;
        }
        return new CompiledPattern(regex, captures.toArray(Capture[]::new), tags);
    }

    private static String normalizePattern(String pattern) {
        String normalized = normalizeWhitespace(pattern.trim());
        normalized = normalized.replaceAll("\\d+¦", "");
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

    private static @Nullable String parseQuotedStringLiteral(String expression) {
        if (expression.length() >= 2 && expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        }
        return null;
    }

    private static boolean canReturnQuotedString(Class<?>[] returnTypes) {
        for (Class<?> returnType : returnTypes) {
            if (returnType == Object.class || returnType.isAssignableFrom(String.class)) {
                return true;
            }
        }
        return false;
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

    private static String compileSequence(String pattern, List<Capture> captures) {
        if (pattern.isEmpty()) {
            return "";
        }
        if (pattern.isBlank()) {
            return "\\s+";
        }
        List<String> tokens = splitTopLevelTokens(pattern);
        StringBuilder regex = new StringBuilder();
        if (Character.isWhitespace(pattern.charAt(0))) {
            regex.append("\\s+");
        }
        boolean hasRequiredBefore = false;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            boolean optionalRoot = isWrappedBy(token, '[', ']');
            if (!optionalRoot) {
                if (hasRequiredBefore) {
                    regex.append("\\s+");
                }
                regex.append(compileToken(token, captures));
                hasRequiredBefore = true;
                continue;
            }

            String inner = token.substring(1, token.length() - 1);
            String body = compileGroupContent(inner, captures);
            boolean hasRequiredAfter = hasRequiredTokenAfter(tokens, i + 1);
            if (!hasRequiredBefore) {
                regex.append("(?:").append(body);
                if (hasRequiredAfter) {
                    regex.append("\\s+");
                }
                regex.append(")?");
                continue;
            }
            regex.append("(?:\\s+").append(body).append(")?");
        }
        if (Character.isWhitespace(pattern.charAt(pattern.length() - 1))) {
            regex.append("\\s+");
        }
        return regex.toString();
    }

    private static String compileToken(String token, List<Capture> captures) {
        if (token.isBlank()) {
            return "\\s+";
        }
        if (isWrappedBy(token, '(', ')')) {
            return "(?:" + compileAlternatives(token.substring(1, token.length() - 1), captures) + ")";
        }

        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (ch == '%') {
                int end = token.indexOf('%', i + 1);
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed placeholder in token: " + token);
                }
                String placeholder = token.substring(i + 1, end).trim();
                regex.append("(.+?)");
                captures.add(Capture.expression(resolvePlaceholderTypes(placeholder)));
                i = end;
                continue;
            }
            if (ch == '<') {
                int end = token.indexOf('>', i + 1);
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed regex capture in token: " + token);
                }
                String rawRegex = token.substring(i + 1, end).trim();
                if (rawRegex.isEmpty()) {
                    throw new IllegalArgumentException("Empty regex capture in token: " + token);
                }
                regex.append('(').append(rawRegex).append(')');
                captures.add(Capture.regex(rawRegex));
                i = end;
                continue;
            }
            if (ch == '[') {
                int end = findMatching(token, i, '[', ']');
                regex.append("(?:")
                        .append(compileGroupContent(token.substring(i + 1, end), captures))
                        .append(")?");
                i = end;
                continue;
            }
            if (ch == '(') {
                int end = findMatching(token, i, '(', ')');
                regex.append("(?:")
                        .append(compileAlternatives(token.substring(i + 1, end), captures))
                        .append(')');
                i = end;
                continue;
            }
            regex.append(Pattern.quote(String.valueOf(ch)));
        }
        return regex.toString();
    }

    private static String compileAlternatives(String pattern, List<Capture> captures) {
        List<String> branches = splitTopLevel(pattern, '|');
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < branches.size(); i++) {
            if (i > 0) {
                regex.append('|');
            }
            regex.append(compileSequence(branches.get(i), captures));
        }
        return regex.toString();
    }

    private static String compileGroupContent(String pattern, List<Capture> captures) {
        List<String> branches = splitTopLevel(pattern, '|');
        if (branches.size() > 1) {
            StringBuilder regex = new StringBuilder();
            for (int i = 0; i < branches.size(); i++) {
                if (i > 0) {
                    regex.append('|');
                }
                regex.append(compileSequence(branches.get(i), captures));
            }
            return "(?:" + regex + ")";
        }
        return compileSequence(pattern, captures);
    }

    private static List<String> splitTopLevelTokens(String pattern) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int squareDepth = 0;
        int parenDepth = 0;
        boolean inPlaceholder = false;
        boolean inRegex = false;
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (!inPlaceholder && !inRegex && squareDepth == 0 && parenDepth == 0 && Character.isWhitespace(ch)) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(ch);
            if (ch == '%' && !inRegex) {
                inPlaceholder = !inPlaceholder;
                continue;
            }
            if (inPlaceholder) {
                continue;
            }
            if (ch == '<' && !inRegex) {
                inRegex = true;
                continue;
            }
            if (ch == '>' && inRegex) {
                inRegex = false;
                continue;
            }
            if (inRegex) {
                continue;
            }
            if (ch == '[') {
                squareDepth++;
            } else if (ch == ']') {
                squareDepth--;
            } else if (ch == '(') {
                parenDepth++;
            } else if (ch == ')') {
                parenDepth--;
            }
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    private static List<String> splitTopLevel(String pattern, char delimiter) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int squareDepth = 0;
        int parenDepth = 0;
        boolean inPlaceholder = false;
        boolean inRegex = false;
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (!inPlaceholder && !inRegex && squareDepth == 0 && parenDepth == 0 && ch == delimiter) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
            if (ch == '%' && !inRegex) {
                inPlaceholder = !inPlaceholder;
                continue;
            }
            if (inPlaceholder) {
                continue;
            }
            if (ch == '<' && !inRegex) {
                inRegex = true;
                continue;
            }
            if (ch == '>' && inRegex) {
                inRegex = false;
                continue;
            }
            if (inRegex) {
                continue;
            }
            if (ch == '[') {
                squareDepth++;
            } else if (ch == ']') {
                squareDepth--;
            } else if (ch == '(') {
                parenDepth++;
            } else if (ch == ')') {
                parenDepth--;
            }
        }
        parts.add(current.toString());
        return parts;
    }

    private static boolean isWrappedBy(String value, char open, char close) {
        if (value.length() < 2 || value.charAt(0) != open) {
            return false;
        }
        return findMatching(value, 0, open, close) == value.length() - 1;
    }

    private static boolean hasRequiredTokenAfter(List<String> tokens, int startIndex) {
        for (int i = startIndex; i < tokens.size(); i++) {
            if (!isWrappedBy(tokens.get(i), '[', ']')) {
                return true;
            }
        }
        return false;
    }

    private static int findMatching(String value, int openIndex, char open, char close) {
        int depth = 0;
        boolean inPlaceholder = false;
        boolean inRegex = false;
        for (int i = openIndex; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '%' && !inRegex) {
                inPlaceholder = !inPlaceholder;
                continue;
            }
            if (inPlaceholder) {
                continue;
            }
            if (ch == '<' && !inRegex) {
                inRegex = true;
                continue;
            }
            if (ch == '>' && inRegex) {
                inRegex = false;
                continue;
            }
            if (inRegex) {
                continue;
            }
            if (ch == open) {
                depth++;
                continue;
            }
            if (ch == close) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("Unmatched delimiter in value: " + value);
    }

    private static boolean bracketsMatch(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '[' && close == ']')
                || (open == '{' && close == '}');
    }
}
