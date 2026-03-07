package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record FabricLoveModeEnterHandle(
        ServerLevel level,
        LivingEntity entity,
        @Nullable ServerPlayer player
) implements FabricLoveModeEnterEventHandle {
}
