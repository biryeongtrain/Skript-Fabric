package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

public class ExprScripts extends SimpleExpression<Script> implements ReflectionExperimentSyntax {

    static {
        Skript.registerExpression(
                ExprScripts.class,
                Script.class,
                "[all [[of] the]|the] scripts",
                "[all [[of] the]|the] (enabled|loaded) scripts",
                "[all [[of] the]|the] (disabled|unloaded) scripts"
        );
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        return true;
    }

    @Override
    protected Script[] get(SkriptEvent event) {
        if (pattern == 2) {
            return new Script[0];
        }
        return loadedScripts().toArray(Script[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Script> getReturnType() {
        return Script.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (pattern == 1) {
            return "all enabled scripts";
        }
        if (pattern == 2) {
            return "all disabled scripts";
        }
        return "all scripts";
    }

    private static List<Script> loadedScripts() {
        try {
            Field field = SkriptRuntime.class.getDeclaredField("scripts");
            field.setAccessible(true);
            Object value = field.get(SkriptRuntime.instance());
            if (value instanceof List<?> entries) {
                List<Script> scripts = new ArrayList<>(entries.size());
                for (Object entry : entries) {
                    if (entry instanceof Script script) {
                        scripts.add(script);
                    }
                }
                return scripts;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return List.of();
    }
}
