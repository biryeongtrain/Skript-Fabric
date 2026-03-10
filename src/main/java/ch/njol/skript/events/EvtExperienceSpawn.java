package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtExperienceSpawn extends SkriptEvent {

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtExperienceSpawn.class)) {
            return;
        }
        Skript.registerEvent(
                EvtExperienceSpawn.class,
                "[e]xp[erience] [orb] spawn",
                "spawn of [a[n]] [e]xp[erience] [orb]"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.ExperienceSpawn;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.ExperienceSpawn.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "experience spawn";
    }
}
