package org.skriptlang.skript.bukkit.loottables;

import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public final class LootTable {

    private final ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key;

    public LootTable(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key) {
        this.key = key;
    }

    public static LootTable fromId(Identifier id) {
        return new LootTable(ResourceKey.create(Registries.LOOT_TABLE, id));
    }

    public ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key() {
        return key;
    }

    public Identifier id() {
        return key.identifier();
    }

    public @Nullable net.minecraft.world.level.storage.loot.LootTable resolve(@Nullable MinecraftServer server) {
        if (server == null) {
            return null;
        }
        return server.reloadableRegistries().getLootTable(key);
    }

    @Override
    public String toString() {
        return MinecraftResourceParser.display(id());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LootTable lootTable)) {
            return false;
        }
        return key.equals(lootTable.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
