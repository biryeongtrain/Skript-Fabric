package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtResourcePackResponse extends SkriptEvent {

    private @Nullable FabricEventCompatHandles.ResourcePackState state;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtResourcePackResponse.class)) {
            return;
        }
        Skript.registerEvent(
                EvtResourcePackResponse.class,
                "resource pack [request] response",
                "resource pack [request] %resourcepackstates%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        if (matchedPattern == 1 && args.length > 0 && args[0] != null) {
            state = ((Literal<FabricEventCompatHandles.ResourcePackState>) args[0]).getSingle(null);
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.ResourcePackResponse handle
                && (state == null || state == handle.status());
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.ResourcePackResponse.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return state != null ? "resource pack " + state.name().toLowerCase().replace('_', ' ') : "resource pack request response";
    }
}
