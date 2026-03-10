package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class EvtMoveOn extends SkriptEvent {

    private FabricItemType[] types = new FabricItemType[0];

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtMoveOn.class)) {
            return;
        }
        Skript.registerEvent(EvtMoveOn.class, "(step|walk)[ing] (on|over) %*itemtypes%");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        Literal<FabricItemType> literal = (Literal<FabricItemType>) args[0];
        types = literal.getAll(null);
        return types.length > 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.MoveOn handle)) {
            return false;
        }
        Item item = handle.blockState().getBlock().asItem();
        if (item == null) {
            return false;
        }
        for (FabricItemType type : types) {
            if (type != null && type.matches(new ItemStack(item, Math.max(1, type.amount())))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.MoveOn.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "walk on " + java.util.Arrays.toString(types);
    }
}
