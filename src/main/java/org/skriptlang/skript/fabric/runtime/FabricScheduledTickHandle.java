package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public record FabricScheduledTickHandle(
        @Nullable ServerLevel level,
        long tick,
        long dayTime
) {
}
