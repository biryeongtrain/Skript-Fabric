package ch.njol.skript.events;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.level.GameType;

final class EventClassInfoRegistrar {

    private static boolean registered;

    private EventClassInfoRegistrar() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        EntityData.register();
        registerEnum(GameType.class, "gamemode", "game modes");
        registerEnum(FabricEventCompatHandles.WeatherType.class, "weathertype", "weather types");
        registerEnum(FabricEventCompatHandles.ArmorSlot.class, "armorslot", "armor slots");
        registerEnum(FabricEventCompatHandles.ResourcePackState.class, "resourcepackstate", "resource pack states");
        registerEnum(SpawnReason.class, "spawnreason", "spawn reasons");
        registerEnum(TeleportCause.class, "teleportcause", "teleport causes");
        registered = true;
    }

    private static <T extends Enum<T>> void registerEnum(Class<T> type, String codeName, String languageNode) {
        if (Classes.getExactClassInfo(type) != null) {
            return;
        }
        Classes.registerClassInfo(new EnumClassInfo<>(type, codeName, languageNode));
    }
}
