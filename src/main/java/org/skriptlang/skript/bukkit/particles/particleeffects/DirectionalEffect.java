package org.skriptlang.skript.bukkit.particles.particleeffects;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

public final class DirectionalEffect extends ParticleEffect {

    public DirectionalEffect(ParticleOptions particle) {
        super(particle);
    }

    public DirectionalEffect velocity(Vec3 velocity) {
        count(0);
        offset(velocity);
        return this;
    }

    public Vec3 velocity() {
        return offset();
    }

    @Override
    public DirectionalEffect copy() {
        DirectionalEffect copy = new DirectionalEffect(particle());
        copy.count(count());
        copy.offset(offset());
        copy.extra(extra());
        copy.data(data());
        return copy;
    }
}
