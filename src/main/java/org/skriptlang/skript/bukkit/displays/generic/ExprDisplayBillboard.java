package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayBillboard extends AbstractDisplayExpression<Display.BillboardConstraints> {

    @Override
    protected @Nullable Display.BillboardConstraints convert(Display display) {
        return PrivateEntityAccess.displayBillboardConstraints(display);
    }

    @Override
    protected Display.BillboardConstraints[] createArray(int length) {
        return new Display.BillboardConstraints[length];
    }

    @Override
    public Class<? extends Display.BillboardConstraints> getReturnType() {
        return Display.BillboardConstraints.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case RESET -> new Class[0];
            case SET -> new Class[]{Display.BillboardConstraints.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Display.BillboardConstraints next = delta != null && delta.length > 0 && delta[0] instanceof Display.BillboardConstraints constraints
                ? constraints
                : Display.BillboardConstraints.FIXED;
        if (mode != ChangeMode.RESET && mode != ChangeMode.SET) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (entity instanceof Display display) {
                PrivateEntityAccess.setDisplayBillboardConstraints(display, next);
            }
        }
    }

    @Override
    protected String propertyName() {
        return "billboard";
    }
}
