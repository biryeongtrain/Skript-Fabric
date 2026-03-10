package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.script.Script;

public class CondIsUsingFeature extends Condition {

    static {
        Skript.registerCondition(CondIsUsingFeature.class,
                "%script% is using %strings%",
                "%scripts% are using %strings%",
                "%script% is(n't| not) using %strings%",
                "%scripts% are(n't| not) using %strings%");
    }

    private Expression<String> names;
    private Expression<Script> scripts;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
        names = (Expression<String>) expressions[1];
        scripts = (Expression<Script>) expressions[0];
        setNegated(pattern > 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        String[] values = names.getArray(event);
        if (values.length == 0) {
            return true ^ isNegated();
        }
        boolean using = true;
        for (Script script : scripts.getArray(event)) {
            ExperimentSet experiments = script.getData(ExperimentSet.class);
            if (experiments == null) {
                using = false;
                continue;
            }
            for (String value : values) {
                using &= experiments.hasExperiment(value);
            }
        }
        return using ^ isNegated();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String verb = scripts.isSingle() ? (isNegated() ? "isn't" : "is") : (isNegated() ? "aren't" : "are");
        return scripts.toString(event, debug) + " " + verb + " using " + names.toString(event, debug);
    }
}
