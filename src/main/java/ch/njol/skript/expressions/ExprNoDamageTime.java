package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("No Damage Time")
@Description("The amount of time an entity is invulnerable to any damage.")
@Example("""
    on damage:
        set victim's invulnerability time to 20 ticks #Victim will not take damage for the next second
    """)
@Example("""
    if the no damage timespan of {_entity} is 0 seconds:
        set the invincibility time span of {_entity} to 1 minute
    """)
@Since("2.11")
public class ExprNoDamageTime extends SimplePropertyExpression<LivingEntity, Timespan> {

    static {
        registerDefault(ExprNoDamageTime.class, Timespan.class, "(invulnerability|invincibility|no damage) time[[ ]span]", "livingentities");
    }

    @Override
    public Timespan convert(LivingEntity entity) {
        return new Timespan(TimePeriod.TICK, entity.invulnerableTime);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET, ADD, REMOVE -> new Class[]{Timespan.class};
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int provided = delta != null && delta[0] instanceof Timespan timespan ? (int) timespan.getAs(TimePeriod.TICK) : 0;
        for (LivingEntity entity : getExpr().getArray(event)) {
            switch (mode) {
                case SET, DELETE, RESET -> entity.invulnerableTime = Math.max(0, provided);
                case ADD -> entity.invulnerableTime = Math.max(0, entity.invulnerableTime + provided);
                case REMOVE -> entity.invulnerableTime = Math.max(0, entity.invulnerableTime - provided);
            }
        }
    }

    @Override
    public Class<Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "no damage timespan";
    }
}
