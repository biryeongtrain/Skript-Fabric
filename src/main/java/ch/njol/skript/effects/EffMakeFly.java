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
import net.minecraft.world.entity.player.Abilities;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Fly")
@Description("Forces a player to start/stop flying.")
@Example("make player fly")
@Example("force all players to stop flying")
@Since("2.2-dev34")
public class EffMakeFly extends Effect {

    static {
        Skript.registerEffect(
                EffMakeFly.class,
                "force %players% to [(start|1¦stop)] fly[ing]",
                "make %players% (start|1¦stop) flying",
                "make %players% fly"
        );
    }

    private Expression<ServerPlayer> players;
    private boolean flying;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(ServerPlayer.class)) {
            return false;
        }
        players = (Expression<ServerPlayer>) expressions[0];
        flying = parseResult.mark != 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (ServerPlayer player : players.getAll(event)) {
            Abilities abilities = player.getAbilities();
            abilities.mayfly = flying;
            abilities.flying = flying;
            player.onUpdateAbilities();
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + players.toString(event, debug) + (flying ? " start " : " stop ") + "flying";
    }
}
