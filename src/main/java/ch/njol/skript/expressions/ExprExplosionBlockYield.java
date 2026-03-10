package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprExplosionBlockYield extends SimpleExpression<Number> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(
                ExprExplosionBlockYield.class,
                Number.class,
                "[the] [explosion['s]] block (yield|amount)",
                "[the] percentage of blocks dropped"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.Explosion.class};
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Explosion handle)) {
            return null;
        }
        return new Number[]{handle.yield()};
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
        if (!(event.handle() instanceof FabricEventCompatHandles.Explosion handle)) {
            return;
        }
        float value = delta == null ? 0.0F : Math.max(0.0F, ((Number) delta[0]).floatValue());
        float updated = switch (mode) {
            case SET -> value;
            case ADD -> handle.yield() + value;
            case REMOVE -> Math.max(0.0F, handle.yield() - value);
            case DELETE -> 0.0F;
            default -> handle.yield();
        };
        handle.setYield(updated);
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
        return "the explosion's block yield";
    }
}
