package org.skriptlang.skript.fabric.runtime;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public record FabricLootGenerateHandle(
        LootTable lootTable,
        List<ItemStack> loot,
        FabricLocation location,
        @Nullable ServerPlayer looter,
        @Nullable Entity lootedEntity
) implements FabricLootGenerateEventHandle {
}
