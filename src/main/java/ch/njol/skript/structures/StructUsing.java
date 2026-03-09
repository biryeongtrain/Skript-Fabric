package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.LifeCycle;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public class StructUsing extends Structure {

    public static final Priority PRIORITY = new Priority(15);
    private static final String[] PATTERNS = {"using [[the] experiment] <.+>"};

    private @Nullable Experiment experiment;

    public static void register() {
        Skript.registerStructure(StructUsing.class, SyntaxInfo.Structure.NodeType.SIMPLE, PATTERNS);
    }

    @Override
    public boolean init(
            Literal<?> @NotNull [] arguments,
            int pattern,
            ParseResult result,
            @Nullable EntryContainer container
    ) {
        if (result.regexes.isEmpty()) {
            return false;
        }
        return enableExperiment(result.regexes.getFirst().group());
    }

    private boolean enableExperiment(String name) {
        Script script = getParser().getCurrentScript();
        if (script == null) {
            return false;
        }

        Experiment parsed = Skript.experiments().find(name.trim());
        experiment = parsed;

        LifeCycle phase = parsed.phase();
        if (phase == LifeCycle.MAINSTREAM) {
            Skript.warning("The experimental feature '" + name + "' is now included by default and is no longer required.");
        } else if (phase == LifeCycle.DEPRECATED) {
            Skript.warning("The experimental feature '" + name + "' is deprecated and may be removed in future versions.");
        } else if (phase == LifeCycle.UNKNOWN) {
            Skript.warning("The experimental feature '" + name + "' was not found.");
        }

        script.getData(ExperimentSet.class, ExperimentSet::new).add(parsed);
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "using " + (experiment == null ? "unknown" : experiment.codeName());
    }
}
