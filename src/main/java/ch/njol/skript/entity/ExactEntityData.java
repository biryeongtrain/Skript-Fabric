package ch.njol.skript.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

abstract class ExactEntityData<E extends Entity> extends EntityData<E> {

    private final net.minecraft.world.entity.EntityType<?> entityType;

    protected ExactEntityData(String codeName, Class<? extends E> entityClass, String entityTypePath) {
        super(codeName, entityClass);
        ResourceLocation id = ResourceLocation.withDefaultNamespace(entityTypePath);
        net.minecraft.world.entity.EntityType<?> resolved = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        if (resolved == null) {
            throw new IllegalStateException("Unknown entity type: " + id);
        }
        this.entityType = resolved;
    }

    net.minecraft.world.entity.EntityType<?> getMinecraftType() {
        return entityType;
    }

    @Override
    public boolean matches(net.minecraft.world.entity.EntityType<?> entityType) {
        return this.entityType == entityType;
    }
}
