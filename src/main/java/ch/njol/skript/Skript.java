package ch.njol.skript;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.Statement;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.SyntaxRegistryService;

public class Skript implements SkriptAddon {

    private static final Logger LOGGER = LoggerFactory.getLogger("skript-fabric");
    private static final Skript INSTANCE = new Skript();
    private static volatile boolean acceptRegistrations = false;
    private static volatile boolean debug = false;

    private final SyntaxRegistryService syntaxRegistryService = new SyntaxRegistryService();
    private final ExperimentRegistry experimentRegistry = new ExperimentRegistry(this);
    private final Map<Class<?>, Object> registries = new ConcurrentHashMap<>();

    public static Skript instance() {
        return INSTANCE;
    }

    public static SkriptAddon getAddonInstance() {
        return INSTANCE;
    }

    public static void checkAcceptRegistrations() {
        // Intentionally permissive in compatibility mode.
    }

    public static boolean isAcceptRegistrations() {
        return acceptRegistrations;
    }

    public static void setAcceptRegistrations(boolean value) {
        acceptRegistrations = value;
    }

    public static void warning(String message) {
        LOGGER.warn(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static boolean debug() {
        return debug;
    }

    public static void setDebug(boolean value) {
        debug = value;
    }

    public static ExperimentRegistry experiments() {
        return INSTANCE.experimentRegistry;
    }

    public static <E extends Statement> void registerStatement(Class<E> statementClass, String... patterns) {
        instance().syntaxRegistry().register(SyntaxRegistry.STATEMENT, statementClass, patterns);
    }

    public static <E extends Condition> void registerCondition(Class<E> conditionClass, String... patterns) {
        instance().syntaxRegistry().register(SyntaxRegistry.CONDITION, conditionClass, patterns);
    }

    public static <E extends Expression<T>, T> void registerExpression(
            Class<E> expressionClass,
            Class<T> returnType,
            String... patterns
    ) {
        instance().syntaxRegistry().registerExpression(expressionClass, returnType, patterns);
    }

    public static <E extends Effect> void registerEffect(Class<E> effectClass, String... patterns) {
        instance().syntaxRegistry().register(SyntaxRegistry.EFFECT, effectClass, patterns);
    }

    public static <E extends Section> void registerSection(Class<E> sectionClass, String... patterns) {
        instance().syntaxRegistry().register(SyntaxRegistry.SECTION, sectionClass, patterns);
    }

    public static <E extends org.skriptlang.skript.lang.structure.Structure> void registerStructure(
            Class<E> structureClass,
            SyntaxInfo.Structure.NodeType nodeType,
            String... patterns
    ) {
        instance().syntaxRegistry().registerStructure(structureClass, nodeType, null, patterns);
    }

    public static <E extends SkriptEvent> void registerEvent(Class<E> eventClass, String... patterns) {
        instance().syntaxRegistry().register(
                SyntaxRegistry.EVENT,
                new SyntaxInfo.Structure<>(
                        eventClass,
                        patterns,
                        eventClass.getName(),
                        null,
                        SyntaxInfo.Structure.NodeType.SECTION
                )
        );
    }

    public SyntaxRegistryService syntaxRegistry() {
        return syntaxRegistryService;
    }

    @Override
    public String name() {
        return "Skript";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T registry(Class<T> type) {
        return (T) registries.computeIfAbsent(type, this::newRegistryInstance);
    }

    private Object newRegistryInstance(Class<?> type) {
        try {
            Constructor<?> ctor = type.getDeclaredConstructor(Skript.class);
            ctor.setAccessible(true);
            return ctor.newInstance(this);
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create registry: " + type.getName(), e);
        }
    }
}
