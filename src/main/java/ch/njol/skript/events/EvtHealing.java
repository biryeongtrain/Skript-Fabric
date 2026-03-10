package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class EvtHealing extends SkriptEvent {

    private @Nullable Literal<EntityData<?>> entityDatas;
    private @Nullable Literal<String> healReasons;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtHealing.class)) {
            return;
        }
        Skript.registerEvent(
                EvtHealing.class,
                "heal[ing] [of %-entitydatas%] [(from|due to|by) %-strings%]",
                "%entitydatas% heal[ing] [(from|due to|by) %-strings%]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        entityDatas = args.length > 0 ? (Literal<EntityData<?>>) args[0] : null;
        healReasons = args.length > 1 ? (Literal<String>) args[1] : null;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (event.handle() instanceof FabricEventCompatHandles.Healing handle) {
            return matchesEntity(handle.entity()) && matchesReason(event, handle.reason());
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Healing.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "heal"
                + (entityDatas != null ? " of " + entityDatas.toString(event, debug) : "")
                + (healReasons != null ? " by " + healReasons.toString(event, debug) : "");
    }

    private boolean matchesEntity(Entity entity) {
        if (entityDatas == null) {
            return true;
        }
        for (EntityData<?> entityData : entityDatas.getAll(null)) {
            if (entityData != null && entityData.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesReason(org.skriptlang.skript.lang.event.SkriptEvent event, @Nullable String reason) {
        if (healReasons == null) {
            return true;
        }
        return reason != null && healReasons.check(event, candidate -> candidate.equalsIgnoreCase(reason));
    }
}
