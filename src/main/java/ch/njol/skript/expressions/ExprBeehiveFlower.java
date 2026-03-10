package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import java.lang.reflect.Field;
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

    private static final Field SAVED_FLOWER_POS = findField(BeehiveBlockEntity.class, "savedFlowerPos");

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
        try {
            BlockPos flower = (BlockPos) SAVED_FLOWER_POS.get(beehive);
            return flower == null ? null : new FabricLocation(level, flower.getCenter());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read beehive flower position.", exception);
        }
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
            try {
                SAVED_FLOWER_POS.set(beehive, flower);
                beehive.setChanged();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to change beehive flower position.", exception);
            }
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

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access beehive flower position.", exception);
        }
    }
}
