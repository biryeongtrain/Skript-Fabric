package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("PvP")
@Description("Set the PvP state for a given world.")
@Example("enable PvP #(current world only)")
@Example("disable PvP in all worlds")
@Since("1.3.4")
public final class EffPvP extends Effect {

    private static boolean registered;

    private Expression<ServerLevel> worlds;
    private boolean enable;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffPvP.class, "enable PvP [in %worlds%]", "disable PVP [in %worlds%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        worlds = (Expression<ServerLevel>) exprs[0];
        enable = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.server() == null || worlds.getArray(event).length == 0) {
            return;
        }
        event.server().setPvpAllowed(enable);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (enable ? "enable" : "disable") + " PvP in " + worlds.toString(event, debug);
    }
}
