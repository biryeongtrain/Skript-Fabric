package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Do Respawn Anchors Work")
@Description("Checks whether or not respawn anchors work in a world.")
@Example("respawn anchors work in world \"world_nether\"")
@RequiredPlugins("Minecraft 1.16+")
@Since("2.7")
public class CondAnchorWorks extends Condition {

    static {
        Skript.registerCondition(CondAnchorWorks.class, "respawn anchors [do[1:(n't| not)]] work in %worlds%");
    }

    private Expression<ServerLevel> worlds;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        worlds = (Expression<ServerLevel>) exprs[0];
        setNegated(parseResult.mark == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return worlds.check(event, world -> world.dimensionType().respawnAnchorWorks(), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "respawn anchors " + (isNegated() ? "don't" : "do") + " work in " + worlds.toString(event, debug);
    }
}
