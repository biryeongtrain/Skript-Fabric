package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.lang.reflect.Array;
import java.util.Arrays;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Nearest Entity")
@Description("Gets the entity nearest to a location or another entity.")
@Example("kill nearest cow relative to player")
@Example("teleport player to nearest pig relative to location of player")
@Since("2.7, Fabric")
public class ExprNearestEntity extends SimpleExpression<Entity> {

    static {
        Skript.registerExpression(
                ExprNearestEntity.class,
                Entity.class,
                "[the] (nearest|closest) %*entitydatas% [[relative] to %-entity/location%]",
                "[the] %*entitydatas% (nearest|closest) [to %-entity/location%]"
        );
    }

    private EntityData<?>[] entityDatas;
    private Expression<?> relativeTo;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entityDatas = ((Literal<EntityData<?>>) exprs[0]).getArray(null);
        if (entityDatas.length != Arrays.stream(entityDatas).distinct().count()) {
            Skript.error("Entity list may not contain duplicate entities");
            return false;
        }
        relativeTo = exprs[1] == null ? new SimpleExpression<>() {
            @Override
            protected Object @Nullable [] get(SkriptEvent event) {
                FabricLocation location = FabricLocationExpressionSupport.eventLocation(event);
                return location == null ? new Object[0] : new Object[]{location};
            }

            @Override
            public boolean isSingle() {
                return true;
            }

            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
        } : exprs[1];
        return true;
    }

    @Override
    protected Entity[] get(SkriptEvent event) {
        Object relativeObject = relativeTo.getSingle(event);
        FabricLocation relativePoint = FabricLocationExpressionSupport.locationOf(relativeObject);
        if (relativePoint == null || relativePoint.level() == null) {
            return (Entity[]) Array.newInstance(getReturnType(), 0);
        }

        Entity excludedEntity = relativeObject instanceof Entity entity ? entity : null;
        Entity[] nearestEntities = (Entity[]) Array.newInstance(getReturnType(), entityDatas.length);
        for (int index = 0; index < entityDatas.length; index++) {
            nearestEntities[index] = getNearestEntity(entityDatas[index], relativePoint, excludedEntity);
        }
        return nearestEntities;
    }

    @Override
    public boolean isSingle() {
        return entityDatas.length == 1;
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return entityDatas.length == 1 ? entityDatas[0].getType() : Entity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "nearest " + Arrays.toString(entityDatas) + " relative to " + relativeTo.toString(event, debug);
    }

    private @Nullable Entity getNearestEntity(EntityData<?> entityData, FabricLocation relativePoint, @Nullable Entity excludedEntity) {
        WorldBorder border = relativePoint.level().getWorldBorder();
        AABB bounds = new AABB(
                border.getMinX(),
                relativePoint.level().getMinY(),
                border.getMinZ(),
                border.getMaxX(),
                relativePoint.level().getMaxY(),
                border.getMaxZ()
        );

        Entity nearestEntity = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : relativePoint.level().getEntitiesOfClass(entityData.getType(), bounds, entityData::isInstance)) {
            if (entity == excludedEntity) {
                continue;
            }
            double distance = entity.distanceToSqr(relativePoint.position());
            if (nearestEntity == null || distance < nearestDistance) {
                nearestDistance = distance;
                nearestEntity = entity;
            }
        }
        return nearestEntity;
    }
}
