package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.PrivateBellAccess;

@Name("Ringing Time")
@Description({
        "Returns the ringing time of a bell.",
        "A bell typically rings for 50 game ticks."
})
@Example("broadcast \"The bell has been ringing for %ringing time of target block%\"")
@Since("2.9.0, Fabric")
public class ExprRingingTime extends SimplePropertyExpression<FabricBlock, Timespan> {

    static {
        register(ExprRingingTime.class, Timespan.class, "ring[ing] time", "blocks");
    }

    @Override
    public @Nullable Timespan convert(FabricBlock block) {
        if (!(block.level().getBlockEntity(block.position()) instanceof BellBlockEntity bell)) {
            return null;
        }
        int ringingTicks = PrivateBellAccess.ringingTicks(bell);
        return ringingTicks == 0 ? null : new Timespan(Timespan.TimePeriod.TICK, ringingTicks);
    }

    @Override
    protected String getPropertyName() {
        return "ringing time";
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }
}
