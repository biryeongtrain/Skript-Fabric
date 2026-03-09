package ch.njol.skript.lang.parser;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.variables.HintManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

public final class ParserInstance {

    private static final ThreadLocal<ParserInstance> LOCAL = ThreadLocal.withInitial(ParserInstance::new);
    private static final Map<Class<? extends Data>, Function<ParserInstance, ? extends Data>> DATA_FACTORIES = new ConcurrentHashMap<>();

    public abstract static class Data {

        private final ParserInstance parserInstance;

        protected Data(ParserInstance parserInstance) {
            this.parserInstance = parserInstance;
        }

        public ParserInstance parser() {
            return parserInstance;
        }

        public void onCurrentEventsChange(@Nullable Class<?>[] currentEvents) {
        }
    }

    private final Map<Class<? extends Data>, Data> data = new ConcurrentHashMap<>();
    private final ParsingStack parsingStack = new ParsingStack();
    private @Nullable Script currentScript;
    private @Nullable Node node;
    private @Nullable String currentEventName;
    private Class<?>[] currentEventClasses = new Class<?>[0];
    private List<TriggerSection> currentSections = new ArrayList<>();
    private HintManager hintManager = new HintManager(true);

    public static ParserInstance get() {
        return LOCAL.get();
    }

    public static <T> T withInstance(ParserInstance parser, Supplier<T> action) {
        ParserInstance previous = LOCAL.get();
        LOCAL.set(parser);
        try {
            return action.get();
        } finally {
            LOCAL.set(previous);
        }
    }

    public static <T extends Data> void registerData(Class<T> type, Function<ParserInstance, T> factory) {
        DATA_FACTORIES.put(type, factory);
    }

    public static boolean isRegistered(Class<? extends Data> type) {
        return DATA_FACTORIES.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends Data> T getData(Class<T> type) {
        Data value = data.computeIfAbsent(type, key -> {
            Function<ParserInstance, ? extends Data> factory = DATA_FACTORIES.get(type);
            if (factory != null) {
                return factory.apply(this);
            }
            try {
                return type.getDeclaredConstructor(ParserInstance.class).newInstance(this);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No parser data factory for " + type.getName(), e);
            }
        });
        return (T) value;
    }

    public boolean isActive() {
        return currentScript != null;
    }

    public @Nullable Script getCurrentScript() {
        return currentScript;
    }

    public void setCurrentScript(@Nullable Script currentScript) {
        if (this.currentScript == currentScript) {
            return;
        }
        this.node = null;
        this.currentEventName = null;
        this.currentEventClasses = new Class<?>[0];
        this.currentSections = new ArrayList<>();
        this.data.clear();
        this.currentScript = currentScript;
        this.hintManager = new HintManager(currentScript != null);
    }

    public @Nullable Node getNode() {
        return node;
    }

    public void setNode(@Nullable Node node) {
        this.node = node == null || node.getParent() == null ? null : node;
    }

    public void setCurrentEvent(String eventName, Class<?>... eventClasses) {
        this.currentEventName = eventName;
        updateCurrentEventClasses(eventClasses);
    }

    public void deleteCurrentEvent() {
        this.currentEventName = null;
        updateCurrentEventClasses(null);
    }

    public boolean isCurrentEvent(Class<?>... eventClasses) {
        if (currentEventClasses.length == 0 || eventClasses == null || eventClasses.length == 0) {
            return false;
        }
        for (Class<?> expected : eventClasses) {
            for (Class<?> current : currentEventClasses) {
                if (expected.isAssignableFrom(current)) {
                    return true;
                }
            }
        }
        return false;
    }

    public @Nullable String getCurrentEventName() {
        return currentEventName;
    }

    public Class<?>[] getCurrentEventClasses() {
        return currentEventClasses;
    }

    private void updateCurrentEventClasses(@Nullable Class<?>[] eventClasses) {
        this.currentEventClasses = eventClasses == null ? new Class<?>[0] : eventClasses;
        for (Data dataInstance : getRegisteredDataInstances()) {
            dataInstance.onCurrentEventsChange(eventClasses);
        }
    }

    public void setCurrentSections(List<TriggerSection> currentSections) {
        this.currentSections = currentSections == null ? new ArrayList<>() : currentSections;
    }

    public List<TriggerSection> getCurrentSections() {
        return currentSections;
    }

    public HintManager getHintManager() {
        return hintManager;
    }

    private List<Data> getRegisteredDataInstances() {
        List<Data> instances = new ArrayList<>(DATA_FACTORIES.size());
        for (Class<? extends Data> type : DATA_FACTORIES.keySet()) {
            instances.add(getData(type));
        }
        return instances;
    }

    /**
     * Gets the current parsing stack.
     * Modifying it directly is possible but not recommended.
     */
    public ParsingStack getParsingStack() {
        return parsingStack;
    }
}
