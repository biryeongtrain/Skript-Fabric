package org.skriptlang.skript.fabric.runtime;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public interface FabricBrewingCompleteEventHandle extends FabricBlockEventHandle {

    BrewingStandBlockEntity brewingStand();

    List<ItemStack> results();
}
