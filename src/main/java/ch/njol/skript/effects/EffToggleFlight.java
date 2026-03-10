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

@Name("Toggle Flight")
@Description("Toggle the <a href='#ExprFlightMode'>flight mode</a> of a player.")
@Example("allow flight to event-player")
@Since("2.3")
public class EffToggleFlight extends Effect {

    private static boolean registered;

    private Expression<ServerPlayer> players;
    private boolean allow;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffToggleFlight.class,
                "(allow|enable) (fly|flight) (for|to) %players%",
                "(disallow|disable) (fly|flight) (for|to) %players%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        allow = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (ServerPlayer player : players.getArray(event)) {
            player.getAbilities().mayfly = allow;
            if (!allow) {
                player.getAbilities().flying = false;
            }
            player.onUpdateAbilities();
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (allow ? "allow" : "disallow") + " flight to " + players.toString(event, debug);
    }
}
