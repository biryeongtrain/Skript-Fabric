package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.coll.iterator.ConsumingIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.StructureInfo;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.Structure.StructureData;
import org.skriptlang.skript.registration.SyntaxInfo;

@SuppressWarnings("NotNullFieldNotInitialized")
public abstract class SkriptEvent extends Structure {

    public static final Priority PRIORITY = new Priority(600);

    private String expr = "";
    private SectionNode source;
    protected @Nullable EventPriority eventPriority;
    protected @Nullable ListeningBehavior listeningBehavior;
    protected boolean supportsListeningBehavior;
    private @Nullable SkriptEventInfo<?> skriptEventInfo;

    protected Trigger trigger;

    @Override
    public final boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
        this.expr = parseResult.expr;

        SyntaxElementInfo<? extends Structure> syntaxElementInfo = getParser().getData(StructureData.class).getStructureInfo();
        if (syntaxElementInfo instanceof SkriptEventInfo<?> info) {
            this.skriptEventInfo = info;
        }

        if (entryContainer == null) {
            Skript.error("SkriptEvent requires a section-based entry container.");
            return false;
        }
        this.source = entryContainer.getSource();

        if (listeningBehavior == null) {
            listeningBehavior = ListeningBehavior.ANY;
        }
        if (eventPriority == null) {
            eventPriority = EventPriority.NORMAL;
        }

        return init(args, matchedPattern, parseResult);
    }

    public abstract boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult);

    @Override
    public boolean load() {
        if (!shouldLoadEvent()) {
            return false;
        }

        if (Skript.debug()) {
            Skript.debug(expr + " (" + this + "):");
        }

        try {
            getParser().setCurrentEvent(eventName().toLowerCase(Locale.ENGLISH), getEventClasses());

            List<TriggerItem> items = ScriptLoader.loadItems(source);
            Script script = getParser().getCurrentScript();

            trigger = new Trigger(script, expr, this, items);
            int lineNumber = source.getLine();
            trigger.setLineNumber(lineNumber);
            String scriptLabel = script != null ? script.toString() : "unknown script";
            trigger.setDebugLabel(scriptLabel + ": line " + lineNumber);
        } finally {
            getParser().deleteCurrentEvent();
        }

        return true;
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    public abstract boolean check(org.skriptlang.skript.lang.event.SkriptEvent event);

    public @Nullable Trigger getTrigger() {
        return trigger;
    }

    public boolean shouldLoadEvent() {
        return true;
    }

    public Class<?>[] getEventClasses() {
        if (skriptEventInfo != null) {
            return skriptEventInfo.events;
        }
        return new Class<?>[]{Object.class};
    }

    public EventPriority getEventPriority() {
        return eventPriority != null ? eventPriority : EventPriority.NORMAL;
    }

    public boolean isEventPrioritySupported() {
        return true;
    }

    public ListeningBehavior getListeningBehavior() {
        if (listeningBehavior != null) {
            return listeningBehavior;
        }
        if (skriptEventInfo != null) {
            return skriptEventInfo.getListeningBehavior();
        }
        return ListeningBehavior.ANY;
    }

    public boolean isListeningBehaviorSupported() {
        return supportsListeningBehavior;
    }

    public boolean canExecuteAsynchronously() {
        return false;
    }

    public static String fixPattern(String pattern) {
        return pattern;
    }

    public static @Nullable SkriptEvent parse(String expr, SectionNode sectionNode, @Nullable String defaultError) {
        ParserInstance.get().getData(StructureData.class).node = sectionNode;

        try (ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler()) {
            SkriptEvent event = parseEventExpression(expr, defaultError);
            if (event == null && expr.regionMatches(true, 0, "on ", 0, 3)) {
                event = parseEventExpression(expr.substring(3).trim(), defaultError);
            }
            if (event != null) {
                parseLogHandler.printLog();
                return event;
            }
            parseLogHandler.printError();
            return null;
        }
    }

    private static Iterator<?> eventSyntaxIterator() {
        var iterator = Skript.instance().syntaxRegistry().syntaxes(org.skriptlang.skript.registration.SyntaxRegistry.EVENT).iterator();
        return new ConsumingIterator<>(iterator, info -> {
            StructureData structureData = ParserInstance.get().getData(StructureData.class);
            if (info instanceof SyntaxInfo.Structure<?> structureInfo) {
                @SuppressWarnings("unchecked")
                SyntaxInfo.Structure<? extends Structure> typed = (SyntaxInfo.Structure<? extends Structure>) structureInfo;
                structureData.structureInfo = new StructureInfo<>(typed);
            } else {
                structureData.structureInfo = null;
            }
        });
    }

    private static @Nullable SkriptEvent parseEventExpression(String expr, @Nullable String defaultError) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        SkriptEvent event = (SkriptEvent) SkriptParser.parseModern(
                expr,
                (Iterator) eventSyntaxIterator(),
                ParseContext.EVENT,
                defaultError
        );
        return event;
    }

    private String eventName() {
        if (skriptEventInfo != null) {
            return skriptEventInfo.getName();
        }
        return "event";
    }

    public enum EventPriority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        MONITOR
    }

    public enum ListeningBehavior {
        UNCANCELLED,
        CANCELLED,
        ANY;

        public boolean matches(boolean cancelled) {
            return switch (this) {
                case CANCELLED -> cancelled;
                case UNCANCELLED -> !cancelled;
                case ANY -> true;
            };
        }
    }
}
