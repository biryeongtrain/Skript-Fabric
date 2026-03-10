package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricBreedingEventHandle;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("unchecked")
public class EvtBreeding extends SimpleEvent {

    private static final String[] PATTERNS = {
            "[entity] breed[ing]",
            "[entity] breed[ing] [of] %-entitydatas%"
    };

    private @Nullable EntityData<?>[] entityTypes;

    public static synchronized void register() {
        EntityData.register();
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(EvtBreeding.class, PATTERNS);
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtBreeding.class) {
                return true;
            }
        }
        return false;
    }

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (matchedPattern == 0 || args.length == 0 || args[0] == null) {
            entityTypes = null;
            return true;
        }
        entityTypes = ((Literal<EntityData<?>>) args[0]).getAll(null);
        return entityTypes.length > 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricBreedingEventHandle handle)) {
            return false;
        }
        if (entityTypes == null || entityTypes.length == 0) {
            return true;
        }
        for (EntityData<?> entityType : entityTypes) {
            if (entityType != null && entityType.isInstance(handle.offspring())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricBreedingEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return entityTypes == null || entityTypes.length == 0
                ? "breeding"
                : "breeding of " + Classes.toString(entityTypes, false);
    }
}
