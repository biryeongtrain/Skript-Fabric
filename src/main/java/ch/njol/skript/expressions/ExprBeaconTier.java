package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.PrivateBeaconAccess;

public class ExprBeaconTier extends SimplePropertyExpression<FabricBlock, Integer> {

    static {
        register(ExprBeaconTier.class, Integer.class, "beacon tier", "blocks");
    }

    @Override
    public @Nullable Integer convert(FabricBlock block) {
        BeaconBlockEntity beacon = block.level() == null
                ? null
                : (block.level().getBlockEntity(block.position()) instanceof BeaconBlockEntity found ? found : null);
        return beacon == null ? null : PrivateBeaconAccess.levels(beacon);
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "beacon tier";
    }
}
