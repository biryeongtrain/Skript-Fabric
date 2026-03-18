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
import kim.biryeong.skriptFabric.mixin.BeehiveBlockEntityStoredAccessor;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Clear Entity Storage")
@Description("Clear the stored entities of an entity block storage (i.e. beehive).")
@Example("clear the stored entities of {_beehive}")
@Since("2.11")
public final class EffClearEntityStorage extends Effect {

    private static boolean registered;

    private Expression<FabricBlock> blocks;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffClearEntityStorage.class,
                "(clear|empty) the (stored entities|entity storage) of %blocks%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        blocks = (Expression<FabricBlock>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (FabricBlock block : blocks.getArray(event)) {
            if (block.blockEntity() instanceof BeehiveBlockEntity beehive) {
                ((BeehiveBlockEntityStoredAccessor) beehive).skript$getStored().clear();
                beehive.setChanged();
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "clear the stored entities of " + blocks.toString(event, debug);
    }
}
