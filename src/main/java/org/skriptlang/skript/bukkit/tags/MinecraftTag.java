package org.skriptlang.skript.bukkit.tags;

import net.minecraft.resources.ResourceLocation;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public record MinecraftTag(ResourceLocation id, Target target) {

    public enum Target {
        ANY,
        ITEM,
        BLOCK,
        ENTITY
    }

    @Override
    public String toString() {
        return MinecraftResourceParser.display(id);
    }
}
