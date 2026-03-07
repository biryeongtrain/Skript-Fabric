package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public record FabricDamageHandle(
        ServerLevel level,
        LivingEntity entity,
        DamageSource damageSource,
        float amount
) implements FabricDamageEventHandle {
}
