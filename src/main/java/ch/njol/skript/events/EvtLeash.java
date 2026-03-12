package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEntityUnleashHandle;

public final class EvtLeash extends SkriptEvent {

    private static final @Nullable Class<?> ENTITY_UNLEASH_EVENT_CLASS = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$EntityUnleash");
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
        if (parseResult.hasTag("player")) {
            action = parseResult.hasTag("un")
                    ? FabricEventCompatHandles.LeashAction.PLAYER_UNLEASH
                    : FabricEventCompatHandles.LeashAction.PLAYER_LEASH;
        } else {
            action = parseResult.hasTag("un")
                    ? FabricEventCompatHandles.LeashAction.UNLEASH
                    : FabricEventCompatHandles.LeashAction.LEASH;
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (event.handle() instanceof FabricEventCompatHandles.Leash handle) {
            if (handle.action() != action) {
                return false;
            }
            return matchesTypes(handle.entity());
        }
        if (!(event.handle() instanceof FabricEntityUnleashHandle handle)) {
            return false;
        }
        if (action == FabricEventCompatHandles.LeashAction.LEASH) {
            return false;
        }
        if (action == FabricEventCompatHandles.LeashAction.PLAYER_UNLEASH && !(handle.actor() instanceof ServerPlayer)) {
            return false;
        }
        return matchesTypes(handle.entity());
    }

    @Override
    public Class<?>[] getEventClasses() {
        if (ENTITY_UNLEASH_EVENT_CLASS == null) {
            return new Class<?>[]{FabricEventCompatHandles.Leash.class};
        }
        return new Class<?>[]{FabricEventCompatHandles.Leash.class, ENTITY_UNLEASH_EVENT_CLASS};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return action.toString().toLowerCase().replace('_', ' ')
                + (types != null ? " of " + Classes.toString(types, false) : "");
    }

    private boolean matchesTypes(Entity entity) {
        if (types == null) {
            return true;
        }
        for (EntityData<?> entityData : types) {
            if (entityData != null && entityData.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
