package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface FabricBreedingEventHandle extends FabricItemEventHandle {

    LivingEntity mother();

    LivingEntity father();

    LivingEntity offspring();

    @Nullable LivingEntity breeder();

    ItemStack bredWith();

    @Override
    default ItemStack itemStack() {
        return bredWith();
    }
}
