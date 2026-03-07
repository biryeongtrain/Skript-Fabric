package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public record FabricAttackEntityHandle(
        ServerLevel level,
        ServerPlayer player,
        InteractionHand hand,
        Entity entity,
        @Nullable EntityHitResult hitResult
) implements FabricEntityEventHandle {
}
