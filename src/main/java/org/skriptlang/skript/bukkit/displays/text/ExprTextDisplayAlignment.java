package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.classes.Changer.ChangeMode;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTextDisplayAlignment extends AbstractTextDisplayExpression<Display.TextDisplay.Align> {

    @Override
    protected @Nullable Display.TextDisplay.Align convert(Display.TextDisplay textDisplay) {
        return PrivateEntityAccess.textDisplayAlignment(textDisplay);
    }

    @Override
    protected Display.TextDisplay.Align[] createArray(int length) {
        return new Display.TextDisplay.Align[length];
    }

    @Override
    public Class<? extends Display.TextDisplay.Align> getReturnType() {
        return Display.TextDisplay.Align.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{Display.TextDisplay.Align.class};
            case RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Display.TextDisplay.Align alignment = mode == ChangeMode.RESET
                ? Display.TextDisplay.Align.CENTER
                : delta != null && delta.length > 0 && delta[0] instanceof Display.TextDisplay.Align value ? value : null;
        if (alignment == null) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (entity instanceof Display.TextDisplay textDisplay) {
                PrivateEntityAccess.setTextDisplayAlignment(textDisplay, alignment);
            }
        }
    }

    @Override
    protected String propertyName() {
        return "text alignment";
    }
}
