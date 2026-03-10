package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Release From Entity Storage")
@Description({
        "Releases the stored entities in an entity block storage (i.e. beehive).",
        "When using beehives, providing a timespan will prevent the released bees from re-entering the beehive for that amount of time.",
        "Due to unstable behaviour on older versions, this effect requires Minecraft version 1.21+."
})
@Example("release the stored entities of {_beehive}")
@Example("release the entity storage of {_hive} for 5 seconds")
@RequiredPlugins("Minecraft 1.21")
@Since("2.11")
public final class EffReleaseEntityStorage extends Effect {

    private static boolean registered;

    private Expression<FabricBlock> blocks;
    private @Nullable Expression<Timespan> timespan;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffReleaseEntityStorage.class,
                "(release|evict) [the] (stored entities|entity storage) of %blocks% [for %-timespan%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        blocks = (Expression<FabricBlock>) exprs[0];
        timespan = exprs[1] == null ? null : (Expression<Timespan>) exprs[1];
        Skript.error("Entity block storage mutation is not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("release the stored entities of", blocks);
        if (timespan != null) {
            builder.append("for", timespan);
        }
        return builder.toString();
    }
}
