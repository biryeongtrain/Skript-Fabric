package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtResourcePackResponse extends SkriptEvent {

    private @Nullable String state;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtResourcePackResponse.class)) {
            return;
        }
        Skript.registerEvent(
                EvtResourcePackResponse.class,
                "resource pack [request] response",
                "resource pack [request] accepted",
                "resource pack [request] declined",
                "resource pack [request] failed [download]",
                "resource pack [request] loaded"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        state = switch (matchedPattern) {
            case 1 -> "accepted";
            case 2 -> "declined";
            case 3 -> "failed_download";
            case 4 -> "successfully_loaded";
            default -> null;
        };
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.ResourcePackResponse handle
                && (state == null || state.equalsIgnoreCase(String.valueOf(handle.status())));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.ResourcePackResponse.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return state != null ? "resource pack " + state.replace('_', ' ') : "resource pack request response";
    }
}
