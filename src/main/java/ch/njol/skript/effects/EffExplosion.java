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

@Name("Explosion")
@Description({
        "Creates an explosion of a given force.",
        "Starting with Bukkit 1.4.5 and Skript 2.0 you can use safe explosions which will damage entities but won't destroy any blocks."
})
@Example("create an explosion of force 10 at the player")
@Example("create an explosion of force 0 at the victim")
@Since("1.0")
public class EffExplosion extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffExplosion.class,
                "[(create|make)] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%] [(1¦with fire)]",
                "[(create|make)] [a] safe explosion (of|with) (force|strength|power) %number% [%directions% %locations%]",
                "[(create|make)] [a] fake explosion [%directions% %locations%]",
                "[(create|make)] [an] explosion[ ]effect [%directions% %locations%]"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        Skript.error("EffExplosion is blocked in the Fabric port until Direction compatibility is imported.");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "explosion";
    }
}
