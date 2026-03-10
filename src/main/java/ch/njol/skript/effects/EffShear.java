package ch.njol.skript.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Shear")
@Description("Shears or un-shears sheep on the current compatibility surface.")
@Example("""
    on rightclick on a sheep holding a sword:
        shear the clicked sheep
    """)
@Since("2.0 (cows, sheep & snowmen), 2.8.0 (all shearable entities)")
public class EffShear extends Effect {

    private Expression<LivingEntity> entities;
    private boolean force;
    private boolean shear;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(LivingEntity.class)) {
            return false;
        }
        entities = (Expression<LivingEntity>) expressions[0];
        force = parseResult.hasTag("force");
        shear = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getAll(event)) {
            if (entity instanceof Sheep sheep) {
                if (force || sheep.readyForShearing()) {
                    sheep.setSheared(shear);
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (shear ? "" : "un") + "shear " + entities.toString(event, debug);
    }
}
