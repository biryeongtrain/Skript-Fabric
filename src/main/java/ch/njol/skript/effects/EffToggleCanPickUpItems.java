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
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Toggle Picking Up Items")
@Description("Determines whether living entities are able to pick up items or not")
@Example("forbid player from picking up items")
@Example("""
    on drop:
        if player can't pick up items:
            allow player to pick up items
    """)
@Since("2.8.0")
public class EffToggleCanPickUpItems extends Effect {

    static {
        Skript.registerEffect(
                EffToggleCanPickUpItems.class,
                "allow %livingentities% to pick([ ]up items| items up)",
                "(forbid|disallow) %livingentities% (from|to) pick([ing | ]up items|[ing] items up)"
        );
    }

    private Expression<LivingEntity> entities;
    private boolean allowPickUp;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(LivingEntity.class)) {
            return false;
        }
        entities = (Expression<LivingEntity>) expressions[0];
        allowPickUp = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getAll(event)) {
            if (entity instanceof Mob mob) {
                mob.setCanPickUpLoot(allowPickUp);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (allowPickUp) {
            return "allow " + entities.toString(event, debug) + " to pick up items";
        }
        return "forbid " + entities.toString(event, debug) + " from picking up items";
    }
}
