package ch.njol.skript.patterns;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;

public final class PatternCompiler {

    private PatternCompiler() {
    }

    public static SkriptPattern compile(String pattern) {
        return new SkriptPattern(pattern, compilePattern(pattern));
    }

    static CompiledPattern compilePattern(@Nullable String pattern) {
        String source = pattern == null ? "" : pattern;
        String normalizedPattern = normalizePattern(source);
        List<CaptureSpec> captures = new ArrayList<>();
        String regex = compileSequence(normalizedPattern, captures);
        AtomicInteger expressionAmount = new AtomicInteger();
        PatternElement first = compileElementSequence(normalizedPattern, expressionAmount);
        return new CompiledPattern(
                source,
                Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE),
                List.copyOf(captures),
                first,
                expressionAmount.get()
        );
    }

    static String normalizeInput(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String normalizePattern(String pattern) {
        String normalized = normalizeInput(pattern == null ? "" : pattern.trim());
        return normalized.replaceAll("([\\p{L}]+)/(\\p{L}+)", "($1|$2)");
    }

    static Class<?>[] resolvePlaceholderTypes(String placeholder) {
        return parsePlaceholderSpec(placeholder).returnTypes();
    }

    private static PlaceholderSpec parsePlaceholderSpec(String placeholder) {
        if (placeholder == null || placeholder.isBlank()) {
            return new PlaceholderSpec(new Class[]{Object.class}, new boolean[]{false}, SkriptParser.ALL_FLAGS, 0, false);
        }
        String value = placeholder.strip();
        int cursor = 0;
        int flagMask = SkriptParser.ALL_FLAGS;
        boolean optional = false;
        while (cursor < value.length()) {
            char marker = value.charAt(cursor);
            if (marker == '-') {
                optional = true;
            } else if (marker == '*') {
                flagMask &= ~SkriptParser.PARSE_EXPRESSIONS;
            } else if (marker == '~') {
                flagMask &= ~SkriptParser.PARSE_LITERALS;
            } else {
                break;
            }
            cursor++;
        }

        int time = 0;
        int timeIndex = value.indexOf('@', cursor);
        String typeText = timeIndex >= 0 ? value.substring(cursor, timeIndex).trim() : value.substring(cursor).trim();
        if (timeIndex >= 0) {
            String timeText = value.substring(timeIndex + 1).trim();
            if (timeText.isEmpty()) {
                throw new IllegalArgumentException("Missing time state in placeholder: " + placeholder);
            }
            time = Integer.parseInt(timeText);
        }

        if (typeText.isEmpty()) {
            return new PlaceholderSpec(new Class[]{Object.class}, new boolean[]{false}, flagMask, time, optional);
        }

        String[] parts = typeText.split("/");
        List<Class<?>> resolved = new ArrayList<>(parts.length);
        boolean[] pluralities = new boolean[parts.length];
        for (String part : parts) {
            ResolvedPlaceholderType resolvedType = resolvePlaceholderType(part.trim());
            if (resolvedType.type() == Object.class && resolvedType.raw().isEmpty()) {
                resolved.add(Object.class);
                pluralities[resolved.size() - 1] = false;
                continue;
            }
            resolved.add(resolvedType.type());
            pluralities[resolved.size() - 1] = resolvedType.plural();
        }
        return new PlaceholderSpec(resolved.toArray(Class[]::new), pluralities, flagMask, time, optional);
    }

    private static ResolvedPlaceholderType resolvePlaceholderType(String rawType) {
        String value = rawType == null ? "" : rawType.trim();
        if (value.isEmpty()) {
            return new ResolvedPlaceholderType(Object.class, false, "");
        }

        ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(value);
        if (classInfo != null) {
            return new ResolvedPlaceholderType(
                    classInfo.getC(),
                    Classes.isPluralClassInfoUserInput(value, classInfo),
                    value
            );
        }

        String normalized = value.toLowerCase(Locale.ENGLISH).replace("-", "").replace(" ", "");
        boolean plural = normalized.endsWith("s") && normalized.length() > 1;
        if (plural) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        Class<?> type = switch (normalized) {
            case "string", "text" -> String.class;
            case "integer", "int", "number" -> Integer.class;
            case "decimal", "double" -> Double.class;
            case "boolean", "bool" -> Boolean.class;
            case "potioncause", "potioneffectcause" -> FabricPotionEffectCause.class;
            case "object", "value", "any", "expression" -> Object.class;
            default -> Object.class;
        };
        return new ResolvedPlaceholderType(type, plural, value);
    }

    private static TypePatternElement buildTypePatternElement(String placeholder, int expressionIndex) {
        PlaceholderSpec spec = parsePlaceholderSpec(placeholder);
        return new TypePatternElement(
                placeholder,
                spec.returnTypes(),
                spec.pluralities(),
                spec.flagMask(),
                spec.time(),
                spec.optional(),
                expressionIndex
        );
    }

    private static @Nullable PatternElement compileElementSequence(String pattern, AtomicInteger expressionAmount) {
        if (pattern == null || pattern.isEmpty()) {
            return null;
        }
        List<String> tokens = splitTopLevelTokens(pattern);
        PatternElement first = null;
        PatternElement last = null;
        for (String token : tokens) {
            PatternElement tokenFirst = compileElementToken(token, expressionAmount);
            if (tokenFirst == null) {
                continue;
            }
            if (first == null) {
                first = tokenFirst;
            } else if (last != null) {
                last.setOriginalNext(tokenFirst);
            }
            last = lastElement(tokenFirst);
        }
        return first;
    }

    private static @Nullable PatternElement compileElementToken(String token, AtomicInteger expressionAmount) {
        if (token.isBlank()) {
            return new LiteralPatternElement(token);
        }
        PatternElement autoTagged = compileLeadingAutoTagElement(token, expressionAmount);
        if (autoTagged != null) {
            return autoTagged;
        }

        PatternElement first = null;
        PatternElement last = null;
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (ch == '\\') {
                if (i + 1 >= token.length()) {
                    throw new IllegalArgumentException("Pattern ends with an escape: " + token);
                }
                literal.append(token.charAt(i + 1));
                i++;
                continue;
            }
            if (ch == ':' || ch == '¦') {
                String metadataText = literal.toString().trim();
                literal.setLength(0);
                PatternElement metadata = metadataElement(metadataText, ch, token);
                if (metadata != null) {
                    if (first == null) {
                        first = metadata;
                    } else if (last != null) {
                        last.setOriginalNext(metadata);
                    }
                    last = lastElement(metadata);
                }
                continue;
            }
            if (ch == '%') {
                first = appendElement(first, last, flushLiteral(literal));
                last = first == null ? null : lastElement(first);
                int end = findUnescaped(token, i + 1, '%');
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed placeholder in token: " + token);
                }
                String placeholder = token.substring(i + 1, end).trim();
                PatternElement type = buildTypePatternElement(placeholder, expressionAmount.getAndIncrement());
                if (first == null) {
                    first = type;
                } else if (last != null) {
                    last.setOriginalNext(type);
                }
                last = type;
                i = end;
                continue;
            }
            if (ch == '<') {
                first = appendElement(first, last, flushLiteral(literal));
                last = first == null ? null : lastElement(first);
                int end = findUnescaped(token, i + 1, '>');
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed regex capture in token: " + token);
                }
                String rawRegex = token.substring(i + 1, end).trim();
                PatternElement regex = new RegexPatternElement(rawRegex);
                if (first == null) {
                    first = regex;
                } else if (last != null) {
                    last.setOriginalNext(regex);
                }
                last = regex;
                i = end;
                continue;
            }
            if (ch == '[') {
                first = appendElement(first, last, flushLiteral(literal));
                last = first == null ? null : lastElement(first);
                int end = findMatching(token, i, '[', ']');
                PatternElement optional = new OptionalPatternElement(
                        compileElementGroupContent(token.substring(i + 1, end), expressionAmount)
                );
                if (first == null) {
                    first = optional;
                } else if (last != null) {
                    last.setOriginalNext(optional);
                }
                last = optional;
                i = end;
                continue;
            }
            if (ch == '(') {
                first = appendElement(first, last, flushLiteral(literal));
                last = first == null ? null : lastElement(first);
                int end = findMatching(token, i, '(', ')');
                PatternElement group = compileParenthesizedElementGroup(token.substring(i + 1, end), expressionAmount);
                if (first == null) {
                    first = group;
                } else if (last != null) {
                    last.setOriginalNext(group);
                }
                last = lastElement(group);
                i = end;
                continue;
            }
            literal.append(ch);
        }
        PatternElement literalElement = flushLiteral(literal);
        if (literalElement != null) {
            if (first == null) {
                first = literalElement;
            } else if (last != null) {
                last.setOriginalNext(literalElement);
            }
        }
        return first;
    }

    private static @Nullable PatternElement compileLeadingAutoTagElement(String token, AtomicInteger expressionAmount) {
        if (token.length() < 2 || token.charAt(0) != ':') {
            return null;
        }
        char next = token.charAt(1);
        if (next == '(' || next == '[') {
            return compileLeadingAutoTaggedGroupElement(token, expressionAmount);
        }
        String tag = deriveLeadingLiteralTag(token.substring(1));
        if (tag == null) {
            return null;
        }
        ParseTagPatternElement metadata = parseTagElement(tag);
        PatternElement body = compileElementToken(token.substring(1), expressionAmount);
        metadata.setOriginalNext(body);
        return metadata;
    }

    private static @Nullable PatternElement compileLeadingAutoTaggedGroupElement(
            String token,
            AtomicInteger expressionAmount
    ) {
        char open = token.charAt(1);
        char close = open == '(' ? ')' : ']';
        int end = findMatching(token, 1, open, close);
        List<String> branches = splitTopLevel(token.substring(2, end), '|');
        if (branches.size() <= 1) {
            return null;
        }

        List<PatternElement> compiledBranches = new ArrayList<>();
        for (String branch : branches) {
            PatternElement branchElement = compileElementSequence(branch, expressionAmount);
            String tag = deriveLeadingLiteralTag(branch);
            if (tag != null) {
                ParseTagPatternElement metadata = parseTagElement(tag);
                metadata.setOriginalNext(branchElement);
                branchElement = metadata;
            }
            compiledBranches.add(branchElement == null ? new LiteralPatternElement("") : branchElement);
        }

        PatternElement grouped = new ChoicePatternElement(compiledBranches);
        if (open == '[') {
            grouped = new OptionalPatternElement(grouped);
        }

        String suffix = token.substring(end + 1);
        if (suffix.isEmpty()) {
            return grouped;
        }

        PatternElement suffixElement = compileElementToken(suffix, expressionAmount);
        lastElement(grouped).setOriginalNext(suffixElement);
        return grouped;
    }

    private static @Nullable PatternElement compileElementGroupContent(String pattern, AtomicInteger expressionAmount) {
        List<String> branches = splitTopLevel(pattern, '|');
        if (branches.size() == 1) {
            return compileElementSequence(pattern, expressionAmount);
        }
        List<PatternElement> compiledBranches = new ArrayList<>(branches.size());
        for (String branch : branches) {
            PatternElement compiled = compileElementSequence(branch, expressionAmount);
            compiledBranches.add(compiled == null ? new LiteralPatternElement("") : compiled);
        }
        return new ChoicePatternElement(compiledBranches);
    }

    private static PatternElement compileParenthesizedElementGroup(String pattern, AtomicInteger expressionAmount) {
        List<String> branches = splitTopLevel(pattern, '|');
        if (branches.size() == 1) {
            return new GroupPatternElement(compileElementSequence(pattern, expressionAmount));
        }
        List<PatternElement> compiledBranches = new ArrayList<>(branches.size());
        for (String branch : branches) {
            PatternElement compiled = compileElementSequence(branch, expressionAmount);
            compiledBranches.add(compiled == null ? new LiteralPatternElement("") : compiled);
        }
        return new ChoicePatternElement(compiledBranches);
    }

    private static @Nullable PatternElement metadataElement(String metadata, char kind, String token) {
        if (kind == '¦') {
            if (metadata.isEmpty()) {
                throw new IllegalArgumentException("Empty parse mark in token: " + token);
            }
            try {
                return new ParseTagPatternElement(null, Integer.parseInt(metadata));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid parse mark in token: " + token, exception);
            }
        }
        if (metadata.isEmpty()) {
            return null;
        }
        return parseTagElement(metadata);
    }

    private static ParseTagPatternElement parseTagElement(String metadata) {
        int mark = 0;
        try {
            mark = Integer.parseInt(metadata);
        } catch (NumberFormatException ignored) {
        }
        return new ParseTagPatternElement(metadata, mark);
    }

    private static @Nullable PatternElement flushLiteral(StringBuilder literal) {
        if (literal.isEmpty()) {
            return null;
        }
        PatternElement element = new LiteralPatternElement(literal.toString());
        literal.setLength(0);
        return element;
    }

    private static @Nullable PatternElement appendElement(
            @Nullable PatternElement first,
            @Nullable PatternElement last,
            @Nullable PatternElement next
    ) {
        if (next == null) {
            return first;
        }
        if (first == null) {
            return next;
        }
        if (last != null) {
            last.setOriginalNext(next);
        }
        return first;
    }

    private static PatternElement lastElement(PatternElement first) {
        PatternElement current = first;
        while (current.getOriginalNext() != null) {
            current = current.getOriginalNext();
        }
        return current;
    }

    private static String compileSequence(String pattern, List<CaptureSpec> captures) {
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

    private static String compileToken(String token, List<CaptureSpec> captures) {
        if (token.isBlank()) {
            return "\\s+";
        }
        String autoTagged = compileLeadingAutoTag(token, captures);
        if (autoTagged != null) {
            return autoTagged;
        }
        if (isWrappedBy(token, '(', ')')) {
            return "(?:" + compileAlternatives(token.substring(1, token.length() - 1), captures) + ")";
        }

        StringBuilder regex = new StringBuilder();
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (ch == '\\') {
                if (i + 1 >= token.length()) {
                    throw new IllegalArgumentException("Pattern ends with an escape: " + token);
                }
                literal.append(token.charAt(i + 1));
                i++;
                continue;
            }
            if (ch == ':' || ch == '¦') {
                addMetadataCapture(regex, captures, literal.toString().trim(), ch, token);
                literal.setLength(0);
                continue;
            }
            if (ch == '%') {
                flushLiteral(regex, literal);
                int end = findUnescaped(token, i + 1, '%');
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed placeholder in token: " + token);
                }
                String placeholder = token.substring(i + 1, end).trim();
                regex.append("(.+?)");
                captures.add(CaptureSpec.expression(buildTypePatternElement(placeholder, -1)));
                i = end;
                continue;
            }
            if (ch == '<') {
                flushLiteral(regex, literal);
                int end = findUnescaped(token, i + 1, '>');
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed regex capture in token: " + token);
                }
                String rawRegex = token.substring(i + 1, end).trim();
                if (rawRegex.isEmpty()) {
                    throw new IllegalArgumentException("Empty regex capture in token: " + token);
                }
                regex.append('(').append(rawRegex).append(')');
                captures.add(CaptureSpec.regex(Pattern.compile("^" + rawRegex + "$", Pattern.CASE_INSENSITIVE)));
                i = end;
                continue;
            }
            if (ch == '[') {
                flushLiteral(regex, literal);
                int end = findMatching(token, i, '[', ']');
                regex.append("(?:")
                        .append(compileGroupContent(token.substring(i + 1, end), captures))
                        .append(")?");
                i = end;
                continue;
            }
            if (ch == '(') {
                flushLiteral(regex, literal);
                int end = findMatching(token, i, '(', ')');
                regex.append("(?:")
                        .append(compileAlternatives(token.substring(i + 1, end), captures))
                        .append(')');
                i = end;
                continue;
            }
            literal.append(ch);
        }
        flushLiteral(regex, literal);
        return regex.toString();
    }

    private static @Nullable String compileLeadingAutoTag(String token, List<CaptureSpec> captures) {
        if (token.length() < 2 || token.charAt(0) != ':') {
            return null;
        }
        char next = token.charAt(1);
        if (next == '(' || next == '[') {
            return compileLeadingAutoTaggedGroup(token, captures);
        }
        String tag = deriveLeadingLiteralTag(token.substring(1));
        if (tag == null) {
            return null;
        }
        StringBuilder regex = new StringBuilder("()");
        captures.add(metadataSpec(tag));
        regex.append(compileToken(token.substring(1), captures));
        return regex.toString();
    }

    private static @Nullable String compileLeadingAutoTaggedGroup(String token, List<CaptureSpec> captures) {
        char open = token.charAt(1);
        char close = open == '(' ? ')' : ']';
        int end = findMatching(token, 1, open, close);
        String inner = token.substring(2, end);
        List<String> branches = splitTopLevel(inner, '|');
        if (branches.size() <= 1) {
            return null;
        }

        String suffix = token.substring(end + 1);
        StringBuilder regex = new StringBuilder();
        if (open == '[') {
            regex.append("(?:");
        }
        regex.append("(?:");
        for (int i = 0; i < branches.size(); i++) {
            if (i > 0) {
                regex.append('|');
            }
            String branch = branches.get(i);
            String tag = deriveLeadingLiteralTag(branch);
            if (tag != null) {
                regex.append("()");
                captures.add(metadataSpec(tag));
            }
            regex.append(compileSequence(branch, captures));
        }
        regex.append(")");
        if (open == '[') {
            regex.append(")?");
        }
        if (!suffix.isEmpty()) {
            regex.append(compileToken(suffix, captures));
        }
        return regex.toString();
    }

    private static @Nullable String deriveLeadingLiteralTag(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\\') {
                if (i + 1 >= value.length()) {
                    break;
                }
                literal.append(value.charAt(i + 1));
                i++;
                continue;
            }
            if (Character.isWhitespace(ch)
                    || ch == '['
                    || ch == '('
                    || ch == '%'
                    || ch == '<'
                    || ch == ':'
                    || ch == '¦') {
                break;
            }
            literal.append(ch);
        }
        if (literal.isEmpty()) {
            return null;
        }
        return literal.toString().trim().toLowerCase(Locale.ENGLISH);
    }

    private static void flushLiteral(StringBuilder regex, StringBuilder literal) {
        if (literal.isEmpty()) {
            return;
        }
        regex.append(Pattern.quote(literal.toString()));
        literal.setLength(0);
    }

    private static void addMetadataCapture(
            StringBuilder regex,
            List<CaptureSpec> captures,
            String metadata,
            char kind,
            String token
    ) {
        if (kind == '¦') {
            if (metadata.isEmpty()) {
                throw new IllegalArgumentException("Empty parse mark in token: " + token);
            }
            try {
                int mark = Integer.parseInt(metadata);
                regex.append("()");
                captures.add(CaptureSpec.metadata(null, mark));
                return;
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid parse mark in token: " + token, exception);
            }
        }

        if (metadata.isEmpty()) {
            return;
        }
        regex.append("()");
        captures.add(metadataSpec(metadata));
    }

    private static CaptureSpec metadataSpec(String metadata) {
        int mark = 0;
        try {
            mark = Integer.parseInt(metadata);
        } catch (NumberFormatException ignored) {
        }
        return CaptureSpec.metadata(metadata, mark);
    }

    private static String compileAlternatives(String pattern, List<CaptureSpec> captures) {
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

    private static String compileGroupContent(String pattern, List<CaptureSpec> captures) {
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
            if (ch == '\\' && !inPlaceholder && !inRegex) {
                current.append(ch);
                if (i + 1 < pattern.length()) {
                    current.append(pattern.charAt(i + 1));
                    i++;
                }
                continue;
            }
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
            if (ch == '\\' && !inPlaceholder && !inRegex) {
                current.append(ch);
                if (i + 1 < pattern.length()) {
                    current.append(pattern.charAt(i + 1));
                    i++;
                }
                continue;
            }
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
            if (ch == '\\' && !inPlaceholder && !inRegex) {
                i++;
                continue;
            }
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

    private static int findUnescaped(String value, int startIndex, char target) {
        for (int i = startIndex; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\\') {
                i++;
                continue;
            }
            if (ch == target) {
                return i;
            }
        }
        return -1;
    }

    enum CaptureKind {
        EXPRESSION,
        REGEX,
        METADATA
    }

    record CaptureSpec(
            CaptureKind kind,
            @Nullable TypePatternElement typePattern,
            @Nullable Pattern regexPattern,
            @Nullable String tag,
            int mark
    ) {
        static CaptureSpec expression(TypePatternElement typePattern) {
            return new CaptureSpec(CaptureKind.EXPRESSION, typePattern, null, null, 0);
        }

        static CaptureSpec regex(Pattern regexPattern) {
            return new CaptureSpec(CaptureKind.REGEX, null, regexPattern, null, 0);
        }

        static CaptureSpec metadata(@Nullable String tag, int mark) {
            return new CaptureSpec(CaptureKind.METADATA, null, null, tag, mark);
        }
    }

    record CompiledPattern(
            String source,
            Pattern regex,
            List<CaptureSpec> captures,
            @Nullable PatternElement first,
            int expressionAmount
    ) {
    }

    private record PlaceholderSpec(
            Class<?>[] returnTypes,
            boolean[] pluralities,
            int flagMask,
            int time,
            boolean optional
    ) {
    }

    private record ResolvedPlaceholderType(Class<?> type, boolean plural, String raw) {
    }
}
