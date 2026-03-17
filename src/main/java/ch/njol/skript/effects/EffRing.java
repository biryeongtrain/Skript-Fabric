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
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Ring Bell")
@Description({
        "Causes a bell to ring.",
        "Optionally, the entity that rang the bell and the direction the bell should ring can be specified.",
        "A bell can only ring in two directions, and the direction is determined by which way the bell is facing.",
        "By default, the bell will ring in the direction it is facing.",
})
@Example("make player ring target-block")
@Since("2.9.0")
public final class EffRing extends Effect {

    private static boolean registered;
    private @Nullable Expression<Entity> entity;
    private Expression<FabricBlock> blocks;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffRing.class,
                "ring %blocks%",
                "(make|let) %entity% ring %blocks%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entity = matchedPattern == 0 ? null : (Expression<Entity>) exprs[0];
        blocks = (Expression<FabricBlock>) exprs[matchedPattern];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Entity ringer = entity != null ? entity.getSingle(event) : null;
        for (FabricBlock fabricBlock : blocks.getArray(event)) {
            BlockState state = fabricBlock.state();
            if (!(state.getBlock() instanceof BellBlock bellBlock))
                continue;
            Direction facing = state.getValue(BellBlock.FACING);
            if (ringer != null) {
                bellBlock.attemptToRing(ringer, fabricBlock.level(), fabricBlock.position(), facing);
            } else {
                bellBlock.attemptToRing(fabricBlock.level(), fabricBlock.position(), facing);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (entity != null ? "make " + entity.toString(event, debug) + " " : "")
                + "ring " + blocks.toString(event, debug);
    }
}
