package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Entity Visibility")
@Description({
        "Change visibility of the given entities for the given players.",
        "If no players are given, will hide the entities from all online players."
})
@Example("""
        on spawn:
            if event-entity is a chicken:
                hide event-entity
        """)
@Example("reveal hidden players of players")
@Since("2.3, 2.10 (entities)")
@RequiredPlugins("Minecraft 1.19+ (entities)")
public class EffEntityVisibility extends Effect {

    private static boolean registered;

    private boolean reveal;
    private Expression<Entity> hidden;
    private @Nullable Expression<ServerPlayer> viewers;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffEntityVisibility.class,
                "hide %entities% [(from|for) %-players%]",
                "reveal %entities% [(to|for|from) %-players%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
        reveal = matchedPattern == 1;
        hidden = (Expression<Entity>) exprs[0];
        viewers = exprs.length > 1 ? (Expression<ServerPlayer>) exprs[1] : null;
        Skript.error("Per-player entity visibility is not wired in the Fabric runtime yet.");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (reveal ? "reveal " : "hide ") + hidden.toString(event, debug)
                + (viewers == null ? "" : (reveal ? " to " : " from ") + viewers.toString(event, debug));
    }
}
