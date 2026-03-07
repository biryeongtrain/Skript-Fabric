package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public record FabricBreedingHandle(
        ServerLevel level,
        LivingEntity mother,
        LivingEntity father,
        LivingEntity offspring,
        LivingEntity breeder
) implements FabricBreedingEventHandle {
}
