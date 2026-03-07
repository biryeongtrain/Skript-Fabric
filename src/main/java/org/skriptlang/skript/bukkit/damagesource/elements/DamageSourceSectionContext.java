package org.skriptlang.skript.bukkit.damagesource.elements;

import java.lang.reflect.Constructor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public final class DamageSourceSectionContext {

    private final @Nullable ServerLevel level;
    private @Nullable net.minecraft.core.Holder<DamageType> damageType;
    private @Nullable Entity causingEntity;
    private @Nullable Entity directEntity;
    private @Nullable FabricLocation damageLocation;

    public DamageSourceSectionContext(@Nullable ServerLevel level) {
        this.level = level;
    }

    public @Nullable net.minecraft.core.Holder<DamageType> damageType() {
        return damageType;
    }

    public void damageType(net.minecraft.core.Holder<DamageType> damageType) {
        this.damageType = damageType;
    }

    public @Nullable Entity causingEntity() {
        return causingEntity;
    }

    public void causingEntity(@Nullable Entity causingEntity) {
        this.causingEntity = causingEntity;
    }

    public @Nullable Entity directEntity() {
        return directEntity;
    }

    public void directEntity(@Nullable Entity directEntity) {
        this.directEntity = directEntity;
    }

    public @Nullable FabricLocation damageLocation() {
        return damageLocation;
    }

    public void damageLocation(@Nullable FabricLocation damageLocation) {
        this.damageLocation = damageLocation;
    }

    public DamageSource build() {
        Vec3 position = damageLocation == null ? null : damageLocation.position();
        DamageSource fallback = level != null ? level.damageSources().generic() : null;
        if (damageType == null) {
            return fallback != null ? fallback : createFallback(position);
        }
        try {
            Constructor<DamageSource> constructor = DamageSource.class.getDeclaredConstructor(
                    net.minecraft.core.Holder.class,
                    Entity.class,
                    Entity.class,
                    Vec3.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(damageType, directEntity, causingEntity, position);
        } catch (ReflectiveOperationException ignored) {
            return fallback != null ? fallback : createFallback(position);
        }
    }

    private DamageSource createFallback(@Nullable Vec3 position) {
        if (level == null) {
            throw new IllegalStateException("Cannot construct a fallback damage source without a server level.");
        }
        try {
            Constructor<DamageSource> constructor = DamageSource.class.getDeclaredConstructor(
                    net.minecraft.core.Holder.class,
                    Entity.class,
                    Entity.class,
                    Vec3.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(level.damageSources().generic().typeHolder(), directEntity, causingEntity, position);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to construct a Minecraft damage source.", exception);
        }
    }
}
