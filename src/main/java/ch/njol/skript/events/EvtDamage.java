package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricDamageEventHandle;

@SuppressWarnings("unchecked")
public class EvtDamage extends SkriptEvent {

    private static boolean registered;

    private @Nullable Literal<EntityData<?>> ofTypes;
    private @Nullable Literal<EntityData<?>> byTypes;

    public static synchronized void register() {
        EntityData.register();
        if (registered) {
            return;
        }
        Skript.registerEvent(EvtDamage.class, "damag(e|ing) [of %-entitydata%] [by %-entitydata%]");
        registered = true;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        ofTypes = args.length > 0 ? (Literal<EntityData<?>>) args[0] : null;
        byTypes = args.length > 1 ? (Literal<EntityData<?>>) args[1] : null;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricDamageEventHandle handle)) {
            return false;
        }
        if (!matches(ofTypes, handle.entity())) {
            return false;
        }
        if (byTypes == null) {
            return true;
        }
        Entity causingEntity = handle.damageSource().getEntity();
        return causingEntity != null && matches(byTypes, causingEntity);
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricDamageEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "damage"
                + (ofTypes != null ? " of " + ofTypes.toString(event, debug) : "")
                + (byTypes != null ? " by " + byTypes.toString(event, debug) : "");
    }

    private boolean matches(@Nullable Literal<EntityData<?>> types, Entity entity) {
        if (types == null) {
            return true;
        }
        for (EntityData<?> type : types.getAll(null)) {
            if (type != null && type.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }
}
