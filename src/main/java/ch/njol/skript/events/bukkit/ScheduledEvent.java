package ch.njol.skript.events.bukkit;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class ScheduledEvent {

    private final @Nullable ServerLevel world;

    public ScheduledEvent(@Nullable ServerLevel world) {
        this.world = world;
    }

    public final @Nullable ServerLevel getWorld() {
        return world;
    }
}
