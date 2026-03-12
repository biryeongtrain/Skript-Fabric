package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public final class EvtEntity extends SkriptEvent {

    private static final @Nullable Class<?> ENTITY_DEATH_EVENT_CLASS = resolveEntityDeathEventClass();
    private @Nullable EntityData<?>[] types;
    private boolean spawn;

    private static @Nullable Class<?> resolveEntityDeathEventClass() {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$EntityDeath");
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtEntity.class)) {
            return;
        }
        Skript.registerEvent(
                EvtEntity.class,
                "death [of %-entitydatas%]",
                "spawn[ing] [of %-entitydatas%]"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        types = args[0] == null ? null : ((Literal<EntityData<?>>) args[0]).getAll(null);
        spawn = matchedPattern == 1;
        if (types != null) {
            for (EntityData<?> type : types) {
                if (type == null) {
                    continue;
                }
                if (spawn && Player.class.isAssignableFrom(type.getType())) {
                    Skript.error("The spawn event does not work for human entities");
                    return false;
                }
                if (!spawn && !LivingEntity.class.isAssignableFrom(type.getType())) {
                    Skript.error("The death event only works for living entities");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        Entity entity;
        if (spawn) {
            if (!(event.handle() instanceof FabricEventCompatHandles.EntityLifecycle handle) || !handle.spawn()) {
                return false;
            }
            entity = handle.entity();
        } else {
            if (!(event.handle() instanceof org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle handle)) {
                return false;
            }
            entity = handle.entity();
            if (!(entity instanceof LivingEntity)) {
                return false;
            }
        }
        if (types == null) {
            return true;
        }
        for (EntityData<?> type : types) {
            if (type != null && type.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        if (spawn || ENTITY_DEATH_EVENT_CLASS == null) {
            return new Class<?>[]{FabricEventCompatHandles.EntityLifecycle.class};
        }
        return new Class<?>[]{ENTITY_DEATH_EVENT_CLASS};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return (spawn ? "spawn" : "death") + (types == null ? "" : " of " + Classes.toString(types, false));
    }
}
