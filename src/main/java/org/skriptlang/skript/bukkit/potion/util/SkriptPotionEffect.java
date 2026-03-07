package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.lang.Expression;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;

public final class SkriptPotionEffect implements Cloneable {

    public static final int DEFAULT_DURATION_TICKS = 600;

    private final Holder<MobEffect> type;
    private @Nullable Integer amplifier;
    private @Nullable Boolean ambient;
    private @Nullable Boolean particles;
    private @Nullable Boolean icon;
    private @Nullable Integer duration;
    private @Nullable Boolean infinite;
    private @Nullable LivingEntity entitySource;

    public SkriptPotionEffect(
            Holder<MobEffect> type,
            @Nullable Integer amplifier,
            @Nullable Boolean ambient,
            @Nullable Boolean particles,
            @Nullable Boolean icon
    ) {
        this(type, amplifier, ambient, particles, icon, null, null);
    }

    public SkriptPotionEffect(
            Holder<MobEffect> type,
            @Nullable Integer amplifier,
            @Nullable Boolean ambient,
            @Nullable Boolean particles,
            @Nullable Boolean icon,
            @Nullable Integer duration,
            @Nullable Boolean infinite
    ) {
        this.type = type;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
        this.duration = duration;
        this.infinite = infinite;
    }

    public static SkriptPotionEffect fromType(Holder<MobEffect> type) {
        return new SkriptPotionEffect(type, null, null, null, null);
    }

    public static SkriptPotionEffect fromInstance(MobEffectInstance instance) {
        return fromInstance(instance, null);
    }

    public static SkriptPotionEffect fromInstance(MobEffectInstance instance, @Nullable LivingEntity source) {
        SkriptPotionEffect effect = new SkriptPotionEffect(
                instance.getEffect(),
                instance.getAmplifier(),
                instance.isAmbient(),
                instance.isVisible(),
                instance.showIcon(),
                instance.getDuration(),
                instance.isInfiniteDuration()
        );
        effect.entitySource = source;
        return effect;
    }

    public static boolean isChangeable(Expression<? extends SkriptPotionEffect> expression) {
        return expression != null;
    }

    public Holder<MobEffect> type() {
        return type;
    }

    public int amplifier() {
        return amplifier != null ? amplifier : 0;
    }

    public SkriptPotionEffect amplifier(int amplifier) {
        this.amplifier = Math.max(0, amplifier);
        applyToSource();
        return this;
    }

    public boolean ambient() {
        return ambient != null && ambient;
    }

    public SkriptPotionEffect ambient(boolean ambient) {
        this.ambient = ambient;
        applyToSource();
        return this;
    }

    public boolean particles() {
        return particles == null || particles;
    }

    public SkriptPotionEffect particles(boolean particles) {
        this.particles = particles;
        applyToSource();
        return this;
    }

    public boolean icon() {
        return icon == null || icon;
    }

    public SkriptPotionEffect icon(boolean icon) {
        this.icon = icon;
        applyToSource();
        return this;
    }

    public int duration() {
        return duration != null ? duration : DEFAULT_DURATION_TICKS;
    }

    public SkriptPotionEffect duration(int duration) {
        this.duration = Math.max(0, duration);
        this.infinite = Boolean.FALSE;
        applyToSource();
        return this;
    }

    public boolean infinite() {
        return infinite != null && infinite;
    }

    public SkriptPotionEffect infinite(boolean infinite) {
        this.infinite = infinite;
        if (infinite) {
            this.duration = MobEffectInstance.INFINITE_DURATION;
        } else if (duration == null || duration == MobEffectInstance.INFINITE_DURATION) {
            this.duration = DEFAULT_DURATION_TICKS;
        }
        applyToSource();
        return this;
    }

    public boolean instant() {
        return type.value().isInstantenous();
    }

    public MobEffectInstance asMobEffectInstance() {
        int resolvedDuration = infinite() ? MobEffectInstance.INFINITE_DURATION : duration();
        return new MobEffectInstance(type, resolvedDuration, amplifier(), ambient(), particles(), icon());
    }

    public boolean matchesQualities(MobEffectInstance instance) {
        if (!instance.is(type)) {
            return false;
        }
        if (amplifier != null && instance.getAmplifier() != amplifier.intValue()) {
            return false;
        }
        if (ambient != null && instance.isAmbient() != ambient.booleanValue()) {
            return false;
        }
        if (particles != null && instance.isVisible() != particles.booleanValue()) {
            return false;
        }
        if (icon != null && instance.showIcon() != icon.booleanValue()) {
            return false;
        }
        if (duration != null && instance.getDuration() != duration.intValue()) {
            return false;
        }
        if (infinite != null && instance.isInfiniteDuration() != infinite.booleanValue()) {
            return false;
        }
        return true;
    }

    public SkriptPotionEffect copy() {
        SkriptPotionEffect copy = new SkriptPotionEffect(type, amplifier, ambient, particles, icon, duration, infinite);
        copy.entitySource = null;
        return copy;
    }

    @Override
    public SkriptPotionEffect clone() {
        return copy();
    }

    @Override
    public String toString() {
        return PotionEffectSupport.effectId(type);
    }

    private void applyToSource() {
        if (entitySource == null) {
            return;
        }
        FabricPotionEffectCauseContext.run(FabricPotionEffectCause.PLUGIN, () -> {
            entitySource.removeEffect(type);
            entitySource.addEffect(asMobEffectInstance());
        });
    }
}
