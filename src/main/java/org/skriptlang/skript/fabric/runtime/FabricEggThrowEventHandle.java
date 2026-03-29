package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import org.jetbrains.annotations.Nullable;

public interface FabricEggThrowEventHandle extends FabricEntityEventHandle {

    @Nullable ThrownEgg egg();

    @Override
    default @Nullable ThrownEgg entity() {
        return egg();
    }

    boolean hatching();

    void setHatching(boolean hatching);

    byte hatches();

    void setHatches(byte hatches);

    @Nullable EntityType<?> hatchingType();

    void setHatchingType(EntityType<?> hatchingType);
}
