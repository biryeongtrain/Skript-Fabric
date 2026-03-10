package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import java.lang.reflect.Field;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Allay Duplication Cooldown")
@Description({
        "The cooldown time until an allay can duplicate again naturally.",
        "Resetting the cooldown time will set the cooldown time to the same amount of time after an allay has duplicated."
})
@Example("set {_time} to the duplicate cooldown of last spawned allay")
@Example("add 5 seconds to the duplication cool down time of last spawned allay")
@Example("remove 3 seconds from the duplicating cooldown time of last spawned allay")
@Example("clear the clone cool down of last spawned allay")
@Example("reset the cloning cool down time of last spawned allay")
@Since("2.11")
public class ExprDuplicateCooldown extends SimplePropertyExpression<LivingEntity, Timespan> {

    private static final Field DUPLICATION_COOLDOWN = findField();
    private static final long DEFAULT_DUPLICATION_COOLDOWN = 6000L;

    static {
        registerDefault(ExprDuplicateCooldown.class, Timespan.class, "(duplicat(e|ing|ion)|clon(e|ing)) cool[ ]down [time]", "livingentities");
    }

    @Override
    public @Nullable Timespan convert(LivingEntity entity) {
        if (!(entity instanceof Allay allay)) {
            return null;
        }
        try {
            return new Timespan(TimePeriod.TICK, DUPLICATION_COOLDOWN.getLong(allay));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read allay duplication cooldown.", exception);
        }
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Timespan.class};
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        long ticks = delta == null ? 0L : ((Timespan) delta[0]).getAs(TimePeriod.TICK);
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (!(entity instanceof Allay allay)) {
                continue;
            }
            try {
                long current = DUPLICATION_COOLDOWN.getLong(allay);
                long next = switch (mode) {
                    case SET, DELETE -> ticks;
                    case ADD -> current + ticks;
                    case REMOVE -> current - ticks;
                    case RESET -> DEFAULT_DUPLICATION_COOLDOWN;
                };
                DUPLICATION_COOLDOWN.setLong(allay, Math.max(0L, next));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to change allay duplication cooldown.", exception);
            }
        }
    }

    @Override
    public Class<Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "duplicate cooldown time";
    }

    private static Field findField() {
        try {
            Field field = Allay.class.getDeclaredField("duplicationCooldown");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access allay duplication cooldown.", exception);
        }
    }
}
