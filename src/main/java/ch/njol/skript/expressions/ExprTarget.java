package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Target")
@Description("For mobs, the entity they are attacking or following.")
@Example("set target of last spawned zombie to player")
@Since("1.4.2")
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {

    static {
        Skript.registerExpression(
                ExprTarget.class,
                Entity.class,
                "[the] target [of %livingentities%]",
                "%livingentities%'[s] target",
                "[the] targeted %-*entitydata% [of %livingentities%]",
                "%livingentities%'[s] targeted %-*entitydata%"
        );
    }

    private @Nullable EntityData<?> type;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (matchedPattern < 2) {
            type = null;
            setExpr((Expression<? extends LivingEntity>) expressions[0]);
            return true;
        }
        if (matchedPattern == 2) {
            type = expressions[0] == null ? null : (EntityData<?>) expressions[0].getSingle(null);
            setExpr((Expression<? extends LivingEntity>) expressions[1]);
            return true;
        }
        type = expressions[1] == null ? null : (EntityData<?>) expressions[1].getSingle(null);
        setExpr((Expression<? extends LivingEntity>) expressions[0]);
        return true;
    }

    @Override
    protected Entity[] get(SkriptEvent event, LivingEntity[] source) {
        return get(source, entity -> {
            if (!(entity instanceof Mob mob)) {
                return null;
            }
            LivingEntity target = mob.getTarget();
            if (target == null || type != null && !type.isInstance(target)) {
                return null;
            }
            return target;
        });
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET -> new Class[]{LivingEntity.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        LivingEntity target = mode == ChangeMode.SET && delta != null ? (LivingEntity) delta[0] : null;
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (entity instanceof Mob mob) {
                mob.setTarget(target);
            }
        }
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return type != null ? type.getType() : Entity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "target" + (type == null ? "" : " " + type) + " of " + getExpr().toString(event, debug);
    }
}
