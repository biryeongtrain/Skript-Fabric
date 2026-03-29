package org.skriptlang.skript.bukkit.particles;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public final class GameEffect {

    private final Identifier id;
    private @Nullable Object data;

    public GameEffect(Identifier id) {
        this.id = id;
    }

    public Identifier id() {
        return id;
    }

    public @Nullable Object data() {
        return data;
    }

    public void data(@Nullable Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return MinecraftResourceParser.display(id);
    }
}
