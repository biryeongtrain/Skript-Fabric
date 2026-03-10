package ch.njol.skript.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Leash entities")
@Description({
    "Leash living entities to other entities.",
    "Ender dragons, withers, players, and bats still ignore the leash request through the underlying entity API."
})
@Example("""
    on right click:
        leash event-entity to player
        send "&aYou leashed &2%event-entity%!" to player
    """)
@Since("2.3")
public class EffLeash extends Effect {

    private Expression<LivingEntity> targets;
    private @Nullable Expression<Entity> holder;
    private boolean leash;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        leash = matchedPattern != 2;
        if (leash) {
            if (matchedPattern == 0) {
                if (!expressions[0].canReturn(LivingEntity.class) || !expressions[1].canReturn(Entity.class)) {
                    return false;
                }
                targets = (Expression<LivingEntity>) expressions[0];
                holder = (Expression<Entity>) expressions[1];
            } else {
                if (!expressions[0].canReturn(Entity.class) || !expressions[1].canReturn(LivingEntity.class)) {
                    return false;
                }
                holder = (Expression<Entity>) expressions[0];
                targets = (Expression<LivingEntity>) expressions[1];
            }
            return true;
        }
        if (!expressions[0].canReturn(LivingEntity.class)) {
            return false;
        }
        targets = (Expression<LivingEntity>) expressions[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (leash) {
            Entity leashHolder = holder == null ? null : holder.getSingle(event);
            if (leashHolder == null) {
                return;
            }
            for (LivingEntity target : targets.getAll(event)) {
                if (target instanceof Mob mob) {
                    mob.setLeashedTo(leashHolder, true);
                }
            }
            return;
        }
        for (LivingEntity target : targets.getAll(event)) {
            if (target instanceof Mob mob) {
                mob.dropLeash();
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (!leash) {
            return "unleash " + targets.toString(event, debug);
        }
        return "leash " + targets.toString(event, debug) + " to " + (holder == null ? "null" : holder.toString(event, debug));
    }
}
