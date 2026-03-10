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
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Insert Entity Storage")
@Description({
        "Add an entity into the entity storage of a block (e.g. beehive).",
        "The entity must be of the right type for the block (e.g. bee for beehive).",
        "Due to unstable behavior on older versions, adding entities to an entity storage requires Minecraft version 1.21+."
})
@Example("add last spawned bee into the entity storage of {_beehive}")
@RequiredPlugins("Minecraft 1.21+")
@Since("2.11")
public final class EffInsertEntityStorage extends Effect {

    private static boolean registered;

    private Expression<? extends LivingEntity> entities;
    private Expression<FabricBlock> block;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffInsertEntityStorage.class,
                "(add|insert) %livingentities% [in[ ]]to [the] (stored entities|entity storage) of %block%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<? extends LivingEntity>) exprs[0];
        block = (Expression<FabricBlock>) exprs[1];
        Skript.error("Entity block storage mutation is not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "add " + entities.toString(event, debug) + " into the entity storage of " + block.toString(event, debug);
    }
}
