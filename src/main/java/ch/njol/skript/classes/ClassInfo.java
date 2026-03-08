package ch.njol.skript.classes;

import ch.njol.skript.lang.DefaultExpression;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

public class ClassInfo<T> {

    public interface Parser<T> {
        boolean canParse(ch.njol.skript.lang.ParseContext context);

        @Nullable T parse(String input, ch.njol.skript.lang.ParseContext context);
    }

    private final Class<T> type;
    private final String codeName;
    private final Map<Property<?>, Property.PropertyInfo<?>> properties = new ConcurrentHashMap<>();
    private final Set<String> literalPatterns = new LinkedHashSet<>();
    private final Set<String> after = new LinkedHashSet<>();
    private @Nullable Set<String> before;
    private @Nullable DefaultExpression<T> defaultExpression;
    private @Nullable Parser<T> parser;

    public ClassInfo(Class<T> type) {
        this(type, deriveCodeName(type));
    }

    public ClassInfo(Class<T> type, String codeName) {
        this.type = type;
        if (!isValidCodeName(codeName)) {
            throw new IllegalArgumentException("Code names for classes must be lowercase latin letters and numbers only");
        }
        this.codeName = codeName;
    }

    public Class<T> getC() {
        return type;
    }

    public String getCodeName() {
        return codeName;
    }

    public ClassInfo<T> defaultExpression(DefaultExpression<T> defaultExpression) {
        if (!defaultExpression.isDefault()) {
            throw new IllegalArgumentException(
                    "defaultExpression.isDefault() must return true for the default expression of a class"
            );
        }
        this.defaultExpression = defaultExpression;
        return this;
    }

    public boolean hasProperty(Property<?> property) {
        return properties.containsKey(property);
    }

    @SuppressWarnings("unchecked")
    public <H extends PropertyHandler<?>> @Nullable Property.PropertyInfo<H> getPropertyInfo(Property<H> property) {
        return (Property.PropertyInfo<H>) properties.get(property);
    }

    public <H extends PropertyHandler<?>> void setPropertyInfo(Property<H> property, H handler) {
        properties.put(property, new Property.PropertyInfo<>(property, handler));
    }

    public @Nullable Parser<T> getParser() {
        return parser;
    }

    public @Nullable DefaultExpression<T> getDefaultExpression() {
        return defaultExpression;
    }

    public void setParser(@Nullable Parser<T> parser) {
        this.parser = parser;
    }

    public ClassInfo<T> literalPatterns(String... patterns) {
        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank()) {
                continue;
            }
            literalPatterns.add(normalizeLiteralPattern(pattern));
        }
        return this;
    }

    public Set<String> getLiteralPatterns() {
        return Set.copyOf(literalPatterns);
    }

    public ClassInfo<T> before(String... before) {
        if (this.before == null) {
            this.before = new LinkedHashSet<>();
        }
        for (String codeName : before) {
            if (codeName == null || codeName.isBlank()) {
                continue;
            }
            this.before.add(codeName);
        }
        return this;
    }

    public ClassInfo<T> after(String... after) {
        for (String codeName : after) {
            if (codeName == null || codeName.isBlank()) {
                continue;
            }
            this.after.add(codeName);
        }
        return this;
    }

    public @Nullable Set<String> before() {
        return before;
    }

    public Set<String> after() {
        return after;
    }

    @Override
    public String toString() {
        return codeName;
    }

    public static boolean isValidCodeName(String codeName) {
        return codeName != null && codeName.matches("(?:any-)?[a-z0-9]+");
    }

    private static String deriveCodeName(Class<?> type) {
        String normalized = type.getSimpleName().replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ENGLISH);
        if (!isValidCodeName(normalized)) {
            throw new IllegalArgumentException("Cannot derive a valid code name from " + type.getName());
        }
        return normalized;
    }

    private static String normalizeLiteralPattern(String pattern) {
        return pattern.trim().toLowerCase(Locale.ENGLISH);
    }
}
