package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayTransformationScaleTranslation extends AbstractDisplayExpression<Vec3> {

    private boolean scale;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        scale = matchedPattern == 0;
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected @Nullable Vec3 convert(Display display) {
        Transformation transformation = PrivateEntityAccess.displayTransformation(display);
        Vector3f vector = scale ? transformation.getScale() : transformation.getTranslation();
        return new Vec3(vector.x, vector.y, vector.z);
    }

    @Override
    protected Vec3[] createArray(int length) {
        return new Vec3[length];
    }

    @Override
    public Class<? extends Vec3> getReturnType() {
        return Vec3.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{Vec3.class, String.class};
            case RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Vec3 vector = mode == ChangeMode.RESET ? (scale ? new Vec3(1.0D, 1.0D, 1.0D) : Vec3.ZERO) : resolve(delta);
        if (vector == null) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            Transformation transformation = PrivateEntityAccess.displayTransformation(display);
            Vector3f nextVector = new Vector3f((float) vector.x, (float) vector.y, (float) vector.z);
            Quaternionf leftRotation = new Quaternionf(transformation.getLeftRotation());
            Quaternionf rightRotation = new Quaternionf(transformation.getRightRotation());
            Vector3f translation = scale ? new Vector3f(transformation.getTranslation()) : nextVector;
            Vector3f nextScale = scale ? nextVector : new Vector3f(transformation.getScale());
            PrivateEntityAccess.setDisplayTransformation(
                    display,
                    new Transformation(translation, leftRotation, nextScale, rightRotation)
            );
        }
    }

    private @Nullable Vec3 resolve(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return null;
        }
        Object value = delta[0];
        if (value instanceof Vec3 vector) {
            return vector;
        }
        if (value instanceof String string) {
            return Classes.parse(string, Vec3.class, ParseContext.DEFAULT);
        }
        return null;
    }

    @Override
    protected String propertyName() {
        return scale ? "transformation scale" : "transformation translation";
    }
}
