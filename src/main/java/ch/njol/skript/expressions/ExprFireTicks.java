package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.lang.reflect.Method;

@Name("Entity Fire Burn Duration")
@Description("How much time an entity will be burning for.")
@Example("send \"You will stop burning in %fire time of player%\"")
@Example("send the max burn time of target")
@Since("2.7, 2.10 (maximum)")
public class ExprFireTicks extends SimplePropertyExpression<Entity, Timespan> {

    static {
        register(ExprFireTicks.class, Timespan.class, "[:max[imum]] (burn[ing]|fire) (time|duration)", "entities");
    }

    private boolean max;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        max = parseResult.hasTag("max");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Timespan convert(Entity entity) {
        int ticks = max ? getFireImmuneTicks(entity) : Math.max(entity.getRemainingFireTicks(), 0);
        return new Timespan(TimePeriod.TICK, ticks);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (max) {
            return null;
        }
        return switch (mode) {
            case ADD, SET, RESET, DELETE, REMOVE -> new Class[]{Timespan.class};
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int change = delta == null || delta.length == 0 ? 0 : (int) ((Timespan) delta[0]).getAs(TimePeriod.TICK);
        for (Entity entity : getExpr().getArray(event)) {
            switch (mode) {
                case REMOVE -> entity.setRemainingFireTicks(entity.getRemainingFireTicks() - change);
                case ADD -> entity.setRemainingFireTicks(entity.getRemainingFireTicks() + change);
                case DELETE, RESET, SET -> entity.setRemainingFireTicks(change);
            }
        }
    }

    @Override
    public Class<Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "fire time";
    }

    private int getFireImmuneTicks(Entity entity) {
        try {
            Method method = Entity.class.getDeclaredMethod("getFireImmuneTicks");
            method.setAccessible(true);
            return (int) method.invoke(entity);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read fire immune ticks", e);
        }
    }
}
