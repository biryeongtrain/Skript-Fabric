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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Lightning")
@Description("Strike lightning at a given location. Can use 'lightning effect' to create a lightning that does not harm entities or start fires.")
@Example("strike lightning at the player")
@Example("strike lightning effect at the victim")
@Since("1.4")
public class EffLightning extends Effect {

    private static boolean registered;

    private Expression<FabricLocation> locations;
    private boolean effectOnly;
    @Nullable
    public static Entity lastSpawned;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffLightning.class, "(create|strike) lightning(1¦[ ]effect|) %locations%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        locations = (Expression<FabricLocation>) exprs[0];
        effectOnly = parseResult.mark == 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (FabricLocation location : locations.getArray(event)) {
            if (location.level() == null) continue;
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(location.level(), EntitySpawnReason.TRIGGERED);
            if (lightning == null) {
                continue;
            }
            lightning.setPos(location.position());
            lightning.setVisualOnly(effectOnly);
            location.level().addFreshEntity(lightning);
            lastSpawned = lightning;
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "strike lightning " + (effectOnly ? "effect " : "") + locations.toString(event, debug);
    }
}
