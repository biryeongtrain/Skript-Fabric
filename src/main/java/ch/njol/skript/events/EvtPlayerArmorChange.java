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
        if (EventSyntaxRegistry.isRegistered(EvtPlayerArmorChange.class)) {
            return;
        }
        Skript.registerEvent(
                EvtPlayerArmorChange.class,
                "[player] armo[u]r change[d]",
                "[player] helmet change[d]",
                "[player] chestplate change[d]",
                "[player] leggings change[d]",
                "[player] boots change[d]"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        slot = switch (matchedPattern) {
            case 1 -> FabricEventCompatHandles.ArmorSlot.HEAD;
            case 2 -> FabricEventCompatHandles.ArmorSlot.CHEST;
            case 3 -> FabricEventCompatHandles.ArmorSlot.LEGS;
            case 4 -> FabricEventCompatHandles.ArmorSlot.FEET;
            default -> null;
        };
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
        if (slot == null) {
            builder.append("armor change");
        } else {
            builder.append(slot.toString()).append("changed");
        }
        return builder.toString();
    }
}
