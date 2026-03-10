package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public interface FabricEggThrowEventHandle {

    boolean hatching();

    void setHatching(boolean hatching);

    byte hatches();

    void setHatches(byte hatches);

    @Nullable EntityType<?> hatchingType();

    void setHatchingType(EntityType<?> hatchingType);
}
