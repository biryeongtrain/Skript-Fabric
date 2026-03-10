package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Max Health")
@Description("The maximum health of an entity, e.g. 10 for a player.")
@Example("""
	on join:
		set the maximum health of the player to 100
	""")
@Example("""
	spawn a giant
	set the last spawned entity's max health to 1000
	""")
@Since("2.0")
@Events({"damage", "death"})
public class ExprMaxHealth extends SimplePropertyExpression<LivingEntity, Number> {

    static {
        register(ExprMaxHealth.class, Number.class, "max[imum] health", "livingentities");
    }

    @Override
    public Number convert(LivingEntity entity) {
        return entity.getMaxHealth() / 2.0F;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "max health";
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.DELETE ? null : new Class[]{Number.class};
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        double amount = delta == null ? 0.0 : ((Number) delta[0]).doubleValue();
        Object[] values = ((Expression<?>) getExpr()).getArray(event);
        for (Object value : values) {
            if (!(value instanceof LivingEntity entity)) {
                continue;
            }
            float current = entity.getMaxHealth() / 2.0F;
            float updated = switch (mode) {
                case SET -> (float) amount;
                case ADD -> (float) (current + amount);
                case REMOVE -> (float) (current - amount);
                case RESET -> (float) (entity.getAttribute(Attributes.MAX_HEALTH).getAttribute().value().getDefaultValue() / 2.0);
                default -> current;
            };
            entity.getAttribute(Attributes.MAX_HEALTH)
                    .setBaseValue(Math.max(0.0, updated * 2.0));
            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }
}
