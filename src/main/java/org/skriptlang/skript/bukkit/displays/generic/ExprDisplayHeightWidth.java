package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayHeightWidth extends AbstractDisplayExpression<Float> {

    private boolean height;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        height = matchedPattern == 0;
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected @Nullable Float convert(Display display) {
        return height ? PrivateEntityAccess.displayHeight(display) : PrivateEntityAccess.displayWidth(display);
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
            case ADD, REMOVE, RESET, SET -> new Class[]{Float.class, Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        float amount = delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.floatValue() : 0F;
        if (Float.isNaN(amount) || Float.isInfinite(amount)) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            float current = height ? PrivateEntityAccess.displayHeight(display) : PrivateEntityAccess.displayWidth(display);
            float next = switch (mode) {
                case ADD -> current + amount;
                case REMOVE -> current - amount;
                case RESET -> 0F;
                case SET -> amount;
                default -> current;
            };
            if (Float.isNaN(next) || Float.isInfinite(next)) {
                continue;
            }
            next = Math.max(0F, next);
            if (height && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET || mode == ChangeMode.SET)) {
                PrivateEntityAccess.setDisplayHeight(display, next);
            } else if (!height && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET || mode == ChangeMode.SET)) {
                PrivateEntityAccess.setDisplayWidth(display, next);
            }
        }
    }

    @Override
    protected String propertyName() {
        return height ? "display height" : "display width";
    }
}
