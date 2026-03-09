package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SectionExitHandler;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.sections.SecConditional;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffExit extends Effect {

    @SuppressWarnings("unchecked")
    private static final Class<? extends TriggerSection>[] TYPES =
            new Class[]{TriggerSection.class, LoopSection.class, SecConditional.class};
    private static final String[] NAMES = {"sections", "loops", "conditionals"};

    private static boolean registered;

    private int type;
    private int breakLevels;
    private @Nullable TriggerSection outerSection;
    private @UnknownNullability List<SectionExitHandler> sectionsToExit;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffExit.class,
                "(exit|stop) [trigger]",
                "(exit|stop) [1|a|the|this] (section|1:loop|2:conditional)",
                "(exit|stop) <" + JavaClasses.INTEGER_NUMBER_PATTERN + "> (section|1:loop|2:conditional)s",
                "(exit|stop) all (section|1:loop|2:conditional)s"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        List<TriggerSection> innerSections = null;
        switch (matchedPattern) {
            case 0 -> {
                innerSections = getParser().getCurrentSections();
                breakLevels = innerSections.size() + 1;
            }
            case 1, 2 -> {
                breakLevels = matchedPattern == 1 ? 1 : Integer.parseInt(parseResult.regexes.getFirst().group());
                if (breakLevels < 1) {
                    return false;
                }
                type = parseResult.mark;
                ParserInstance parser = getParser();
                int levels = parser.getCurrentSections(TYPES[type]).size();
                if (breakLevels > levels) {
                    if (levels == 0) {
                        Skript.error("Can't stop any " + NAMES[type] + " as there are no " + NAMES[type] + " present");
                    } else {
                        Skript.error("Can't stop " + breakLevels + " " + NAMES[type] + " as there are only "
                                + levels + " " + NAMES[type] + " present");
                    }
                    return false;
                }
                innerSections = parser.getSections(breakLevels, TYPES[type]);
                outerSection = innerSections.getFirst();
            }
            case 3 -> {
                ParserInstance parser = getParser();
                type = parseResult.mark;
                List<? extends TriggerSection> sections = parser.getCurrentSections(TYPES[type]);
                if (sections.isEmpty()) {
                    Skript.error("Can't stop any " + NAMES[type] + " as there are no " + NAMES[type] + " present");
                    return false;
                }
                outerSection = sections.getFirst();
                innerSections = parser.getSectionsUntil(outerSection);
                innerSections.addFirst(outerSection);
                breakLevels = innerSections.size();
            }
            default -> {
            }
        }

        assert innerSections != null;
        sectionsToExit = innerSections.stream()
                .filter(SectionExitHandler.class::isInstance)
                .map(SectionExitHandler.class::cast)
                .toList();
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        debug(event, false);
        for (SectionExitHandler section : sectionsToExit) {
            section.exit(event);
        }
        if (outerSection == null) {
            return null;
        }
        return outerSection instanceof LoopSection loopSection ? loopSection.getActualNext() : outerSection.getNext();
    }

    @Override
    protected void execute(SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected @Nullable ExecutionIntent executionIntent() {
        if (outerSection == null) {
            return ExecutionIntent.stopTrigger();
        }
        return ExecutionIntent.stopSections(breakLevels);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (outerSection == null) {
            return "stop trigger";
        }
        return "stop " + breakLevels + " " + NAMES[type];
    }
}
