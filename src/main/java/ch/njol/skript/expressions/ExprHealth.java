package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * @author Peter Guttiger
 */
@Name("Health")
@Description("The health of a creature, e.g. a player, mob, villager, etc. The minimum value is 0, and the maximum is the creature's max health (e.g. 10 for players).")
@Example("message \"You have %health% HP left.\"")
@Since("1.0")
@Events("damage")
public class ExprHealth extends PropertyExpression<Entity, Number> {

    static {
        register(ExprHealth.class, Number.class, "health", "entities");
    }

    @Override
    protected Number[] get(SkriptEvent event, Entity[] source) {
        return get(source, entity -> {
            if (entity instanceof LivingEntity living) {
                return living.getHealth() / 2.0F;
            }
            return null;
        });
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the health of " + getExpr().toString(event, debug);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{Number.class};
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        double change = delta == null ? 0.0 : ((Number) delta[0]).doubleValue();
        for (Entity entity : getExpr().getArray(event)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            switch (mode) {
                case DELETE, SET -> living.setHealth(clampHealth(living, change));
                case REMOVE -> living.setHealth(clampHealth(living, living.getHealth() / 2.0 - change));
                case ADD -> living.setHealth(clampHealth(living, living.getHealth() / 2.0 + change));
                case RESET -> living.setHealth(living.getMaxHealth());
            }
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    private static float clampHealth(LivingEntity entity, double hearts) {
        double raw = hearts * 2.0;
        return (float) Math.max(0.0, Math.min(raw, entity.getMaxHealth()));
    }
}
