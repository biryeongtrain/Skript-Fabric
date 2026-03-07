package org.skriptlang.skript.fabric.runtime;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

public interface FabricPotionEffectEventHandle extends FabricEntityEventHandle {

    ServerLevel level();

    LivingEntity entity();

    @Nullable SkriptPotionEffect currentEffect();

    @Nullable SkriptPotionEffect previousEffect();

    FabricPotionEffectAction action();

    FabricPotionEffectCause cause();

    default @Nullable SkriptPotionEffect effect(int time) {
        return time == 1 ? previousEffect() : currentEffect();
    }

    default @Nullable Holder<MobEffect> modifiedType() {
        SkriptPotionEffect effect = currentEffect();
        if (effect != null) {
            return effect.type();
        }
        effect = previousEffect();
        return effect == null ? null : effect.type();
    }
}
