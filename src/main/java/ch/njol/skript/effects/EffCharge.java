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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Charge Entity")
@Description("Charges or uncharges a creeper or wither skull. A creeper is charged when it has been struck by lightning.")
@Example("""
        on spawn of creeper:
            charge the event-entity
        """)
@Since("2.5, 2.10 (wither skulls)")
public class EffCharge extends Effect {

    private static boolean registered;

    private Expression<Entity> entities;
    private boolean charge;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffCharge.class,
                "make %entities% [un:(un|not |non[-| ])](charged|powered)",
                "[:un](charge|power) %entities%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        charge = !parseResult.hasTag("un");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Entity entity : entities.getArray(event)) {
            if (entity instanceof Creeper creeper) {
                EffectRuntimeSupport.invokeCompatible(creeper, new String[]{"setPowered", "setCharged"}, charge);
            } else if (entity instanceof WitherSkull witherSkull) {
                EffectRuntimeSupport.invokeCompatible(witherSkull, new String[]{"setDangerous", "setCharged"}, charge);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + entities.toString(event, debug) + (charge ? " charged" : " not charged");
    }
}
