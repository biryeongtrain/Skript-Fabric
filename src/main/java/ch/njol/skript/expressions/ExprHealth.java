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
public class ExprHealth extends PropertyExpression<LivingEntity, Number> {

    static {
        register(ExprHealth.class, Number.class, "health", "livingentities");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        setExpr((Expression<LivingEntity>) vars[0]);
        return true;
    }

    @Override
    protected Number[] get(SkriptEvent event, LivingEntity[] source) {
        return get(source, entity -> entity.getHealth() / 2.0F);
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
        for (LivingEntity entity : getExpr().getArray(event)) {
            switch (mode) {
                case DELETE, SET -> entity.setHealth(clampHealth(entity, change));
                case REMOVE -> entity.setHealth(clampHealth(entity, entity.getHealth() / 2.0 - change));
                case ADD -> entity.setHealth(clampHealth(entity, entity.getHealth() / 2.0 + change));
                case RESET -> entity.setHealth(entity.getMaxHealth());
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
