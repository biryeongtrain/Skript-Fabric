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
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Detonate Entities")
@Description("Immediately detonates an entity. Accepted entities are fireworks, TNT minecarts, primed TNT, wind charges and creepers.")
@Example("detonate last launched firework")
@Since("2.10")
public class EffDetonate extends Effect {

    private static boolean registered;

    private Expression<Entity> entities;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffDetonate.class, "detonate %entities%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Entity entity : entities.getArray(event)) {
            if (entity instanceof FireworkRocketEntity firework) {
                EffectRuntimeSupport.invokeCompatible(firework, "explode");
            } else if (entity instanceof AbstractWindCharge windCharge) {
                EffectRuntimeSupport.invokeCompatible(windCharge, "explode");
            } else if (entity instanceof MinecartTNT minecart) {
                Object invoked = EffectRuntimeSupport.invokeCompatible(minecart, "explode");
                if (invoked == null) {
                    EffectRuntimeSupport.invokeCompatible(minecart, "primeFuse");
                }
            } else if (entity instanceof Creeper creeper) {
                Object invoked = EffectRuntimeSupport.invokeCompatible(creeper, "explodeCreeper");
                if (invoked == null) {
                    creeper.ignite();
                }
            } else if (entity instanceof PrimedTnt primedTnt) {
                EffectRuntimeSupport.invokeCompatible(primedTnt, new String[]{"setFuse", "setFuseTicks"}, 0);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "detonate " + entities.toString(event, debug);
    }
}
