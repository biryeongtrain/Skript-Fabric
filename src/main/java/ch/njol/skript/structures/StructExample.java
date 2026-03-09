package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Feature;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public final class StructExample extends Structure implements SimpleExperimentalSyntax {

    public static final Priority PRIORITY = new Priority(550);

    private static final ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.EXAMPLES);
    private static final String[] PATTERNS = {"example"};

    private @Nullable SectionNode source;

    public static void register() {
        Skript.registerStructure(StructExample.class, SyntaxInfo.Structure.NodeType.SECTION, PATTERNS);
    }

    @Override
    public boolean init(
            Literal<?>[] args,
            int matchedPattern,
            ParseResult parseResult,
            @Nullable EntryContainer entryContainer
    ) {
        if (entryContainer == null) {
            return false;
        }
        source = entryContainer.getSource();
        return true;
    }

    @Override
    public ExperimentData getExperimentData() {
        return EXPERIMENT_DATA;
    }

    @Override
    public boolean load() {
        if (source == null) {
            return false;
        }

        ParserInstance parser = getParser();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        parser.setCurrentEvent("example", FunctionEvent.class);
        try {
            ScriptLoader.loadItems(source);
            return true;
        } finally {
            if (previousEventName == null || previousEventClasses.length == 0) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "example";
    }
}
