package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface FabricBucketCatchEventHandle extends FabricEntityEventHandle, FabricTimeAwareItemEventHandle {

    ServerLevel level();

    ServerPlayer player();

    LivingEntity bucketedEntity();

    ItemStack originalBucket();

    ItemStack entityBucket();

    @Override
    default LivingEntity entity() {
        return bucketedEntity();
    }

    @Override
    default ItemStack itemStack() {
        return originalBucket();
    }

    @Override
    default ItemStack itemStack(int time) {
        return time == 1 ? entityBucket() : originalBucket();
    }
}
