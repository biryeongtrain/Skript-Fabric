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
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffContinue extends Effect {

    private static boolean registered;

    private int level = -1;
    private @UnknownNullability LoopSection loop;
    private @UnknownNullability List<SectionExitHandler> sectionsToExit;
    private int breakLevels;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffContinue.class,
                "continue [this loop|[the] [current] loop]",
                "continue [the] <" + JavaClasses.INTEGER_NUMBER_PATTERN + ">(st|nd|rd|th) loop"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        ParserInstance parser = getParser();
        int loops = parser.getCurrentSections(LoopSection.class).size();
        if (loops == 0) {
            Skript.error("The 'continue' effect may only be used in loops");
            return false;
        }

        level = matchedPattern == 0 ? loops : Integer.parseInt(parseResult.regexes.getFirst().group());
        if (level < 1) {
            return false;
        }

        int levels = loops - level + 1;
        if (levels <= 0) {
            Skript.error("Can't continue the " + ordinal(level) + " loop as there "
                    + (loops == 1 ? "is only 1 loop" : "are only " + loops + " loops") + " present");
            return false;
        }

        List<TriggerSection> innerSections = parser.getSections(levels, LoopSection.class);
        breakLevels = innerSections.size();
        loop = (LoopSection) innerSections.removeFirst();
        sectionsToExit = innerSections.stream()
                .filter(SectionExitHandler.class::isInstance)
                .map(SectionExitHandler.class::cast)
                .toList();
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        debug(event, false);
        for (SectionExitHandler section : sectionsToExit) {
            section.exit(event);
        }
        return loop;
    }

    @Override
    protected @Nullable ExecutionIntent executionIntent() {
        return ExecutionIntent.stopSections(breakLevels);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "continue" + (level == -1 ? "" : " the " + ordinal(level) + " loop");
    }

    private static String ordinal(int value) {
        int mod100 = value % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return value + "th";
        }
        return switch (value % 10) {
            case 1 -> value + "st";
            case 2 -> value + "nd";
            case 3 -> value + "rd";
            default -> value + "th";
        };
    }
}
