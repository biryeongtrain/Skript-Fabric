package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Legacy script function parameter model.
 */
public final class Parameter<T> {

    public enum Modifier {
        OPTIONAL,
        KEYED
    }

    public static final Pattern PARAM_PATTERN = Pattern.compile(
            "^\\s*([^:(){}\",]+?)\\s*:\\s*([a-zA-Z ]+?)\\s*(?:\\s*=\\s*(.+))?\\s*$"
    );

    final String name;
    final ClassInfo<T> type;
    final @Nullable Expression<? extends T> def;
    final boolean single;
    final Set<Modifier> modifiers;
    final boolean keyed;

    public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def) {
        this(name, type, single, def, def != null ? Set.of(Modifier.OPTIONAL) : Set.of());
    }

    public Parameter(
            String name,
            ClassInfo<T> type,
            boolean single,
            @Nullable Expression<? extends T> def,
            Modifier... modifiers
    ) {
        this(name, type, single, def, modifiers == null ? Set.of() : Set.of(modifiers));
    }

    private Parameter(
            String name,
            ClassInfo<T> type,
            boolean single,
            @Nullable Expression<? extends T> def,
            Set<Modifier> modifiers
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.def = def;
        this.single = single;
        EnumSet<Modifier> converted = modifiers.isEmpty()
                ? EnumSet.noneOf(Modifier.class)
                : EnumSet.copyOf(modifiers);
        if (def != null) {
            converted.add(Modifier.OPTIONAL);
        }
        if (!single) {
            converted.add(Modifier.KEYED);
        }
        this.modifiers = Set.copyOf(converted);
        this.keyed = this.modifiers.contains(Modifier.KEYED);
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) (single ? type.getC() : type.getC().arrayType());
        return clazz;
    }

    public boolean isSingle() {
        return single;
    }

    public boolean hasModifier(Modifier modifier) {
        return modifiers.contains(modifier);
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public boolean isOptional() {
        return modifiers.contains(Modifier.OPTIONAL);
    }

    public ClassInfo<T> getType() {
        return type;
    }

    public @Nullable Expression<? extends T> getDefaultExpression() {
        return def;
    }

    public boolean isSingleValue() {
        return single;
    }

    public static <T> @Nullable Parameter<T> newInstance(
            String name,
            ClassInfo<T> type,
            boolean single,
            @Nullable String def
    ) {
        if (!Variable.isValidVariableName(name, true, false)) {
            Skript.error("A parameter's name must be a valid variable name.");
            return null;
        }
        Expression<? extends T> defaultExpr = null;
        if (def != null && !def.isBlank()) {
            RetainingLogHandler log = SkriptLogger.startRetainingLog();
            try {
                @SuppressWarnings("unchecked")
                Expression<? extends T> parsed = (Expression<? extends T>) new SkriptParser(
                        def,
                        SkriptParser.ALL_FLAGS,
                        ParseContext.DEFAULT
                ).parseExpression(new Class[]{type.getC()});
                defaultExpr = parsed;
                if (defaultExpr == null || LiteralUtils.hasUnparsedLiteral(defaultExpr)) {
                    log.printErrors("Can't understand this expression: " + def);
                    return null;
                }
                log.printLog();
            } finally {
                log.stop();
            }
        }

        List<Modifier> modifiers = new ArrayList<>();
        if (defaultExpr != null) {
            modifiers.add(Modifier.OPTIONAL);
        }
        if (!single) {
            modifiers.add(Modifier.KEYED);
        }
        return new Parameter<>(name, type, single, defaultExpr, modifiers.toArray(Modifier[]::new));
    }

    public static @Nullable List<Parameter<?>> parse(String args) {
        if (args == null) {
            return null;
        }
        List<Parameter<?>> params = new ArrayList<>();
        boolean caseInsensitive = Variables.caseInsensitiveVariables;
        int start = 0;
        while (start <= args.length()) {
            int index = nextArgumentBoundary(args, start);
            if (index == -1) {
                Skript.error("Invalid text/variables/parentheses in the arguments of this function");
                return null;
            }
            if (args.isEmpty()) {
                break;
            }
            String arg = args.substring(start, index);
            Matcher matcher = PARAM_PATTERN.matcher(arg);
            if (!matcher.matches()) {
                Skript.error("Invalid argument definition near '" + arg.trim()
                        + "'. Expected 'name: type' or 'name: type = default value'.");
                return null;
            }
            String paramName = matcher.group(1).trim();
            String normalizedParamName = caseInsensitive
                    ? paramName.toLowerCase(Locale.ENGLISH)
                    : paramName;
            for (Parameter<?> parameter : params) {
                String existingName = caseInsensitive
                        ? parameter.name.toLowerCase(Locale.ENGLISH)
                        : parameter.name;
                if (existingName.equals(normalizedParamName)) {
                    Skript.error("Each argument's name must be unique, but the name '"
                            + paramName + "' occurs at least twice.");
                    return null;
                }
            }
            String typeName = matcher.group(2).trim();
            ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(typeName);
            if (classInfo == null) {
                classInfo = guessClassInfo(typeName);
            }
            if (classInfo == null) {
                Skript.error("Cannot recognise the type '" + matcher.group(2).trim() + "'");
                return null;
            }

            Parameter<?> parameter = newInstance(
                    paramName,
                    classInfo,
                    !isPluralUserInput(typeName, classInfo),
                    matcher.group(3)
            );
            if (parameter == null) {
                return null;
            }
            params.add(parameter);

            if (index == args.length()) {
                break;
            }
            start = index + 1;
        }
        return params;
    }

    public Object @Nullable [] evaluate(@Nullable Expression<?> expression, SkriptEvent event) {
        if (expression == null) {
            return null;
        }
        Object[] values;
        if (expression instanceof Literal<?> literal) {
            values = literal.getArray(event);
        } else {
            values = expression.getArray(event);
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = Classes.clone(values[i]);
        }
        if (!hasModifier(Modifier.KEYED)) {
            return values;
        }
        String[] keys = KeyProviderExpression.areKeysRecommended(expression)
                ? ((KeyProviderExpression<?>) expression).getArrayKeys(event)
                : null;
        return KeyedValue.zip(values, keys);
    }

    private static @Nullable ClassInfo<?> guessClassInfo(String typeName) {
        String normalized = typeName.toLowerCase(Locale.ENGLISH);
        return switch (normalized) {
            case "text", "string" -> Classes.getSuperClassInfo(String.class);
            case "number", "integer", "int" -> Classes.getSuperClassInfo(Integer.class);
            case "decimal", "double" -> Classes.getSuperClassInfo(Double.class);
            case "boolean", "bool" -> Classes.getSuperClassInfo(Boolean.class);
            case "object", "value", "any" -> Classes.getSuperClassInfo(Object.class);
            default -> null;
        };
    }

    private static boolean isPluralUserInput(String typeName, ClassInfo<?> classInfo) {
        String normalized = typeName.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.length() <= 1) {
            return false;
        }
        String singularCandidate = null;
        if (normalized.endsWith("ies")) {
            singularCandidate = normalized.substring(0, normalized.length() - 3) + "y";
        } else if (normalized.endsWith("es")) {
            singularCandidate = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("s")) {
            singularCandidate = normalized.substring(0, normalized.length() - 1);
        }
        if (singularCandidate == null || singularCandidate.equals(normalized)) {
            return false;
        }
        ClassInfo<?> singularInfo = Classes.getClassInfoFromUserInput(singularCandidate);
        return singularInfo != null && singularInfo.getC() == classInfo.getC();
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Parameter<?> parameter)) {
            return false;
        }
        return single == parameter.single
                && keyed == parameter.keyed
                && name.equals(parameter.name)
                && type.getC().equals(parameter.type.getC());
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.getC().hashCode();
        result = 31 * result + (single ? 1 : 0);
        result = 31 * result + (keyed ? 1 : 0);
        return result;
    }
}
