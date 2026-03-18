package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class FabricDamageHandle implements FabricDamageEventHandle {

    private final ServerLevel level;
    private final LivingEntity entity;
    private final DamageSource damageSource;
    private float amount;

    public FabricDamageHandle(ServerLevel level, LivingEntity entity, DamageSource damageSource, float amount) {
        this.level = level;
        this.entity = entity;
        this.damageSource = damageSource;
        this.amount = amount;
    }

    public ServerLevel level() { return level; }
    @Override public LivingEntity entity() { return entity; }
    @Override public DamageSource damageSource() { return damageSource; }
    @Override public float amount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
}
