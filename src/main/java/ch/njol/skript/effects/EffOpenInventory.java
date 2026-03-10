package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Open/Close Inventory")
@Description({"Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that they just opened.",
        "Please note that currently 'show' and 'open' have the same effect, but 'show' will eventually show an unmodifiable view of the inventory in the future."})
@Example("show the victim's inventory to the player")
@Example("open the player's inventory for the player")
@Since("2.0, 2.1.1 (closing), 2.2-Fixes-V10 (anvil), 2.4 (hopper, dropper, dispenser)")
public final class EffOpenInventory extends Effect {

    private static boolean registered;

    private boolean open;
    private @Nullable Expression<FabricInventory> inventory;
    private @Nullable String menuType;
    private Expression<ServerPlayer> players;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffOpenInventory.class,
                "close %players%'[s] inventory [view]",
                "close [the] inventory [view] (to|of|for) %players%",
                "open %inventory% (to|for) %players%",
                "open [a] (crafting table|workbench) (to|for) %players%",
                "open [a] chest (to|for) %players%",
                "open [a[n]] anvil (to|for) %players%",
                "open [a] hopper (to|for) %players%",
                "open [a] dropper (to|for) %players%",
                "open [a] dispenser (to|for) %players%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        open = matchedPattern > 1;
        if (matchedPattern == 2) {
            inventory = (Expression<FabricInventory>) exprs[0];
        } else if (matchedPattern > 2) {
            menuType = switch (matchedPattern) {
                case 3 -> "crafting";
                case 4 -> "chest";
                case 5 -> "anvil";
                case 6 -> "hopper";
                case 7 -> "dropper";
                case 8 -> "dispenser";
                default -> null;
            };
        }
        players = (Expression<ServerPlayer>) exprs[exprs.length - 1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (ServerPlayer player : players.getArray(event)) {
            if (!open) {
                player.closeContainer();
                continue;
            }
            if (inventory != null) {
                FabricInventory target = inventory.getSingle(event);
                if (target != null) {
                    openInventory(player, target);
                }
                continue;
            }
            openMenu(player);
        }
    }

    private void openMenu(ServerPlayer player) {
        if (menuType == null) {
            return;
        }
        FabricInventory.menu(menuType).open(player);
    }

    private void openInventory(ServerPlayer player, FabricInventory target) {
        target.open(player);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (!open) {
            return "close inventory view of " + players.toString(event, debug);
        }
        String openedThing = inventory != null
                ? inventory.toString(event, debug)
                : menuType != null ? menuType : "inventory";
        return "open " + openedThing + " to " + players.toString(event, debug);
    }
}
