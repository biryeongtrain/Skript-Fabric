package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayTransformationRotation extends AbstractDisplayExpression<Quaternionf> {

    private boolean left;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        left = matchedPattern == 0;
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected @Nullable Quaternionf convert(Display display) {
        Transformation transformation = PrivateEntityAccess.displayTransformation(display);
        return new Quaternionf(left ? transformation.getLeftRotation() : transformation.getRightRotation());
    }

    @Override
    protected Quaternionf[] createArray(int length) {
        return new Quaternionf[length];
    }

    @Override
    public Class<? extends Quaternionf> getReturnType() {
        return Quaternionf.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{Quaternionf.class, String.class};
            case RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Quaternionf quaternion = mode == ChangeMode.RESET ? new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F) : resolve(delta);
        if (quaternion == null || !quaternion.isFinite()) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            Transformation transformation = PrivateEntityAccess.displayTransformation(display);
            Vector3f translation = new Vector3f(transformation.getTranslation());
            Vector3f scale = new Vector3f(transformation.getScale());
            Quaternionf leftRotation = left ? new Quaternionf(quaternion) : new Quaternionf(transformation.getLeftRotation());
            Quaternionf rightRotation = left ? new Quaternionf(transformation.getRightRotation()) : new Quaternionf(quaternion);
            PrivateEntityAccess.setDisplayTransformation(
                    display,
                    new Transformation(translation, leftRotation, scale, rightRotation)
            );
        }
    }

    private @Nullable Quaternionf resolve(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return null;
        }
        Object value = delta[0];
        if (value instanceof Quaternionf quaternion) {
            return quaternion;
        }
        if (value instanceof String string) {
            return Classes.parse(string, Quaternionf.class, ParseContext.DEFAULT);
        }
        return null;
    }

    @Override
    protected String propertyName() {
        return left ? "left transformation rotation" : "right transformation rotation";
    }
}
