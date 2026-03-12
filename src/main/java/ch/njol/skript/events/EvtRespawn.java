package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtRespawn extends SkriptEvent {

    private static final @Nullable Class<?> PLAYER_RESPAWN_EFFECT_EVENT_CLASS = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn");

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtRespawn.class)) {
            return;
        }
        Skript.registerEvent(EvtRespawn.class, "[player] respawn[ing]");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        Object handle = event.handle();
        return handle instanceof FabricEventCompatHandles.PlayerRespawn
                || PLAYER_RESPAWN_EFFECT_EVENT_CLASS != null && PLAYER_RESPAWN_EFFECT_EVENT_CLASS.isInstance(handle);
    }

    @Override
    public Class<?>[] getEventClasses() {
        if (PLAYER_RESPAWN_EFFECT_EVENT_CLASS == null) {
            return new Class<?>[]{FabricEventCompatHandles.PlayerRespawn.class};
        }
        return new Class<?>[]{FabricEventCompatHandles.PlayerRespawn.class, PLAYER_RESPAWN_EFFECT_EVENT_CLASS};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "respawn";
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
