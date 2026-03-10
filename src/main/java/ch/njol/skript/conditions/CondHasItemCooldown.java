package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Has Item Cooldown")
@Description("""
    Checks whether a cooldown is active on the specified item for a specific player.
    If the provided item has a cooldown group component specified, the cooldown group will take priority.
    Otherwise, the cooldown of the item material will be used.
    """)
@Example("""
    if player has player's tool on cooldown:
        send "You can't use this item right now. Wait %item cooldown of player's tool for player%"
    """)
@RequiredPlugins("MC 1.21.2 (cooldown group)")
@Since({"2.8.0", "2.12 (cooldown group)"})
public class CondHasItemCooldown extends Condition {

    static {
        Skript.registerCondition(
                CondHasItemCooldown.class,
                "%players% (has|have) [([an] item|a)] cooldown (on|for) %itemtypes%",
                "%players% (has|have) %itemtypes% on [(item|a)] cooldown",
                "%players% (doesn't|does not|do not|don't) have [([an] item|a)] cooldown (on|for) %itemtypes%",
                "%players% (doesn't|does not|do not|don't) have %itemtypes% on [(item|a)] cooldown"
        );
    }

    private Expression<ServerPlayer> players;
    private Expression<FabricItemType> itemTypes;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        itemTypes = (Expression<FabricItemType>) exprs[1];
        setNegated(matchedPattern > 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        FabricItemType[] requested = itemTypes.getArray(event);
        return players.check(event, player -> {
            if (itemTypes.getAnd()) {
                for (FabricItemType itemType : requested) {
                    if (player.getCooldowns().isOnCooldown(itemType.toStack())) {
                        return true;
                    }
                }
                return false;
            }
            for (FabricItemType itemType : requested) {
                if (!player.getCooldowns().isOnCooldown(itemType.toStack())) {
                    return false;
                }
            }
            return requested.length > 0;
        }, isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyType.HAVE, event, debug, players,
                itemTypes.toString(event, debug) + " on cooldown");
    }
}
