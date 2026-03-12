package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class SkriptFabricEventBridgeMoveOnTest {

    @Test
    void moveOnBlockPosUsesSupportingBlockBelowFeet() {
        assertEquals(new BlockPos(3, 1, 7), SkriptFabricEventBridge.moveOnBlockPos(new Vec3(3.5D, 2.0D, 7.5D)));
        assertEquals(new BlockPos(3, 3, 7), SkriptFabricEventBridge.moveOnBlockPos(new Vec3(3.5D, 3.1D, 7.5D)));
    }
}
