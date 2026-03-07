package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFishingApproachAngle extends SimpleExpression<Float> {

    private static final float DEFAULT_MINIMUM_DEGREES = 0.0F;
    private static final float DEFAULT_MAXIMUM_DEGREES = 360.0F;

    private boolean minimum;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFishingEventHandle.class)) {
            Skript.error("The 'fishing approach angle' expression can only be used in a fishing event.");
            return false;
        }
        minimum = matchedPattern == 0;
        return true;
    }

    @Override
    protected Float @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return null;
        }
        return new Float[]{minimum ? FabricFishingState.minLureAngle(handle.hook()) : FabricFishingState.maxLureAngle(handle.hook())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Float.class, Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return;
        }
        float amount = mode == ChangeMode.RESET
                ? (minimum ? DEFAULT_MINIMUM_DEGREES : DEFAULT_MAXIMUM_DEGREES)
                : resolveFloat(delta);
        var hook = handle.hook();
        float current = minimum ? FabricFishingState.minLureAngle(hook) : FabricFishingState.maxLureAngle(hook);
        float next = switch (mode) {
            case SET, RESET -> clamp(amount);
            case ADD -> clamp(current + amount);
            case REMOVE -> clamp(current - amount);
            default -> current;
        };
        if (minimum) {
            FabricFishingState.minLureAngle(hook, next);
        } else {
            FabricFishingState.maxLureAngle(hook, next);
        }
    }

    private float resolveFloat(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return 0.0F;
        }
        Object value = delta[0];
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String string) {
            try {
                return Float.parseFloat(string.trim());
            } catch (NumberFormatException ignored) {
                return 0.0F;
            }
        }
        return 0.0F;
    }

    private float clamp(float value) {
        return Math.max(0.0F, Math.min(360.0F, value));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (minimum ? "minimum" : "maximum") + " fishing approach angle";
    }
}
