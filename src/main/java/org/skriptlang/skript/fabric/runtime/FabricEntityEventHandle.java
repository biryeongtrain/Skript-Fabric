package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface FabricEntityEventHandle {

    @Nullable Entity entity();
}
