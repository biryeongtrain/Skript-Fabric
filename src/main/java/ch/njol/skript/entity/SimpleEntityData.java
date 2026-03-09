package ch.njol.skript.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class SimpleEntityData extends EntityData<Entity> {

    private final @Nullable net.minecraft.world.entity.EntityType<?> entityType;
    private final boolean exactType;

    SimpleEntityData(String codeName, Class<? extends Entity> entityClass) {
        this(codeName, entityClass, null, false);
    }

    private SimpleEntityData(
            String codeName,
            Class<? extends Entity> entityClass,
            @Nullable net.minecraft.world.entity.EntityType<?> entityType,
            boolean exactType
    ) {
        super(codeName, entityClass);
        this.entityType = entityType;
        this.exactType = exactType;
    }

    static SimpleEntityData exact(String codeName, net.minecraft.world.entity.EntityType<?> entityType) {
        return new SimpleEntityData(codeName, entityType.getBaseClass(), entityType, true);
    }

    static SimpleEntityData supertype(String codeName, Class<? extends Entity> entityClass) {
        return new SimpleEntityData(codeName, entityClass, null, false);
    }

    public net.minecraft.world.entity.EntityType<?> getMinecraftType() {
        if (entityType == null) {
            throw new IllegalStateException("Supertype entity data does not expose a single minecraft entity type");
        }
        return entityType;
    }

    boolean isExactType() {
        return exactType;
    }

    @Override
    public boolean matches(net.minecraft.world.entity.EntityType<?> entityType) {
        if (exactType) {
            return this.entityType == entityType;
        }
        return getType().isAssignableFrom(entityType.getBaseClass());
    }
}
