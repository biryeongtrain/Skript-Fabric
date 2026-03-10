package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Version;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondMinecraftVersion extends Condition {

    static {
        Skript.registerCondition(CondMinecraftVersion.class, "running [(1¦below)] minecraft %string%");
    }

    private Expression<String> version;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        version = (Expression<String>) exprs[0];
        setNegated(parseResult.mark == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        String ver = version.getSingle(event);
        return ver != null && (Skript.isRunningMinecraft(new Version(ver)) ^ isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "is running minecraft " + version.toString(event, debug);
    }
}
