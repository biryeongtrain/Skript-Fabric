package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.LivingEntity;

public interface FabricBreedingEventHandle {

    LivingEntity mother();

    LivingEntity father();

    LivingEntity offspring();

    LivingEntity breeder();
}
