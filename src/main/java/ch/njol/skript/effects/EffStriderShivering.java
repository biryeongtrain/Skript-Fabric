package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Strider;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffStriderShivering extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean start;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffStriderShivering.class,
                "make %livingentities% start shivering",
                "force %livingentities% to start shivering",
                "make %livingentities% stop shivering",
                "force %livingentities% to stop shivering"
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
        start = matchedPattern <= 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getAll(event)) {
            if (entity instanceof Strider strider) {
                strider.setSuffocating(start);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + entities.toString(event, debug) + ' ' + (start ? "start" : "stop") + " shivering";
    }
}
