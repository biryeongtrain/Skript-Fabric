package org.skriptlang.skript.fabric.compat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record FabricLocation(ServerLevel level, Vec3 position) {
}
