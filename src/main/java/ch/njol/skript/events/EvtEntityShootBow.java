package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class EvtEntityShootBow extends SkriptEvent {

    private Literal<EntityData<?>> entityDatas;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtEntityShootBow.class)) {
            return;
        }
        Skript.registerEvent(EvtEntityShootBow.class, "%entitydatas% shoot[ing] (bow|projectile)");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        entityDatas = (Literal<EntityData<?>>) args[0];
        if (entityDatas instanceof LiteralList<EntityData<?>> list && list.getAnd()) {
            list.invertAnd();
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.EntityShootBow handle)) {
            return false;
        }
        LivingEntity entity = handle.entity();
        return entityDatas.check(event, entityData -> entityData.isInstance(entity));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.EntityShootBow.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return entityDatas.toString(event, debug) + " shoot bow";
    }
}
