package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

public record FabricPotionEffectHandle(
        ServerLevel level,
        LivingEntity entity,
        @Nullable SkriptPotionEffect currentEffect,
        @Nullable SkriptPotionEffect previousEffect,
        FabricPotionEffectAction action,
        FabricPotionEffectCause cause
) implements FabricPotionEffectEventHandle {
}
