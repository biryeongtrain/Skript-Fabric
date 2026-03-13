package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Spawner Type")
@Description("""
    The entity type of a spawner (mob spawner).
    Change the entity type or reset it back to a pig.
    """)
@Example("""
    if event-block is a spawner:
        send "Spawner type: %spawner type of event-block%" to player
    """)
@Example("set the spawner type of {_spawner} to a cow")
@Example("reset the spawner type of {_spawner}")
@Since("2.4, Fabric")
public class ExprSpawnerType extends SimplePropertyExpression<FabricBlock, EntityData<?>> {

    static {
        @SuppressWarnings("unchecked")
        Class<EntityData<?>> returnType = (Class<EntityData<?>>) (Class<?>) EntityData.class;
        register(ExprSpawnerType.class, returnType, "(spawner|entity|creature) type[s]", "blocks");
    }

    @Override
    public @Nullable EntityData<?> convert(FabricBlock block) {
        if (!(block.level().getBlockEntity(block.position()) instanceof SpawnerBlockEntity spawner)) {
            return null;
        }
        Entity displayEntity = spawner.getSpawner().getOrCreateDisplayEntity(block.level(), block.position());
        return EntityData.fromEntity(displayEntity);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET -> new Class[]{EntityData.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        EntityType<?> entityType = mode == ChangeMode.RESET ? EntityType.PIG : resolveType(delta == null ? null : (EntityData<?>) delta[0]);
        if (entityType == null) {
            return;
        }
        for (FabricBlock block : getExpr().getArray(event)) {
            if (!(block.level().getBlockEntity(block.position()) instanceof SpawnerBlockEntity spawner)) {
                continue;
            }
            spawner.setEntityId(entityType, block.level().getRandom());
            spawner.setChanged();
            block.level().sendBlockUpdated(block.position(), block.state(), block.state(), 3);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends EntityData<?>> getReturnType() {
        return (Class<? extends EntityData<?>>) (Class<?>) EntityData.class;
    }

    @Override
    protected String getPropertyName() {
        return "spawner type";
    }

    private static @Nullable EntityType<?> resolveType(@Nullable EntityData<?> data) {
        if (data == null) {
            return null;
        }
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (data.matches(type) && type.canSerialize() && type.canSummon()) {
                return type;
            }
        }
        return null;
    }
}
