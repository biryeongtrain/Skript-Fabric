package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.jetbrains.annotations.Nullable;

public final class EvtPlayerArmorChange extends SkriptEvent {

    private @Nullable FabricEventCompatHandles.ArmorSlot slot;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtPlayerArmorChange.class)) {
            return;
        }
        Skript.registerEvent(
                EvtPlayerArmorChange.class,
                "[player] armo[u]r change[d] [of %-armorslot%]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        if (args.length > 0 && args[0] != null) {
            slot = ((Literal<FabricEventCompatHandles.ArmorSlot>) args[0]).getSingle(null);
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.PlayerArmorChange handle)) {
            return false;
        }
        return slot == null || slot == handle.slot();
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.PlayerArmorChange.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("armor change");
        if (slot != null) {
            builder.append("of").append(slot.name().toLowerCase());
        }
        return builder.toString();
    }
}
