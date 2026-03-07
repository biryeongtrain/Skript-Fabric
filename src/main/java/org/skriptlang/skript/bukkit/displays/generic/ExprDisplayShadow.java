package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayShadow extends AbstractDisplayExpression<Float> {

    private boolean radius;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = matchedPattern == 0;
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected @Nullable Float convert(Display display) {
        return radius ? PrivateEntityAccess.displayShadowRadius(display) : PrivateEntityAccess.displayShadowStrength(display);
    }

    @Override
    protected Float[] createArray(int length) {
        return new Float[length];
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET -> new Class[]{Float.class, Number.class};
            case RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        float amount = delta != null && delta.length > 0 && delta[0] instanceof Number number
                ? number.floatValue()
                : (radius ? 0F : 1F);
        if (Float.isNaN(amount) || Float.isInfinite(amount)) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            float current = radius ? PrivateEntityAccess.displayShadowRadius(display) : PrivateEntityAccess.displayShadowStrength(display);
            float next = switch (mode) {
                case ADD -> current + amount;
                case REMOVE -> current - amount;
                case RESET -> radius ? 0F : 1F;
                case SET -> amount;
                default -> current;
            };
            if (Float.isNaN(next) || Float.isInfinite(next)) {
                continue;
            }
            next = Math.max(0F, next);
            if (radius && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET || mode == ChangeMode.SET)) {
                PrivateEntityAccess.setDisplayShadowRadius(display, next);
            } else if (!radius && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET || mode == ChangeMode.SET)) {
                PrivateEntityAccess.setDisplayShadowStrength(display, next);
            }
        }
    }

    @Override
    protected String propertyName() {
        return radius ? "shadow radius" : "shadow strength";
    }
}
