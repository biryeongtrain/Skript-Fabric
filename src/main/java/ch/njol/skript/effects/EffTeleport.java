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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Teleport")
@Description({
        "Teleport an entity to a specific location.",
        "Teleport flags are settings to retain during a teleport."
})
@Example("teleport the player to {home::%uuid of player%}")
@Example("teleport the attacker to the victim")
@Since("1.0, 2.10 (flags)")
public class EffTeleport extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffTeleport.class, "[:force] teleport %entities% (to|%direction%) %location% [[while] retaining %-teleportflags%]");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Skript.error("EffTeleport is blocked in the Fabric port until Direction compatibility is imported.");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "teleport";
    }
}
