package ch.njol.skript.events;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.registrations.Classes;

final class EventClassInfoRegistrar {

    private static boolean registered;

    private EventClassInfoRegistrar() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        EntityData.register();
        registered = true;
    }

    private static <T extends Enum<T>> void registerEnum(Class<T> type, String codeName, String languageNode) {
        if (Classes.getExactClassInfo(type) != null) {
            return;
        }
        Classes.registerClassInfo(new EnumClassInfo<>(type, codeName, languageNode));
    }
}
