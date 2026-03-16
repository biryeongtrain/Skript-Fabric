package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Ignite/Extinguish")
@Description("Lights entities on fire or extinguishes them.")
@Example("ignite the player")
@Example("extinguish the player")
@Since("1.4")
public class EffIgnite extends Effect {

    static {
        Skript.registerEffect(
                EffIgnite.class,
                "(ignite|set fire to) %entities% [for %-timespan%]",
                "(set|light) %entities% on fire [for %-timespan%]",
                "extinguish %entities%"
        );
    }

    private static final int DEFAULT_DURATION = 8 * 20;

    private Expression<Entity> entities;
    private @Nullable Expression<Timespan> duration;
    private boolean ignite;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length == 0 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        ignite = matchedPattern != 2;
        if (ignite && expressions.length > 1) {
            duration = (Expression<Timespan>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        int fireTicks = ignite ? DEFAULT_DURATION : 0;
        if (duration != null) {
            Timespan timespan = duration.getSingle(event);
            if (timespan == null) {
                return;
            }
            fireTicks = (int) timespan.getAs(Timespan.TimePeriod.TICK);
        }
        for (Entity entity : entities.getAll(event)) {
            entity.setRemainingFireTicks(fireTicks);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (!ignite) {
            return "extinguish " + entities.toString(event, debug);
        }
        String suffix = duration == null
                ? new Timespan(Timespan.TimePeriod.TICK, DEFAULT_DURATION).toString()
                : duration.toString(event, debug);
        return "set " + entities.toString(event, debug) + " on fire for " + suffix;
    }
}
