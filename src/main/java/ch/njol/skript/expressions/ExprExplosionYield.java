package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprExplosionYield extends SimpleExpression<Number> {

    private static final @Nullable Class<?> EXPLOSION_PRIME_EVENT =
            ExpressionHandleSupport.resolveClass("ch.njol.skript.effects.FabricEffectEventHandles$ExplosionPrime");

    static {
        Skript.registerExpression(
                ExprExplosionYield.class,
                Number.class,
                "[the] explosion (yield|radius|size)",
                "[the] (yield|radius|size) of [the] explosion"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (EXPLOSION_PRIME_EVENT == null || !getParser().isCurrentEvent(EXPLOSION_PRIME_EVENT)) {
            Skript.error("The explosion radius is only usable in explosion prime events");
            return false;
        }
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        Object radius = ExpressionHandleSupport.invoke(event.handle(), "radius");
        return radius instanceof Number number ? new Number[]{number} : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Number radius = (Number) ExpressionHandleSupport.invoke(event.handle(), "radius");
        if (radius == null) {
            return;
        }
        float value = delta == null ? 0.0F : Math.max(0.0F, ((Number) delta[0]).floatValue());
        float updated = switch (mode) {
            case SET -> value;
            case ADD -> radius.floatValue() + value;
            case REMOVE -> Math.max(0.0F, radius.floatValue() - value);
            case DELETE -> 0.0F;
            default -> radius.floatValue();
        };
        ExpressionHandleSupport.set(event.handle(), "setRadius", updated);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the yield of the explosion";
    }
}
