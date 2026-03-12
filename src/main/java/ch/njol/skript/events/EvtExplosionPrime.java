package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricExplosionPrimeEventHandle;

public final class EvtExplosionPrime extends SkriptEvent {

    private static final @Nullable Class<?> EXPLOSION_PRIME_EVENT_CLASS = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$ExplosionPrime");

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtExplosionPrime.class)) {
            return;
        }
        Skript.registerEvent(EvtExplosionPrime.class, "explosion prime");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricExplosionPrimeEventHandle;
    }

    @Override
    public Class<?>[] getEventClasses() {
        if (EXPLOSION_PRIME_EVENT_CLASS == null) {
            return new Class<?>[]{FabricExplosionPrimeEventHandle.class};
        }
        return new Class<?>[]{EXPLOSION_PRIME_EVENT_CLASS, FabricExplosionPrimeEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "explosion prime";
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
