package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record FabricBreedingHandle(
        ServerLevel level,
        LivingEntity mother,
        LivingEntity father,
        LivingEntity offspring,
        @Nullable LivingEntity breeder,
        ItemStack bredWith
) implements FabricBreedingEventHandle {
}
