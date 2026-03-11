package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("unchecked")
public class EvtTeleport extends SkriptEvent {

    private @Nullable Literal<?> entitiesLiteral;
    private EntityData<?> @Nullable [] entities;

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(EvtTeleport.class, "[%entitytypes%] teleport[ing]");
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtTeleport.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (args[0] != null) {
            entitiesLiteral = args[0];
            Object[] values = entitiesLiteral.getAll(null);
            entities = new EntityData<?>[values.length];
            for (int index = 0; index < values.length; index++) {
                Object value = values[index];
                if (value instanceof EntityType entityType) {
                    entities[index] = entityType.data;
                } else if (value instanceof EntityData<?> entityData) {
                    entities[index] = entityData;
                }
            }
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.Teleport handle)) {
            return false;
        }
        if (entities == null) {
            return true;
        }
        for (EntityData<?> entityData : entities) {
            if (entityData != null && entityData.isInstance(handle.entity())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.Teleport.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return entitiesLiteral != null ? "on " + entitiesLiteral.toString(event, debug) + " teleport" : "on teleport";
    }
}
