package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Charges")
@Description("Returns the current charges of a respawn anchor block.")
@Example("set {_charges} to charges of target block")
@Since("2.9")
public class ExprCharges extends SimplePropertyExpression<FabricBlock, Integer> {

    static {
        register(ExprCharges.class, Integer.class, "charge[s]", "blocks");
    }

    @Override
    public @Nullable Integer convert(FabricBlock block) {
        BlockState state = block.state();
        if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
            return null;
        }
        return state.getValue(RespawnAnchorBlock.CHARGE);
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String getPropertyName() {
        return "charges";
    }
}
