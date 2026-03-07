package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record FabricBucketCatchHandle(
        ServerLevel level,
        ServerPlayer player,
        LivingEntity bucketedEntity,
        ItemStack originalBucket,
        ItemStack entityBucket
) implements FabricBucketCatchEventHandle {
}
