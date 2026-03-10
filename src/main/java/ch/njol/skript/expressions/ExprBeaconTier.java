package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

public class ExprBeaconTier extends SimplePropertyExpression<FabricBlock, Integer> {

    static {
        register(ExprBeaconTier.class, Integer.class, "beacon tier", "blocks");
    }

    @Override
    public @Nullable Integer convert(FabricBlock block) {
        BeaconBlockEntity beacon = block.level() == null
                ? null
                : (block.level().getBlockEntity(block.position()) instanceof BeaconBlockEntity found ? found : null);
        if (beacon == null) {
            return null;
        }
        try {
            java.lang.reflect.Field field = BeaconBlockEntity.class.getDeclaredField("levels");
            field.setAccessible(true);
            return field.getInt(beacon);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
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
