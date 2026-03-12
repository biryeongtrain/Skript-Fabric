package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprCarryingBlockData extends SimplePropertyExpression<LivingEntity, BlockState> {

    static {
        register(ExprCarryingBlockData.class, BlockState.class, "carr(ied|ying) block[[ ]data]", "livingentities");
    }

    @Override
    public @Nullable BlockState convert(LivingEntity entity) {
        return entity instanceof EnderMan enderman ? enderman.getCarriedBlock() : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE -> new Class[]{FabricBlock.class, BlockState.class, FabricItemType.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        BlockState state = null;
        if (mode != ChangeMode.DELETE && delta != null && delta.length > 0) {
            Object value = delta[0];
            if (value instanceof BlockState blockState) {
                state = blockState;
            } else if (value instanceof FabricBlock block) {
                state = block.state();
            } else if (value instanceof FabricItemType itemType && itemType.item() instanceof BlockItem blockItem) {
                state = blockItem.getBlock().defaultBlockState();
            }
        }

        for (LivingEntity entity : getExpr().getArray(event)) {
            if (entity instanceof EnderMan enderman) {
                enderman.setCarriedBlock(state);
            }
        }
    }

    @Override
    public Class<? extends BlockState> getReturnType() {
        return BlockState.class;
    }

    @Override
    protected String getPropertyName() {
        return "carrying block data";
    }
}
