package ch.njol.skript.lang.parser;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.variables.HintManager;
import ch.njol.util.Kleenean;
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

        protected final ParserInstance getParser() {
            return parserInstance;
        }

        public ParserInstance parser() {
            return getParser();
        }

        @Deprecated(since = "2.11.0", forRemoval = true)
        public void onCurrentScriptChange(@Nullable Config currentScript) {
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
    private Kleenean hasDelayBefore = Kleenean.FALSE;
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
        this.hasDelayBefore = Kleenean.FALSE;
        this.currentScript = currentScript;
        List<Data> dataInstances = getRegisteredDataInstances();
        Config currentConfig = currentScript != null ? currentScript.getConfig() : null;
        for (Data dataInstance : dataInstances) {
            dataInstance.onCurrentScriptChange(currentConfig);
        }
        if (currentScript == null) {
            this.data.clear();
        }
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
        this.hasDelayBefore = Kleenean.FALSE;
    }

    public void deleteCurrentEvent() {
        this.currentEventName = null;
        updateCurrentEventClasses(null);
        this.hasDelayBefore = Kleenean.FALSE;
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

    public <T extends TriggerSection> @Nullable T getCurrentSection(Class<T> sectionClass) {
        for (int i = currentSections.size() - 1; i >= 0; i--) {
            TriggerSection triggerSection = currentSections.get(i);
            if (sectionClass.isInstance(triggerSection)) {
                return sectionClass.cast(triggerSection);
            }
        }
        return null;
    }

    public <T extends TriggerSection> List<T> getCurrentSections(Class<T> sectionClass) {
        List<T> sections = new ArrayList<>();
        for (TriggerSection triggerSection : currentSections) {
            if (sectionClass.isInstance(triggerSection)) {
                sections.add(sectionClass.cast(triggerSection));
            }
        }
        return sections;
    }

    public List<TriggerSection> getSectionsUntil(TriggerSection section) {
        int index = currentSections.indexOf(section);
        if (index < 0 || index + 1 >= currentSections.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(currentSections.subList(index + 1, currentSections.size()));
    }

    public List<TriggerSection> getSections(int levels) {
        if (levels < 1) {
            throw new IllegalArgumentException("Depth must be at least 1");
        }
        return new ArrayList<>(currentSections.subList(Math.max(currentSections.size() - levels, 0), currentSections.size()));
    }

    public List<TriggerSection> getSections(int levels, Class<? extends TriggerSection> type) {
        if (levels < 1) {
            throw new IllegalArgumentException("Depth must be at least 1");
        }
        List<? extends TriggerSection> sections = getCurrentSections(type);
        if (sections.isEmpty()) {
            return new ArrayList<>();
        }
        TriggerSection section = sections.get(Math.max(sections.size() - levels, 0));
        return new ArrayList<>(currentSections.subList(currentSections.indexOf(section), currentSections.size()));
    }

    public boolean isCurrentSection(Class<? extends TriggerSection> sectionClass) {
        for (TriggerSection triggerSection : currentSections) {
            if (sectionClass.isInstance(triggerSection)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public final boolean isCurrentSection(Class<? extends TriggerSection>... sectionClasses) {
        for (Class<? extends TriggerSection> sectionClass : sectionClasses) {
            if (isCurrentSection(sectionClass)) {
                return true;
            }
        }
        return false;
    }

    public HintManager getHintManager() {
        return hintManager;
    }

    public void setHasDelayBefore(Kleenean hasDelayBefore) {
        this.hasDelayBefore = hasDelayBefore;
    }

    public Kleenean getHasDelayBefore() {
        return hasDelayBefore;
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
