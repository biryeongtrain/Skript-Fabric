package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("PvP")
@Description("Checks the PvP state of a world.")
@Example("PvP is enabled")
@Example("PvP is disabled in \"world\"")
@Since("1.3.4")
public class CondPvP extends Condition {

    static {
        Skript.registerCondition(CondPvP.class, "(is PvP|PvP is) enabled [in %worlds%]", "(is PvP|PvP is) disabled [in %worlds%]");
    }

    private Expression<ServerLevel> worlds;
    private boolean enabled;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        worlds = (Expression<ServerLevel>) exprs[0];
        enabled = matchedPattern == 0;
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return worlds.check(event, world -> world.getServer().isPvpAllowed() == enabled, isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "PvP is " + (enabled ? "enabled" : "disabled") + " in " + worlds.toString(event, debug);
    }
}
