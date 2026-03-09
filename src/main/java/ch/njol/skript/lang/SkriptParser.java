package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.lang.parser.DefaultValueData;
import ch.njol.skript.lang.parser.ParseStackOverflowException;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.patterns.TypePatternElement;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.NonNullPair;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
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
        public @Nullable SkriptPattern source;
        public List<Matcher> regexes = List.of();
        public List<String> tags = new ArrayList<>();
        public int mark = 0;

        public boolean hasTag(String tag) {
            return tags.contains(tag);
        }
    }

    public static class ExprInfo {
        public int flagMask = ALL_FLAGS;
        public boolean[] isPlural = new boolean[0];
        public int time;
    }

    private static final Pattern VARIABLE_PATTERN = Pattern.compile(
            "((the )?var(iable)? )?\\{.+\\}",
            Pattern.CASE_INSENSITIVE
    );

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

    public static @Nullable NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> validatePattern(String pattern) {
        List<NonNullPair<ClassInfo<?>, Boolean>> pairs = new ArrayList<>();
        int groupLevel = 0;
        int optionalLevel = 0;
        Deque<Character> groups = new LinkedList<>();
        StringBuilder normalized = new StringBuilder(pattern == null ? 0 : pattern.length());
        int last = 0;
        String source = pattern == null ? "" : pattern;
        for (int index = 0; index < source.length(); index++) {
            char character = source.charAt(index);
            if (character == '(') {
                groupLevel++;
                groups.addLast(character);
            } else if (character == '|') {
                if (groupLevel == 0 || (groups.peekLast() != '(' && groups.peekLast() != '|')) {
                    return patternError("Cannot use the pipe character '|' outside of groups. Escape it if you want to match a literal pipe: '\\|'");
                }
                groups.removeLast();
                groups.addLast(character);
            } else if (character == ')') {
                if (groupLevel == 0 || (groups.peekLast() != '(' && groups.peekLast() != '|')) {
                    return patternError("Unexpected closing group bracket ')'. Escape it if you want to match a literal bracket: '\\)'");
                }
                if (groups.peekLast() == '(') {
                    return patternError("(...|...) groups have to contain at least one pipe character '|' to separate it into parts. Escape the brackets if you want to match literal brackets: \"\\(not a group\\)\"");
                }
                groupLevel--;
                groups.removeLast();
            } else if (character == '[') {
                optionalLevel++;
                groups.addLast(character);
            } else if (character == ']') {
                if (optionalLevel == 0 || groups.peekLast() != '[') {
                    return patternError("Unexpected closing optional bracket ']'. Escape it if you want to match a literal bracket: '\\]'");
                }
                optionalLevel--;
                groups.removeLast();
            } else if (character == '<') {
                int closing = source.indexOf('>', index + 1);
                if (closing == -1) {
                    return patternError("Missing closing regex bracket '>'. Escape the '<' if you want to match a literal bracket: '\\<'");
                }
                try {
                    Pattern.compile(source.substring(index + 1, closing));
                } catch (PatternSyntaxException exception) {
                    return patternError("Invalid Regular Expression '" + source.substring(index + 1, closing) + "': "
                            + exception.getLocalizedMessage());
                }
                index = closing;
            } else if (character == '>') {
                return patternError("Unexpected closing regex bracket '>'. Escape it if you want to match a literal bracket: '\\>'");
            } else if (character == '%') {
                int closing = source.indexOf('%', index + 1);
                if (closing == -1) {
                    return patternError("Missing end sign '%' of expression. Escape the percent sign to match a literal '%': '\\%'");
                }
                String rawType = source.substring(index + 1, closing);
                ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(rawType);
                if (classInfo == null) {
                    return patternError("The type '" + rawType + "' could not be found. Please check your spelling or escape the percent signs if you want to match literal %s: \"\\%not an expression\\%\"");
                }
                boolean plural = Classes.isPluralClassInfoUserInput(rawType, classInfo);
                pairs.add(new NonNullPair<>(classInfo, plural));
                normalized.append(source, last, index + 1);
                normalized.append(toEnglishPlural(classInfo.getCodeName(), plural));
                last = closing;
                index = closing;
            } else if (character == '\\') {
                if (index == source.length() - 1) {
                    return patternError("Pattern must not end in an unescaped backslash. Add another backslash to escape it, or remove it altogether.");
                }
                index++;
            }
        }
        normalized.append(source.substring(last));
        @SuppressWarnings("unchecked")
        NonNullPair<ClassInfo<?>, Boolean>[] array = pairs.toArray(new NonNullPair[0]);
        return new NonNullPair<>(normalized.toString(), array);
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
        if (expression == null || !VARIABLE_PATTERN.matcher(expression).matches()) {
            return null;
        }
        int openBrace = expression.indexOf('{');
        int closeBrace = expression.lastIndexOf('}');
        if (openBrace < 0 || closeBrace <= openBrace) {
            return null;
        }
        String variableName = expression.substring(openBrace + 1, closeBrace).trim();
        if (variableName.isEmpty()) {
            return null;
        }
        boolean inExpression = false;
        int variableDepth = 0;
        for (char character : variableName.toCharArray()) {
            if (character == '%' && variableDepth == 0) {
                inExpression = !inExpression;
            }
            if (inExpression) {
                if (character == '{') {
                    variableDepth++;
                } else if (character == '}') {
                    variableDepth--;
                }
            }
            if (!inExpression && (character == '{' || character == '}')) {
                return null;
            }
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
        if (input.isEmpty()) {
            if (defaultError != null && !defaultError.isBlank()) {
                Skript.error(defaultError);
            }
            return null;
        }
        ParsingStack parsingStack = ParserInstance.get().getParsingStack();
        while (iterator.hasNext()) {
            SyntaxElementInfo<? extends E> info = iterator.next();
            String[] patterns = info.getPatterns();
            for (int matchedPattern = 0; patterns != null && matchedPattern < patterns.length; matchedPattern++) {
                ParsingStack.Element stackElement = new ParsingStack.Element(legacySyntaxInfo(info), matchedPattern);
                E element = null;
                SkriptPattern compiledPattern = null;
                MatchResult matched = null;
                try {
                    parsingStack.push(stackElement);
                    compiledPattern = compilePattern(patterns[matchedPattern]);
                    // Upstream parity: legacy parse path should allow both expressions and literals.
                    // Placeholder-level flag masks (e.g., %*type% or %~type%) further restrict what is allowed.
                    matched = compiledPattern != null ? compiledPattern.match(input, ALL_FLAGS, context) : null;
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
                parseResult.source = compiledPattern;
                Expression<?>[] withDefaults;
                try {
                    withDefaults = applyDefaultValues(
                            matched.expressions(),
                            compiledPattern,
                            patterns[matchedPattern]
                    );
                } catch (SkriptAPIException exception) {
                    Skript.error(exception.getMessage());
                    continue;
                }
                if (withDefaults == null) {
                    // Required placeholder omitted and no default available: pattern fails.
                    continue;
                }
                parseResult.exprs = withDefaults;
                parseResult.regexes = matched.regexes();
                parseResult.tags = matched.tags();
                parseResult.mark = matched.mark();
                try {
                    if (element.init(parseResult.exprs, matchedPattern, Kleenean.FALSE, parseResult)) {
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
        if (input.isEmpty()) {
            if (defaultError != null && !defaultError.isBlank()) {
                Skript.error(defaultError);
            }
            return null;
        }
        ParsingStack parsingStack = ParserInstance.get().getParsingStack();
        while (iterator.hasNext()) {
            SyntaxInfo<? extends E> info = iterator.next();
            String[] patterns = info.patterns();
            for (int matchedPattern = 0; patterns != null && matchedPattern < patterns.length; matchedPattern++) {
                ParsingStack.Element stackElement = new ParsingStack.Element(rawSyntaxInfo(info), matchedPattern);
                E element = null;
                SkriptPattern compiledPattern = null;
                MatchResult matched = null;
                try {
                    parsingStack.push(stackElement);
                    compiledPattern = compilePattern(patterns[matchedPattern]);
                    matched = compiledPattern != null ? compiledPattern.match(input, ALL_FLAGS, context) : null;
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
                parseResult.source = compiledPattern;
                Expression<?>[] withDefaults;
                try {
                    withDefaults = applyDefaultValues(
                            matched.expressions(),
                            compiledPattern,
                            patterns[matchedPattern]
                    );
                } catch (SkriptAPIException exception) {
                    Skript.error(exception.getMessage());
                    continue;
                }
                if (withDefaults == null) {
                    // Required placeholder omitted and no default available: pattern fails.
                    continue;
                }
                parseResult.exprs = withDefaults;
                parseResult.regexes = matched.regexes();
                parseResult.tags = matched.tags();
                parseResult.mark = matched.mark();
                try {
                    if (element.init(parseResult.exprs, matchedPattern, Kleenean.FALSE, parseResult)) {
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

    private static final Map<String, SkriptPattern> PATTERNS = new ConcurrentHashMap<>();

    private static @Nullable SkriptPattern compilePattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return null;
        }
        try {
            return PATTERNS.computeIfAbsent(pattern, PatternCompiler::compile);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static @Nullable MatchResult match(String expr, String pattern, ParseContext context, int flags) {
        SkriptPattern compiledPattern = compilePattern(pattern);
        if (compiledPattern == null) {
            return null;
        }
        return compiledPattern.match(expr, flags, context);
    }

    private static @Nullable Expression<?>[] applyDefaultValues(
            Expression<?>[] expressions,
            @Nullable SkriptPattern compiledPattern,
            String pattern
    ) {
        if (compiledPattern == null || expressions.length == 0) {
            return expressions;
        }

        List<TypePatternElement> typePatterns = new ArrayList<>(compiledPattern.getElements(TypePatternElement.class));
        if (typePatterns.isEmpty()) {
            return expressions;
        }
        typePatterns.sort(Comparator.comparingInt(TypePatternElement::expressionIndex));
        Set<Integer> activeExpressionIndices = compiledPattern.getActiveExpressionIndices(expressions);

        DefaultValueData defaultValues = ParserInstance.get().getData(DefaultValueData.class);
        for (TypePatternElement typePattern : typePatterns) {
            int expressionIndex = typePattern.expressionIndex();
            if (expressionIndex >= expressions.length
                    || !activeExpressionIndices.contains(expressionIndex)
                    || expressions[expressionIndex] != null
                    || typePattern.isOptional()) {
                continue;
            }
            DefaultExpression<?> defaultExpression = findDefaultValue(defaultValues, typePattern, pattern);
            if (defaultExpression != null) {
                expressions[expressionIndex] = defaultExpression;
                continue;
            }
            // Upstream parity: if a required placeholder remains omitted and no default is available,
            // the entire pattern must fail.
            return null;
        }
        return expressions;
    }

    private static @Nullable DefaultExpression<?> findDefaultValue(
            DefaultValueData defaultValues,
            TypePatternElement typePattern,
            String pattern
    ) {
        ExprInfo exprInfo = new ExprInfo();
        exprInfo.flagMask = typePattern.flagMask();
        exprInfo.isPlural = typePattern.pluralities();
        exprInfo.time = typePattern.time();

        Class<?>[] returnTypes = typePattern.returnTypes();
        EnumMap<DefaultExpressionUtils.DefaultExpressionError, List<String>> failed =
                new EnumMap<>(DefaultExpressionUtils.DefaultExpressionError.class);
        for (int i = 0; i < returnTypes.length; i++) {
            DefaultExpression<?> defaultExpression = getDefaultValue(defaultValues, returnTypes[i]);
            DefaultExpressionUtils.DefaultExpressionError error =
                    DefaultExpressionUtils.isValid(defaultExpression, exprInfo, i);
            if (error != null) {
                failed.computeIfAbsent(error, ignored -> new ArrayList<>())
                        .add(getDefaultCodeName(returnTypes[i]));
                continue;
            }
            if (defaultExpression == null) {
                continue;
            }
            if (defaultExpression.init()) {
                return defaultExpression;
            }
        }
        if (!failed.isEmpty()) {
            List<String> errors = new ArrayList<>();
            for (Map.Entry<DefaultExpressionUtils.DefaultExpressionError, List<String>> entry : failed.entrySet()) {
                errors.add(entry.getKey().getError(entry.getValue(), pattern));
            }
            throw new SkriptAPIException(String.join("\n", errors));
        }
        return null;
    }

    private static String getDefaultCodeName(Class<?> returnType) {
        ClassInfo<?> classInfo = Classes.getExactClassInfo(returnType);
        if (classInfo != null) {
            return classInfo.getCodeName();
        }
        return returnType.getSimpleName().toLowerCase(Locale.ENGLISH);
    }

    private static @Nullable DefaultExpression<?> getDefaultValue(
            DefaultValueData defaultValues,
            Class<?> returnType
    ) {
        DefaultExpression<?> defaultExpression = defaultValues.getDefaultValue(returnType);
        if (defaultExpression != null) {
            return defaultExpression;
        }

        return Classes.getDefaultExpression(returnType);
    }

    private static String normalizeWhitespace(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
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

    private static boolean bracketsMatch(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '[' && close == ']')
                || (open == '{' && close == '}');
    }

    private static String toEnglishPlural(String value, boolean plural) {
        if (!plural || value == null || value.isBlank()) {
            return value;
        }
        if (value.endsWith("y") && value.length() > 1) {
            char previous = value.charAt(value.length() - 2);
            if ("aeiou".indexOf(Character.toLowerCase(previous)) == -1) {
                return value.substring(0, value.length() - 1) + "ies";
            }
        }
        if (value.endsWith("s") || value.endsWith("x") || value.endsWith("z")
                || value.endsWith("ch") || value.endsWith("sh")) {
            return value + "es";
        }
        return value + "s";
    }

    private static <T> @Nullable T patternError(String error) {
        Skript.error("Invalid pattern: " + error);
        return null;
    }
}
