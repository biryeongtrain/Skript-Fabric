package ch.njol.skript.events;

import ch.njol.skript.Skript;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class EventSyntaxRegistry {

    private EventSyntaxRegistry() {
    }

    static boolean isRegistered(Class<?> eventClass) {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == eventClass) {
                return true;
            }
        }
        return false;
    }
}
