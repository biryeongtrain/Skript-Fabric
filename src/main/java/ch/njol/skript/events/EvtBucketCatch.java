package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricBucketCatchEventHandle;

@SuppressWarnings("unchecked")
public class EvtBucketCatch extends SimpleEvent {

    private static final String[] PATTERNS = {
            "bucket catch",
            "bucket catch[ing]",
            "bucket captur(e|ing)",
            "bucket catch [of] %-entitydatas%",
            "bucket catch[ing] [of] %-entitydatas%",
            "bucket captur(e|ing) [of] %-entitydatas%"
    };

    private static boolean registered;

    private @Nullable EntityData<?>[] entityTypes;

    public static synchronized void register() {
        EntityData.register();
        if (registered) {
            return;
        }
        Skript.registerEvent(EvtBucketCatch.class, PATTERNS);
        registered = true;
    }

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (matchedPattern <= 2 || args.length == 0 || args[0] == null) {
            entityTypes = null;
            return true;
        }
        entityTypes = ((Literal<EntityData<?>>) args[0]).getAll(null);
        return entityTypes.length > 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricBucketCatchEventHandle handle)) {
            return false;
        }
        if (entityTypes == null || entityTypes.length == 0) {
            return true;
        }
        for (EntityData<?> entityType : entityTypes) {
            if (entityType != null && entityType.isInstance(handle.bucketedEntity())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricBucketCatchEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return entityTypes == null || entityTypes.length == 0
                ? "bucket catch"
                : "bucket catch of " + Classes.toString(entityTypes, false);
    }
}
