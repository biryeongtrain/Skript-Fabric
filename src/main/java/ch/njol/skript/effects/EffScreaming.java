package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffScreaming extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean scream;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffScreaming.class,
                "make %livingentities% (start screaming|scream)",
                "force %livingentities% to (start screaming|scream)",
                "make %livingentities% stop screaming",
                "force %livingentities% to stop screaming"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(LivingEntity.class)) {
            return false;
        }
        entities = (Expression<LivingEntity>) expressions[0];
        scream = matchedPattern <= 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getAll(event)) {
            if (entity instanceof Goat goat) {
                goat.setScreamingGoat(scream);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + entities.toString(event, debug) + (scream ? " start " : " stop ") + "screaming";
    }
}
