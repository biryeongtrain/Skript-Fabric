package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.classes.Changer.ChangeMode;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTextDisplayLineWidth extends AbstractTextDisplayExpression<Integer> {

    @Override
    protected @Nullable Integer convert(Display.TextDisplay textDisplay) {
        return PrivateEntityAccess.textDisplayLineWidth(textDisplay);
    }

    @Override
    protected Integer[] createArray(int length) {
        return new Integer[length];
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET -> new Class[]{Integer.class, Number.class};
            case RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.intValue() : 200;
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display.TextDisplay textDisplay)) {
                continue;
            }
            int next = switch (mode) {
                case ADD -> Math.max(0, PrivateEntityAccess.textDisplayLineWidth(textDisplay) + amount);
                case REMOVE -> Math.max(0, PrivateEntityAccess.textDisplayLineWidth(textDisplay) - amount);
                case RESET -> 200;
                case SET -> Math.max(0, amount);
                default -> PrivateEntityAccess.textDisplayLineWidth(textDisplay);
            };
            if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET || mode == ChangeMode.SET) {
                PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, next);
            }
        }
    }

    @Override
    protected String propertyName() {
        return "line width";
    }
}
