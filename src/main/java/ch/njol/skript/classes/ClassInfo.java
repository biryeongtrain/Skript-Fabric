package ch.njol.skript.classes;

import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.localization.Noun;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

public class ClassInfo<T> {

    public static final String NO_DOC = new String();

    public interface Parser<T> {
        boolean canParse(ch.njol.skript.lang.ParseContext context);

        @Nullable T parse(String input, ch.njol.skript.lang.ParseContext context);
    }

    private final Class<T> type;
    private final String codeName;
    private final Noun name;
    private final Map<Property<?>, Property.PropertyInfo<?>> properties = new ConcurrentHashMap<>();
    private final Set<String> literalPatterns = new LinkedHashSet<>();
    private final Set<String> after = new LinkedHashSet<>();
    private @Nullable Set<String> before;
    @Nullable Pattern[] userInputPatterns;
    private @Nullable DefaultExpression<T> defaultExpression;
    private @Nullable Parser<T> parser;
    private @Nullable Cloner<T> cloner;
    private @Nullable Changer changer;
    private @Nullable Supplier<Iterator<T>> supplier;
    private @Nullable String docName;
    private @Nullable String[] description;
    private @Nullable String[] usage;
    private @Nullable String[] examples;
    private @Nullable String since;
    private @Nullable String[] requiredPlugins;
    private @Nullable String documentationId;

    public ClassInfo(Class<T> type) {
        this(type, deriveCodeName(type));
    }

    public ClassInfo(Class<T> type, String codeName) {
        this.type = type;
        if (!isValidCodeName(codeName)) {
            throw new IllegalArgumentException("Code names for classes must be lowercase latin letters and numbers only");
        }
        this.codeName = codeName;
        this.name = new Noun("types." + codeName);
    }

    public Class<T> getC() {
        return type;
    }

    public String getCodeName() {
        return codeName;
    }

    public Noun getName() {
        return name;
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

    @SuppressWarnings("unchecked")
    public ClassInfo<T> parser(Parser<? extends T> parser) {
        this.parser = (Parser<T>) parser;
        return this;
    }

    public ClassInfo<T> cloner(Cloner<T> cloner) {
        this.cloner = cloner;
        return this;
    }

    public @Nullable Cloner<T> getCloner() {
        return cloner;
    }

    public @Nullable DefaultExpression<T> getDefaultExpression() {
        return defaultExpression;
    }

    public ClassInfo<T> changer(Changer changer) {
        this.changer = changer;
        return this;
    }

    public @Nullable Changer getChanger() {
        return changer;
    }

    public ClassInfo<T> supplier(Supplier<Iterator<T>> supplier) {
        this.supplier = supplier;
        return this;
    }

    public ClassInfo<T> supplier(T... values) {
        return supplier(() -> java.util.Arrays.asList(values).iterator());
    }

    public @Nullable Supplier<Iterator<T>> getSupplier() {
        if (supplier == null && type.isEnum()) {
            supplier = () -> java.util.Arrays.asList(type.getEnumConstants()).iterator();
        }
        return supplier;
    }

    public ClassInfo<T> name(String name) {
        this.docName = name;
        return this;
    }

    public ClassInfo<T> description(String... description) {
        this.description = description;
        return this;
    }

    public ClassInfo<T> usage(String... usage) {
        this.usage = usage;
        return this;
    }

    public ClassInfo<T> examples(String... examples) {
        this.examples = examples;
        return this;
    }

    public ClassInfo<T> since(String since) {
        this.since = since;
        return this;
    }

    public ClassInfo<T> requiredPlugins(String... requiredPlugins) {
        this.requiredPlugins = requiredPlugins;
        return this;
    }

    public ClassInfo<T> documentationId(String documentationId) {
        this.documentationId = documentationId;
        return this;
    }

    public @Nullable String getDocName() {
        return docName;
    }

    public @Nullable String[] getDescription() {
        return description == null ? null : description.clone();
    }

    public @Nullable String[] getUsage() {
        return usage == null ? null : usage.clone();
    }

    public @Nullable String[] getExamples() {
        return examples == null ? null : examples.clone();
    }

    public @Nullable String getSince() {
        return since;
    }

    public @Nullable String[] getRequiredPlugins() {
        return requiredPlugins == null ? null : requiredPlugins.clone();
    }

    public @Nullable String getDocumentationID() {
        return documentationId;
    }

    public boolean hasDocs() {
        return docName != null && !NO_DOC.equals(docName);
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

    public ClassInfo<T> user(String... userInputPatterns) throws PatternSyntaxException {
        Pattern[] compiled = new Pattern[userInputPatterns.length];
        for (int i = 0; i < userInputPatterns.length; i++) {
            compiled[i] = Pattern.compile(userInputPatterns[i]);
        }
        this.userInputPatterns = compiled;
        return this;
    }

    public Pattern @Nullable [] getUserInputPatterns() {
        return userInputPatterns == null ? null : userInputPatterns.clone();
    }

    public boolean matchesUserInput(String input) {
        if (userInputPatterns == null) {
            return false;
        }
        for (Pattern pattern : userInputPatterns) {
            if (pattern.matcher(input).matches()) {
                return true;
            }
        }
        return false;
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

    public T clone(T value) {
        return cloner == null ? value : cloner.clone(value);
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
