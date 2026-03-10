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
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Drop")
@Description("Drops one or more items.")
@Example("""
        on death of creeper:
            drop 1 TNT
        """)
@Since("1.0")
public class EffDrop extends Effect {

    private static boolean registered;

    @Nullable
    public static Entity lastSpawned = null;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffDrop.class, "drop %itemtypes/experiences% [%directions% %locations%] [(1¦without velocity)]");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Skript.error("EffDrop is blocked in the Fabric port until Direction compatibility is imported.");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "drop";
    }
}
