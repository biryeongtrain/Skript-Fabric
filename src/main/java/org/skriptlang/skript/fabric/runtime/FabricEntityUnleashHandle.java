package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class FabricEntityUnleashHandle implements FabricEntityEventHandle, FabricEntityUnleashEventHandle {

    private final Entity entity;
    private final @Nullable Entity actor;
    private boolean dropLeash;

    public FabricEntityUnleashHandle(Entity entity, @Nullable Entity actor, boolean dropLeash) {
        this.entity = entity;
        this.actor = actor;
        this.dropLeash = dropLeash;
    }

    @Override
    public Entity entity() {
        return entity;
    }

    public @Nullable Entity actor() {
        return actor;
    }

    public boolean isDropLeash() {
        return dropLeash;
    }

    @Override
    public void setDropLeash(boolean dropLeash) {
        this.dropLeash = dropLeash;
    }
}
