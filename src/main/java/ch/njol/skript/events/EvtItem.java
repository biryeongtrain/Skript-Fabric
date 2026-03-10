package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class EvtItem extends SkriptEvent {

    private @Nullable Literal<FabricItemType> types;
    private boolean entity;
    private FabricEventCompatHandles.ItemAction action;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtItem.class)) {
            return;
        }
        Skript.registerEvent(
                EvtItem.class,
                "dispens(e|ing) [[of] %-itemtypes%]",
                "item spawn[ing] [[of] %-itemtypes%]",
                "[player|1:entity] drop[ping] [[of] %-itemtypes%]",
                "[player] (preparing|beginning) craft[ing] [[of] %-itemtypes%]",
                "[player] craft[ing] [[of] %-itemtypes%]",
                "[(player|1¦entity)] (pick[ ]up|picking up) [[of] %-itemtypes%]",
                "[player] ((eat|drink)[ing]|consum(e|ing)) [[of] %-itemtypes%]",
                "[player] inventory(-| )click[ing] [[at] %-itemtypes%]",
                "(item[ ][stack]|[item] %-itemtypes%) despawn[ing]",
                "[item[ ][stack]] despawn[ing] [[of] %-itemtypes%]",
                "(item[ ][stack]|[item] %-itemtypes%) merg(e|ing)",
                "item[ ][stack] merg(e|ing) [[of] %-itemtypes%]",
                "inventory item (move|transport)",
                "inventory (mov(e|ing)|transport[ing]) [an] item",
                "stonecutting [[of] %-itemtypes%]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        types = findTypes(args);
        entity = parser.mark == 1;
        action = switch (matchedPattern) {
            case 0 -> FabricEventCompatHandles.ItemAction.DISPENSE;
            case 1 -> FabricEventCompatHandles.ItemAction.SPAWN;
            case 2 -> FabricEventCompatHandles.ItemAction.DROP;
            case 3 -> FabricEventCompatHandles.ItemAction.PREPARE_CRAFT;
            case 4 -> FabricEventCompatHandles.ItemAction.CRAFT;
            case 5 -> FabricEventCompatHandles.ItemAction.PICKUP;
            case 6 -> FabricEventCompatHandles.ItemAction.CONSUME;
            case 7 -> FabricEventCompatHandles.ItemAction.INVENTORY_CLICK;
            case 8, 9 -> FabricEventCompatHandles.ItemAction.DESPAWN;
            case 10, 11 -> FabricEventCompatHandles.ItemAction.MERGE;
            case 12, 13 -> FabricEventCompatHandles.ItemAction.INVENTORY_MOVE;
            default -> FabricEventCompatHandles.ItemAction.STONECUTTING;
        };
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Item handle) || handle.action() != action) {
            return false;
        }
        if ((action == FabricEventCompatHandles.ItemAction.DROP || action == FabricEventCompatHandles.ItemAction.PICKUP)
                && entity != handle.entityEvent()) {
            return false;
        }
        if (types == null) {
            return true;
        }
        ItemStack itemStack = handle.itemStack();
        return itemStack != null && types.check(event, itemType -> itemType.matches(itemStack));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Item.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(switch (action) {
            case DISPENSE -> "dispense";
            case SPAWN -> "item spawn";
            case DROP -> entity ? "entity drop" : "drop";
            case PREPARE_CRAFT -> "prepare craft";
            case CRAFT -> "craft";
            case PICKUP -> entity ? "entity pickup" : "pickup";
            case CONSUME -> "consume";
            case INVENTORY_CLICK -> "inventory click";
            case DESPAWN -> "item despawn";
            case MERGE -> "item merge";
            case INVENTORY_MOVE -> "inventory item move";
            case STONECUTTING -> "stonecutting";
        });
        if (types != null) {
            builder.append("of", types);
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Literal<FabricItemType> findTypes(Literal<?>[] args) {
        for (Literal<?> arg : args) {
            if (arg != null) {
                return (Literal<FabricItemType>) arg;
            }
        }
        return null;
    }
}
