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
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Update Block")
@Description({
        "Updates the blocks by setting them to a selected block",
        "Using 'without physics' will not send updates to the surrounding blocks of the blocks being set.",
        "Example: Updating a block next to a sand block in the air 'without physics' will not cause the sand block to fall."
})
@Example("update {_blocks::*} as gravel")
@Example("update {_blocks::*} to be sand without physics updates")
@Example("update {_blocks::*} as stone without neighbouring updates")
@Since("2.10")
public final class EffBlockUpdate extends Effect {

    private static boolean registered;

    private boolean physics;
    private Expression<FabricBlock> blocks;
    private Expression<Object> blockData;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffBlockUpdate.class,
                "update %blocks% (as|to be) %objects% [physics:without [neighbo[u]r[ing]|adjacent] [physics] update[s]]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        physics = !parseResult.hasTag("physics");
        blocks = (Expression<FabricBlock>) exprs[0];
        blockData = (Expression<Object>) exprs[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object value = blockData.getSingle(event);
        if (!(value instanceof BlockState state)) {
            return;
        }
        for (FabricBlock block : blocks.getArray(event)) {
            block.level().setBlock(block.position(), state, physics ? 3 : 2);
        }
    }

    @Override
    public @NotNull String toString(@Nullable SkriptEvent event, boolean debug) {
        return "update " + blocks.toString(event, debug) + " as "
                + blockData.toString(event, debug) + (physics ? "" : " without neighbour updates");
    }
}
