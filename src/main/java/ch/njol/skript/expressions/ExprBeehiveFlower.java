package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import kim.biryeong.skriptFabric.mixin.BeehiveBlockEntityFlowerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Beehive Target Flower")
@Description("The flower a beehive has selected to pollinate from.")
@Example("set the target flower of {_beehive} to location(0, 0, 0)")
@Example("clear the target flower of {_beehive}")
@Since("2.11")
public class ExprBeehiveFlower extends SimplePropertyExpression<FabricBlock, FabricLocation> {

    static {
        registerDefault(ExprBeehiveFlower.class, FabricLocation.class, "target flower", "blocks");
    }

    @Override
    public @Nullable FabricLocation convert(FabricBlock block) {
        if (!(block.level() instanceof ServerLevel level)) {
            return null;
        }
        if (!(block.level().getBlockEntity(block.position()) instanceof BeehiveBlockEntity beehive)) {
            return null;
        }
        BlockPos flower = ((BeehiveBlockEntityFlowerAccessor) beehive).skript$getSavedFlowerPos();
        return flower == null ? null : new FabricLocation(level, flower.getCenter());
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE -> new Class[]{FabricLocation.class, FabricBlock.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        BlockPos flower = null;
        if (delta != null && delta.length > 0) {
            Object value = delta[0];
            if (value instanceof FabricLocation location) {
                flower = BlockPos.containing(location.position());
            } else if (value instanceof FabricBlock block) {
                flower = block.position();
            }
        }
        for (FabricBlock block : getExpr().getArray(event)) {
            if (!(block.level().getBlockEntity(block.position()) instanceof BeehiveBlockEntity beehive)) {
                continue;
            }
            ((BeehiveBlockEntityFlowerAccessor) beehive).skript$setSavedFlowerPos(flower);
            beehive.setChanged();
        }
    }

    @Override
    public Class<FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String getPropertyName() {
        return "target flower";
    }
}
