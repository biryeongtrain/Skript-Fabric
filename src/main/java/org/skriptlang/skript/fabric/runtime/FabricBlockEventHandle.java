package org.skriptlang.skript.fabric.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface FabricBlockEventHandle {

    ServerLevel level();

    BlockPos position();
}
