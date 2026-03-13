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

@Name("Resonating Time")
@Description({
        "Returns the resonating time of a bell.",
        "A bell starts resonating after it has been rung and nearby raiders are detected."
})
@Example("broadcast \"The bell has been resonating for %resonating time of target block%\"")
@Since("2.9.0, Fabric")
public class ExprResonatingTime extends SimplePropertyExpression<FabricBlock, Timespan> {

    static {
        register(ExprResonatingTime.class, Timespan.class, "resonat(e|ing) time", "blocks");
    }

    @Override
    public @Nullable Timespan convert(FabricBlock block) {
        if (!(block.level().getBlockEntity(block.position()) instanceof BellBlockEntity bell)) {
            return null;
        }
        int resonatingTicks = PrivateBellAccess.resonatingTicks(bell);
        return resonatingTicks == 0 ? null : new Timespan(Timespan.TimePeriod.TICK, resonatingTicks);
    }

    @Override
    protected String getPropertyName() {
        return "resonating time";
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }
}
