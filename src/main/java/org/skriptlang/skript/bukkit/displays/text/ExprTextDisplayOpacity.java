package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.classes.Changer.ChangeMode;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTextDisplayOpacity extends AbstractTextDisplayExpression<Integer> {

    @Override
    protected @Nullable Integer convert(Display.TextDisplay textDisplay) {
        return toUnsigned(PrivateEntityAccess.textDisplayOpacity(textDisplay));
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
            case RESET, DELETE -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.intValue() : 255;
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display.TextDisplay textDisplay)) {
                continue;
            }
            byte next = switch (mode) {
                case ADD -> toSigned(clamp(toUnsigned(PrivateEntityAccess.textDisplayOpacity(textDisplay)) + amount, 0, 255));
                case REMOVE -> toSigned(clamp(toUnsigned(PrivateEntityAccess.textDisplayOpacity(textDisplay)) - amount, 0, 255));
                case RESET, DELETE -> toSigned(255);
                case SET -> toSigned(clamp(amount, -128, 255));
                default -> PrivateEntityAccess.textDisplayOpacity(textDisplay);
            };
            if (mode == ChangeMode.ADD
                    || mode == ChangeMode.REMOVE
                    || mode == ChangeMode.RESET
                    || mode == ChangeMode.DELETE
                    || mode == ChangeMode.SET) {
                PrivateEntityAccess.setTextDisplayOpacity(textDisplay, next);
            }
        }
    }

    private static int toUnsigned(byte value) {
        return value < 0 ? 256 + value : value;
    }

    private static byte toSigned(int value) {
        return (byte) (value > 127 ? value - 256 : value);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected String propertyName() {
        return "text opacity";
    }
}
