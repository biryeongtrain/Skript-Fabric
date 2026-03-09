package ch.njol.skript.entity;

import net.minecraft.world.entity.Entity;

abstract class ClassEntityData<E extends Entity> extends EntityData<E> {

    protected ClassEntityData(String codeName, Class<? extends E> entityClass) {
        super(codeName, entityClass);
    }

    @Override
    public boolean matches(net.minecraft.world.entity.EntityType<?> entityType) {
        return getType().isAssignableFrom(entityType.getBaseClass());
    }
}
