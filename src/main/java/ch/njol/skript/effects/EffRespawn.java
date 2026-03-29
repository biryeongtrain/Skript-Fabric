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
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Force Respawn")
@Description("Forces player(s) to respawn if they are dead. If this is called without delay from death event, one tick is waited before respawn attempt.")
@Example("""
        on death of player:
            force event-player to respawn
        """)
@Since("2.2-dev21")
public class EffRespawn extends Effect {

    static {
        Skript.registerEffect(EffRespawn.class, "force %players% to respawn");
    }

    private Expression<ServerPlayer> players;
    private boolean forceDelay;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (getParser().isCurrentEvent(FabricEffectEventHandles.PlayerRespawn.class)) {
            Skript.error("Respawning the player in a respawn event is not possible");
            return false;
        }
        players = (Expression<ServerPlayer>) exprs[0];
        forceDelay = getParser().isCurrentEvent(FabricEffectEventHandles.EntityDeath.class) && isDelayed.isFalse();
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (ServerPlayer player : players.getArray(event)) {
            if (player.level().getServer() != null) {
                player.level().getServer().getPlayerList().respawn(player, false, net.minecraft.world.entity.Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "force " + players.toString(event, debug) + " to respawn";
    }
}
