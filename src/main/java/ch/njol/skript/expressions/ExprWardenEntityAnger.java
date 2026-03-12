package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWardenEntityAnger extends SimpleExpression<Integer> {

    static {
        Skript.registerExpression(
                ExprWardenEntityAnger.class,
                Integer.class,
                "[the] anger level [of] %livingentities% towards %livingentities%",
                "%livingentities%'[s] anger level towards %livingentities%"
        );
    }

    private Expression<LivingEntity> wardens;
    private Expression<LivingEntity> targets;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        wardens = (Expression<LivingEntity>) exprs[0];
        targets = (Expression<LivingEntity>) exprs[1];
        return true;
    }

    @Override
    protected Integer @Nullable [] get(SkriptEvent event) {
        List<Integer> values = new ArrayList<>();
        Entity[] resolvedTargets = targets.getArray(event);
        for (LivingEntity livingEntity : wardens.getArray(event)) {
            if (!(livingEntity instanceof Warden warden)) {
                continue;
            }
            for (Entity target : resolvedTargets) {
                values.add(warden.getAngerManagement().getActiveAnger(target));
            }
        }
        return values.toArray(Integer[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, ADD, REMOVE -> new Class[]{Integer.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int value = delta != null && delta.length > 0 ? ((Number) delta[0]).intValue() : 0;
        BiConsumer<Warden, Entity> consumer = switch (mode) {
            case SET -> (warden, entity) -> applyAnger(warden, entity, value);
            case DELETE -> Warden::clearAnger;
            case ADD -> (warden, entity) -> applyAnger(warden, entity, warden.getAngerManagement().getActiveAnger(entity) + value);
            case REMOVE -> (warden, entity) -> applyAnger(warden, entity, warden.getAngerManagement().getActiveAnger(entity) - value);
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
        Entity[] resolvedTargets = targets.getArray(event);
        for (LivingEntity livingEntity : wardens.getArray(event)) {
            if (!(livingEntity instanceof Warden warden)) {
                continue;
            }
            for (Entity target : resolvedTargets) {
                consumer.accept(warden, target);
            }
        }
    }

    @Override
    public boolean isSingle() {
        return wardens.isSingle() && targets.isSingle();
    }

    private static void applyAnger(Warden warden, Entity entity, int value) {
        int clamped = (int) Math2.fit(0, value, 150);
        warden.clearAnger(entity);
        if (clamped <= 0) {
            return;
        }
        warden.increaseAngerAt(entity, clamped, false);
        if (entity instanceof LivingEntity livingEntity && clamped >= 80) {
            warden.setAttackTarget(livingEntity);
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the anger level of " + wardens.toString(event, debug) + " towards " + targets.toString(event, debug);
    }
}
