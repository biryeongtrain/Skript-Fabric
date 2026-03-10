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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Explode Creeper")
@Description("Starts the explosion process of a creeper or instantly explodes it.")
@Example("start explosion of the last spawned creeper")
@Example("stop ignition of the last spawned creeper")
@Since("2.5")
public class EffExplodeCreeper extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean instant;
    private boolean stop;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffExplodeCreeper.class,
                "instantly explode [creeper[s]] %livingentities%",
                "explode [creeper[s]] %livingentities% instantly",
                "ignite creeper[s] %livingentities%",
                "start (ignition|explosion) [process] of [creeper[s]] %livingentities%",
                "stop (ignition|explosion) [process] of [creeper[s]] %livingentities%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        instant = matchedPattern <= 1;
        stop = matchedPattern == 4;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getArray(event)) {
            if (!(entity instanceof Creeper creeper)) {
                continue;
            }
            if (instant) {
                Object invoked = EffectRuntimeSupport.invokeCompatible(creeper, "explode");
                if (invoked == null) {
                    creeper.ignite();
                }
            } else if (stop) {
                EffectRuntimeSupport.setBooleanField(creeper, "ignited", false);
            } else {
                creeper.ignite();
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (instant ? "instantly explode " : stop ? "stop the explosion process of " : "start the explosion process of ")
                + entities.toString(event, debug);
    }
}
