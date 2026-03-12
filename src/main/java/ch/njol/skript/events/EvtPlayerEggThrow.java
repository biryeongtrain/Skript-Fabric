package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEggThrowEventHandle;

public final class EvtPlayerEggThrow extends SkriptEvent {

    private static final @Nullable Class<?> PLAYER_EGG_THROW_EVENT_CLASS = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$PlayerEggThrow");

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtPlayerEggThrow.class)) {
            return;
        }
        Skript.registerEvent(
                EvtPlayerEggThrow.class,
                "throw[ing] [of] [an] egg",
                "[player] egg throw"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEggThrowEventHandle;
    }

    @Override
    public Class<?>[] getEventClasses() {
        if (PLAYER_EGG_THROW_EVENT_CLASS == null) {
            return new Class<?>[]{FabricEggThrowEventHandle.class};
        }
        return new Class<?>[]{PLAYER_EGG_THROW_EVENT_CLASS, FabricEggThrowEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "player egg throw";
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
