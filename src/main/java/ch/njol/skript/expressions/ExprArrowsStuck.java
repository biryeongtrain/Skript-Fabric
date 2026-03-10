package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Arrows Stuck")
@Description("The number of arrows stuck in a living entity.")
@Example("set arrows stuck in player to 5")
@Since("2.5")
public class ExprArrowsStuck extends SimplePropertyExpression<LivingEntity, Long> {

    static {
        register(ExprArrowsStuck.class, Long.class, "arrow[s] stuck in", "livingentities");
    }

    @Override
    public Long convert(LivingEntity entity) {
        return (long) entity.getArrowCount();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, SET, DELETE, RESET, REMOVE -> new Class[]{Number.class};
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int change = delta == null ? 0 : ((Number) delta[0]).intValue();
        for (LivingEntity entity : getExpr().getArray(event)) {
            switch (mode) {
                case ADD -> entity.setArrowCount(Math.max(0, entity.getArrowCount() + change));
                case SET -> entity.setArrowCount(Math.max(0, change));
                case DELETE, RESET -> entity.setArrowCount(0);
                case REMOVE -> entity.setArrowCount(Math.max(0, entity.getArrowCount() - change));
            }
        }
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "arrows stuck";
    }
}
