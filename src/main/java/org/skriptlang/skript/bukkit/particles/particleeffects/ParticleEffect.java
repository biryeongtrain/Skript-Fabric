package org.skriptlang.skript.bukkit.particles.particleeffects;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public class ParticleEffect {

    protected final ParticleOptions particle;
    private int count = 1;
    private Vec3 offset = Vec3.ZERO;
    private double extra = 0.0D;
    private @Nullable Object data;

    public static ParticleEffect of(ParticleOptions particle) {
        ResourceLocation id = BuiltInRegistries.PARTICLE_TYPE.getKey(particle.getType());
        if (id != null && (id.equals(ResourceLocation.withDefaultNamespace("electric_spark"))
                || id.equals(ResourceLocation.withDefaultNamespace("flame"))
                || id.equals(ResourceLocation.withDefaultNamespace("smoke")))) {
            return new DirectionalEffect(particle);
        }
        return new ParticleEffect(particle);
    }

    protected ParticleEffect(ParticleOptions particle) {
        this.particle = particle;
    }

    public ParticleOptions particle() {
        return particle;
    }

    public ParticleType<?> type() {
        return particle.getType();
    }

    public int count() {
        return count;
    }

    public ParticleEffect count(int count) {
        this.count = Math.max(0, count);
        return this;
    }

    public Vec3 offset() {
        return offset;
    }

    public ParticleEffect offset(Vec3 offset) {
        this.offset = offset;
        return this;
    }

    public Vec3 distribution() {
        return offset;
    }

    public ParticleEffect distribution(Vec3 distribution) {
        if (count == 0) {
            count = 1;
        }
        this.offset = distribution;
        return this;
    }

    public boolean isUsingNormalDistribution() {
        return count > 0;
    }

    public double extra() {
        return extra;
    }

    public ParticleEffect extra(double extra) {
        this.extra = extra;
        return this;
    }

    public @Nullable Object data() {
        return data;
    }

    public ParticleEffect data(@Nullable Object data) {
        this.data = data;
        return this;
    }

    public ParticleEffect copy() {
        ParticleEffect copy = of(particle);
        copy.count = count;
        copy.offset = offset;
        copy.extra = extra;
        copy.data = data;
        return copy;
    }

    @Override
    public String toString() {
        return count + " " + MinecraftResourceParser.display(BuiltInRegistries.PARTICLE_TYPE.getKey(type()));
    }
}
