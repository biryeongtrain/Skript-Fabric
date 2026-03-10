package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

public final class EvtLeash extends SkriptEvent {

    private @Nullable EntityData<?>[] types;
    private FabricEventCompatHandles.LeashAction action;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtLeash.class)) {
            return;
        }
        Skript.registerEvent(EvtLeash.class, "[:player] [:un]leash[ing] [of %-entitydatas%]");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        types = args[0] == null ? null : ((Literal<EntityData<?>>) args[0]).getAll(null);
        action = FabricEventCompatHandles.LeashAction.LEASH;
        if (parseResult.hasTag("un")) {
            action = parseResult.hasTag("player")
                    ? FabricEventCompatHandles.LeashAction.PLAYER_UNLEASH
                    : FabricEventCompatHandles.LeashAction.UNLEASH;
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Leash handle) || handle.action() != action) {
            return false;
        }
        if (types == null) {
            return true;
        }
        for (EntityData<?> entityData : types) {
            if (entityData != null && entityData.isInstance(handle.entity())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Leash.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return action.toString().toLowerCase().replace('_', ' ')
                + (types != null ? " of " + Classes.toString(types, false) : "");
    }
}
