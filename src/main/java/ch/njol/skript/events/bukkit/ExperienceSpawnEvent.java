package ch.njol.skript.events.bukkit;

import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.util.event.Event;

public class ExperienceSpawnEvent implements Event {

    private final int spawnedXP;
    private final FabricLocation location;

    public ExperienceSpawnEvent(int spawnedXP, FabricLocation location) {
        this.spawnedXP = spawnedXP;
        this.location = location;
    }

    public int getSpawnedXP() {
        return spawnedXP;
    }

    public FabricLocation getLocation() {
        return location;
    }
}
