package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Gliding State")
@Description("Sets of gets gliding state of player. It allows you to set gliding state of entity even if they do not have an <a href=\"https://minecraft.wiki/w/Elytra\">Elytra</a> equipped.")
@Example("set gliding of player to off")
@Since("2.2-dev21")
public class ExprGlidingState extends SimplePropertyExpression<LivingEntity, Boolean> {

    private static final int FLAG_FALL_FLYING = findFlag();
    private static final Method SET_SHARED_FLAG = findSetSharedFlag();

    static {
        register(ExprGlidingState.class, Boolean.class, "(gliding|glider) [state]", "livingentities");
    }

    @Override
    public Boolean convert(LivingEntity entity) {
        return entity.isFallFlying();
    }

    @Override
    protected String getPropertyName() {
        return "gliding state";
    }

    @Override
    public Class<Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET || mode == ChangeMode.RESET ? new Class[]{Boolean.class} : null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        boolean state = delta != null && delta.length > 0 && Boolean.TRUE.equals(delta[0]);
        for (LivingEntity entity : getExpr().getArray(event)) {
            try {
                SET_SHARED_FLAG.invoke(entity, FLAG_FALL_FLYING, state);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to change gliding state.", exception);
            }
        }
    }

    private static int findFlag() {
        try {
            Field field = Entity.class.getDeclaredField("FLAG_FALL_FLYING");
            field.setAccessible(true);
            return field.getInt(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access fall flying entity flag.", exception);
        }
    }

    private static Method findSetSharedFlag() {
        try {
            Method method = Entity.class.getDeclaredMethod("setSharedFlag", int.class, boolean.class);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access shared entity flag setter.", exception);
        }
    }
}
