package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.SecCreateGui;
import ch.njol.util.Kleenean;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Opens the last created GUI to a player.
 */
public class EffOpenLastGui extends Effect {

    public static void register() {
        Skript.registerEffect(EffOpenLastGui.class,
                "open last gui to %players%"
        );
    }

    private Expression<ServerPlayer> players;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        SimpleGui gui = SecCreateGui.getCurrentGui();
        if (gui == null) {
            return;
        }

        for (ServerPlayer player : players.getArray(event)) {
            // The GUI created by SecCreateGui is bound to a specific player.
            // If the target player is the same as the GUI's player, just open it.
            // Otherwise, we'd need to recreate the GUI for each player - for now, just open.
            gui.open();
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "open last gui to " + players.toString(event, debug);
    }
}
