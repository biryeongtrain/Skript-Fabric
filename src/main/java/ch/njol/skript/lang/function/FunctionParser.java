package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility parser for legacy function signatures.
 */
public final class FunctionParser {

    private static final Pattern SCRIPT_PARAMETER_PATTERN = Pattern.compile(
            "^\\s*(?<name>[^:(){}\",]+?)\\s*:\\s*(?<type>[a-zA-Z ]+?)\\s*(?:\\s*=\\s*(?<def>.+))?\\s*$"
    );

    private FunctionParser() {
    }

    public static @Nullable Signature<?> parse(
            String script,
            String name,
            String args,
            @Nullable String returns,
            boolean local
    ) {
        Map<String, Parameter<?>> parameters = parseParameters(args);
        if (parameters == null) {
            return null;
        }

        Class<?> returnType = null;
        if (returns != null) {
            ResolvedType resolvedReturnType = resolveType(returns);
            if (resolvedReturnType == null) {
                Skript.error("Cannot recognise the type '" + returns + "'");
                return null;
            }
            returnType = resolvedReturnType.type();
        }

        return new Signature<>(script, name, parameters, returnType, local);
    }

    private static @Nullable Map<String, Parameter<?>> parseParameters(String args) {
        Map<String, Parameter<?>> parameters = new LinkedHashMap<>();
        if (args.isEmpty()) {
            return parameters;
        }

        boolean caseInsensitive = Variables.caseInsensitiveVariables;
        int start = 0;
        while (start <= args.length()) {
            int end = nextArgumentBoundary(args, start);
            if (end == -1) {
                Skript.error("Invalid text/variables/parentheses in the arguments of this function");
                return null;
            }
            if (end != args.length() && args.charAt(end) != ',') {
                start = end;
                continue;
            }

            String argument = args.substring(start, end);
            Matcher matcher = SCRIPT_PARAMETER_PATTERN.matcher(argument);
            if (!matcher.matches()) {
                Skript.error("The " + fancyOrderNumber(parameters.size() + 1)
                        + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'.");
                return null;
            }

            String parameterName = matcher.group("name");
            String comparisonName = caseInsensitive
                    ? parameterName.toLowerCase(Locale.ENGLISH)
                    : parameterName;
            for (String existingName : parameters.keySet()) {
                String candidateName = caseInsensitive
                        ? existingName.toLowerCase(Locale.ENGLISH)
                        : existingName;
                if (candidateName.equals(comparisonName)) {
                    Skript.error("Each argument's name must be unique, but the name '"
                            + parameterName + "' occurs at least twice.");
                    return null;
                }
            }

            String rawType = matcher.group("type");
            ResolvedType resolvedType = resolveType(rawType);
            if (resolvedType == null) {
                Skript.error("Cannot recognise the type '" + rawType + "'");
                return null;
            }

            String variableName = normalizeParameterVariableName(parameterName, resolvedType.plural());
            Parameter<?> parameter = Parameter.newInstance(
                    variableName,
                    resolvedType.classInfo(),
                    !resolvedType.plural(),
                    matcher.group("def")
            );
            if (parameter == null) {
                return null;
            }
            parameters.put(variableName, parameter);

            if (end == args.length()) {
                break;
            }
            start = end + 1;
        }
        return parameters;
    }

    private static int nextArgumentBoundary(String args, int start) {
        boolean inQuotes = false;
        int roundDepth = 0;
        int squareDepth = 0;
        int curlyDepth = 0;
        for (int index = start; index < args.length(); index++) {
            char character = args.charAt(index);
            if (character == '"' && (index == 0 || args.charAt(index - 1) != '\\')) {
                inQuotes = !inQuotes;
                continue;
            }
            if (inQuotes) {
                continue;
            }
            switch (character) {
                case '(' -> roundDepth++;
                case ')' -> {
                    if (roundDepth == 0) {
                        return -1;
                    }
                    roundDepth--;
                }
                case '[' -> squareDepth++;
                case ']' -> {
                    if (squareDepth == 0) {
                        return -1;
                    }
                    squareDepth--;
                }
                case '{' -> curlyDepth++;
                case '}' -> {
                    if (curlyDepth == 0) {
                        return -1;
                    }
                    curlyDepth--;
                }
                case ',' -> {
                    if (roundDepth == 0 && squareDepth == 0 && curlyDepth == 0) {
                        return index;
                    }
                }
                default -> {
                }
            }
        }
        return inQuotes || roundDepth != 0 || squareDepth != 0 || curlyDepth != 0
                ? -1
                : args.length();
    }

    private static @Nullable ResolvedType resolveType(String rawType) {
        ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(rawType);
        if (classInfo == null) {
            classInfo = guessClassInfo(rawType);
        }
        if (classInfo != null) {
            return new ResolvedType(classInfo, false);
        }

        String singular = singularizeUserInput(rawType);
        if (singular == null) {
            return null;
        }

        ClassInfo<?> singularInfo = Classes.getClassInfoFromUserInput(singular);
        if (singularInfo == null) {
            singularInfo = guessClassInfo(singular);
        }
        if (singularInfo == null) {
            return null;
        }
        return new ResolvedType(singularInfo, true);
    }

    private static @Nullable String singularizeUserInput(String rawType) {
        String normalized = rawType.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.length() <= 1) {
            return null;
        }
        if (normalized.endsWith("ies")) {
            return normalized.substring(0, normalized.length() - 3) + "y";
        }
        if (normalized.endsWith("es")) {
            return normalized.substring(0, normalized.length() - 2);
        }
        if (normalized.endsWith("s")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return null;
    }

    private static @Nullable ClassInfo<?> guessClassInfo(String typeName) {
        String normalized = typeName.trim().toLowerCase(Locale.ENGLISH);
        return switch (normalized) {
            case "text", "string" -> Classes.getSuperClassInfo(String.class);
            case "number", "integer", "int" -> Classes.getSuperClassInfo(Integer.class);
            case "decimal", "double" -> Classes.getSuperClassInfo(Double.class);
            case "boolean", "bool" -> Classes.getSuperClassInfo(Boolean.class);
            case "object", "value", "any" -> Classes.getSuperClassInfo(Object.class);
            default -> null;
        };
    }

    private static String normalizeParameterVariableName(String parameterName, boolean pluralType) {
        if (!parameterName.endsWith("*")) {
            return parameterName;
        }
        String baseName = parameterName.substring(0, parameterName.length() - 1);
        return pluralType ? baseName + "::*" : baseName + "::1";
    }

    private static String fancyOrderNumber(int value) {
        int mod100 = value % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return value + "th";
        }
        return switch (value % 10) {
            case 1 -> value + "st";
            case 2 -> value + "nd";
            case 3 -> value + "rd";
            default -> value + "th";
        };
    }

    private record ResolvedType(ClassInfo<?> classInfo, boolean plural) {
        private Class<?> type() {
            return plural ? classInfo.getC().arrayType() : classInfo.getC();
        }
    }
}
