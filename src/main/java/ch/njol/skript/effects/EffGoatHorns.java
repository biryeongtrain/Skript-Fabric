package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Goat Horns")
@Description("Make a goat have or not have a left, right, or both horns.")
@Example("remove the left horn of last spawned goat")
@Example("regrow {_goat}'s horns")
@Example("remove both horns of all goats")
@Since("2.11")
public class EffGoatHorns extends Effect {

    public enum GoatHorn {
        LEFT, RIGHT, BOTH, ANY
    }

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private GoatHorn goatHorn = GoatHorn.LEFT;
    private boolean remove;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffGoatHorns.class,
                "remove [the] (left horn[s]|right:right horn[s]|both:both horns) of %livingentities%",
                "remove %livingentities%'[s] (left horn[s]|right:right horn[s]|both:horns)",
                "(regrow|replace) [the] (left horn[s]|right:right horn[s]|both:both horns) of %livingentities%",
                "(regrow|replace) %livingentities%'[s] (left horn[s]|right:right horn[s]|both:horns)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (parseResult.hasTag("right")) {
            goatHorn = GoatHorn.RIGHT;
        } else if (parseResult.hasTag("both")) {
            goatHorn = GoatHorn.BOTH;
        }
        entities = (Expression<LivingEntity>) exprs[0];
        remove = matchedPattern <= 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getArray(event)) {
            if (!(entity instanceof Goat goat)) {
                continue;
            }
            boolean hasHorn = !remove;
            if (goatHorn != GoatHorn.RIGHT) {
                EffectRuntimeSupport.setBooleanField(goat, "hasLeftHorn", hasHorn);
                EffectRuntimeSupport.invokeCompatible(goat, "addHorns");
            }
            if (goatHorn != GoatHorn.LEFT) {
                EffectRuntimeSupport.setBooleanField(goat, "hasRightHorn", hasHorn);
                EffectRuntimeSupport.invokeCompatible(goat, "addHorns");
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(remove ? "remove" : "regrow");
        builder.append(switch (goatHorn) {
            case LEFT -> "the left horn";
            case RIGHT -> "the right horn";
            case BOTH -> "both horns";
            case ANY -> "any horn";
        });
        builder.append("of", entities);
        return builder.toString();
    }
}
