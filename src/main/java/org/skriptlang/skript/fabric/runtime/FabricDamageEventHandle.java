package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.LivingEntity;

public interface FabricDamageEventHandle extends FabricEntityEventHandle, FabricDamageSourceEventHandle {

    @Override
    LivingEntity entity();

    float amount();
}
