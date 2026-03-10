package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import java.lang.reflect.Field;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Creeper Max Fuse Ticks")
@Description("The max fuse ticks that a creeper has.")
@Example("set target entity's max fuse ticks to 20 #1 second")
@Since("2.5")
public class ExprCreeperMaxFuseTicks extends SimplePropertyExpression<LivingEntity, Long> {

    private static final Field MAX_SWELL = findField();

    static {
        register(ExprCreeperMaxFuseTicks.class, Long.class, "[creeper] max[imum] fuse tick[s]", "livingentities");
    }

    @Override
    public Long convert(LivingEntity entity) {
        if (!(entity instanceof Creeper creeper)) {
            return 0L;
        }
        try {
            return (long) MAX_SWELL.getInt(creeper);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read creeper max fuse ticks.", exception);
        }
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
            if (!(entity instanceof Creeper creeper)) {
                continue;
            }
            int next = switch (mode) {
                case ADD -> (int) (convert(creeper) + change);
                case SET -> change;
                case DELETE -> 0;
                case RESET -> 30;
                case REMOVE -> (int) (convert(creeper) - change);
            };
            try {
                MAX_SWELL.setInt(creeper, Math.max(0, next));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to change creeper max fuse ticks.", exception);
            }
        }
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "creeper max fuse ticks";
    }

    private static Field findField() {
        try {
            Field field = Creeper.class.getDeclaredField("maxSwell");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access creeper max fuse ticks.", exception);
        }
    }
}
