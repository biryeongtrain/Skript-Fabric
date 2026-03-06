package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
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
            T parsed = Classes.parse(def, type.getC(), ch.njol.skript.lang.ParseContext.DEFAULT);
            if (parsed == null) {
                Skript.error("Can't understand this expression: " + def);
                return null;
            }
            defaultExpr = new SimpleLiteral<>(parsed, true);
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
        if (args.isBlank()) {
            return List.of();
        }
        List<Parameter<?>> params = new ArrayList<>();
        String[] split = args.split(",");
        for (int index = 0; index < split.length; index++) {
            String arg = split[index].trim();
            Matcher matcher = PARAM_PATTERN.matcher(arg);
            if (!matcher.matches()) {
                Skript.error("Invalid argument definition near '" + arg + "'. Expected 'name: type' or 'name: type = default value'.");
                return null;
            }
            String paramName = matcher.group(1).trim();
            String typeName = matcher.group(2).trim();
            String defaultValue = matcher.group(3);

            boolean single = true;
            if (typeName.toLowerCase(Locale.ENGLISH).endsWith("s")) {
                single = false;
                typeName = typeName.substring(0, typeName.length() - 1).trim();
            }

            ClassInfo<?> classInfo = guessClassInfo(typeName);
            if (classInfo == null) {
                Skript.error("Cannot recognise the type '" + matcher.group(2) + "'");
                return null;
            }

            Parameter<?> parameter = newInstance(paramName, classInfo, single, defaultValue);
            if (parameter == null) {
                return null;
            }
            params.add(parameter);
        }
        return params;
    }

    public Object @Nullable [] evaluate(@Nullable Expression<?> expression, SkriptEvent event) {
        if (expression == null) {
            return null;
        }
        if (expression instanceof Literal<?> literal) {
            return literal.getArray(event);
        }
        return expression.getArray(event);
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
