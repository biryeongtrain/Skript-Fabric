package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayViewRange extends AbstractDisplayExpression<Float> {

    @Override
    protected @Nullable Float convert(Display display) {
        return PrivateEntityAccess.displayViewRange(display);
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
        float amount = delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.floatValue() : 1F;
        if (Float.isNaN(amount) || Float.isInfinite(amount)) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            float current = PrivateEntityAccess.displayViewRange(display);
            float next = switch (mode) {
                case ADD -> current + amount;
                case REMOVE -> current - amount;
                case RESET -> 1F;
                case SET -> amount;
                default -> current;
            };
            if (Float.isNaN(next) || Float.isInfinite(next)) {
                continue;
            }
            if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET || mode == ChangeMode.SET) {
                PrivateEntityAccess.setDisplayViewRange(display, Math.max(0F, next));
            }
        }
    }

    @Override
    protected String propertyName() {
        return "view range";
    }
}
