package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public final class StructEvent extends Structure {

    private static final String[] PATTERNS = {
            "[on] [:uncancelled|:cancelled|any:(any|all)] <.+?> [priority:with priority (:(lowest|low|normal|high|highest|monitor))]"
    };

    static {
        ParserInstance.registerData(EventData.class, EventData::new);
    }

    private @Nullable SkriptEvent event;

    public static void register() {
        Skript.registerStructure(StructEvent.class, SyntaxInfo.Structure.NodeType.SECTION, PATTERNS);
    }

    @Override
    public boolean init(
            Literal<?>[] args,
            int matchedPattern,
            ParseResult parseResult,
            @Nullable EntryContainer entryContainer
    ) {
        if (entryContainer == null || parseResult.regexes.isEmpty()) {
            return false;
        }

        String expr = parseResult.regexes.getFirst().group();
        EventData data = getParser().getData(EventData.class);
        data.clear();

        if (parseResult.hasTag("uncancelled")) {
            data.behavior = ListeningBehavior.UNCANCELLED;
        } else if (parseResult.hasTag("cancelled")) {
            data.behavior = ListeningBehavior.CANCELLED;
        } else if (parseResult.hasTag("any")) {
            data.behavior = ListeningBehavior.ANY;
        }

        if (parseResult.hasTag("priority")) {
            String lastTag = parseResult.tags.getLast();
            data.priority = SkriptEvent.EventPriority.valueOf(lastTag.toUpperCase(Locale.ENGLISH));
        }

        try {
            event = parseEvent(expr, entryContainer.getSource());
            return event != null;
        } finally {
            data.clear();
        }
    }

    private static @Nullable SkriptEvent parseEvent(String expr, ch.njol.skript.config.SectionNode source) {
        SkriptEvent parsed = SkriptEvent.parse(expr, source, null);
        if (parsed != null || expr.regionMatches(true, 0, "on ", 0, 3)) {
            return parsed;
        }
        return SkriptEvent.parse("on " + expr, source, null);
    }

    @Override
    public boolean preLoad() {
        return event != null && event.preLoad();
    }

    @Override
    public boolean load() {
        return event != null && event.load();
    }

    @Override
    public boolean postLoad() {
        return event != null && event.postLoad();
    }

    @Override
    public void unload() {
        if (event != null) {
            event.unload();
        }
    }

    @Override
    public void postUnload() {
        if (event != null) {
            event.postUnload();
        }
    }

    @Override
    public Priority getPriority() {
        return event != null ? event.getPriority() : DEFAULT_PRIORITY;
    }

    public @Nullable SkriptEvent getSkriptEvent() {
        return event;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return this.event != null ? this.event.toString(event, debug) : "event";
    }

    public static final class EventData extends ParserInstance.Data {

        private @Nullable SkriptEvent.EventPriority priority;
        private @Nullable ListeningBehavior behavior;

        public EventData(ParserInstance parserInstance) {
            super(parserInstance);
        }

        public @Nullable SkriptEvent.EventPriority getPriority() {
            return priority;
        }

        public @Nullable ListeningBehavior getListenerBehavior() {
            return behavior;
        }

        public void clear() {
            priority = null;
            behavior = null;
        }
    }
}
