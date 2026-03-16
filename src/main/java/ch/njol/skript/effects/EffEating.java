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
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Force Eating")
@Description("Make a panda or horse type (horse, camel, donkey, llama, mule) start/stop eating.")
@Example("""
    if last spawned panda is eating:
        make last spawned panda stop eating
    """)
@Since("2.11")
public class EffEating extends Effect {

    static {
        Skript.registerEffect(
                EffEating.class,
                "make %livingentities% (:start|stop) eating",
                "force %livingentities% to (:start|stop) eating"
        );
    }

    private Expression<LivingEntity> entities;
    private boolean start;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(LivingEntity.class)) {
            return false;
        }
        entities = (Expression<LivingEntity>) expressions[0];
        start = parseResult.hasTag("start");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getAll(event)) {
            if (entity instanceof Panda panda) {
                panda.eat(start);
            } else if (entity instanceof AbstractHorse horse) {
                horse.setEating(start);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("make", entities, start ? "start" : "stop", "eating");
        return builder.toString();
    }
}
