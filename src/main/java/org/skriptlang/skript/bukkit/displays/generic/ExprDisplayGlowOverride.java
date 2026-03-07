package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.util.Kleenean;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;

public final class ExprDisplayGlowOverride extends AbstractDisplayExpression<Color> {

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        displays = (Expression<Entity>) expressions[0];
        return true;
    }

    @Override
    protected @Nullable Color convert(Display display) {
        int rgb = PrivateEntityAccess.displayGlowColorOverride(display);
        return rgb < 0 ? null : ColorRGB.fromRgb(rgb);
    }

    @Override
    public Class<? extends Color> getReturnType() {
        return Color.class;
    }

    @Override
    protected Color[] createArray(int length) {
        return new Color[length];
    }

    @Override
    protected String propertyName() {
        return "glow color override";
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{String.class, Number.class, Color.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int rgb = -1;
        if (mode == ChangeMode.SET && delta != null && delta.length > 0) {
            ColorRGB parsed = ColorRGB.parse(delta[0]);
            if (parsed == null) {
                Skript.error("Unsupported glow color override value.");
                return;
            }
            rgb = parsed.rgb();
        }
        for (Entity entity : displays.getAll(event)) {
            if (entity instanceof Display display) {
                PrivateEntityAccess.setDisplayGlowColorOverride(display, rgb);
            }
        }
    }
}
