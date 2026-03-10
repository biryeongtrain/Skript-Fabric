package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class EvtEntityTransform extends SkriptEvent {

    private @Nullable Literal<String> reasons;
    private @Nullable Literal<EntityData<?>> datas;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtEntityTransform.class)) {
            return;
        }
        Skript.registerEvent(
                EvtEntityTransform.class,
                "(entit(y|ies)|%*-entitydatas%) transform[ing] [due to %-strings%]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        datas = args.length > 0 ? (Literal<EntityData<?>>) args[0] : null;
        reasons = args.length > 1 ? (Literal<String>) args[1] : null;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.EntityTransform handle)) {
            return false;
        }
        if (!matchesEntity(handle.entity())) {
            return false;
        }
        return reasons == null || (handle.reason() != null && reasons.check(event, reason -> reason.equalsIgnoreCase(handle.reason())));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.EntityTransform.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        if (datas == null) {
            return "entities transforming" + (reasons == null ? "" : " due to " + reasons.toString(event, debug));
        }
        return datas.toString(event, debug) + " transforming" + (reasons == null ? "" : " due to " + reasons.toString(event, debug));
    }

    private boolean matchesEntity(Entity entity) {
        if (datas == null) {
            return true;
        }
        for (EntityData<?> data : datas.getAll(null)) {
            if (data != null && data.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }
}
