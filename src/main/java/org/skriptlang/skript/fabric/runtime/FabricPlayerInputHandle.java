package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Input;

public record FabricPlayerInputHandle(
        ServerLevel level,
        ServerPlayer player,
        Input previousInput,
        Input currentInput
) implements FabricPlayerInputEventHandle {
}
