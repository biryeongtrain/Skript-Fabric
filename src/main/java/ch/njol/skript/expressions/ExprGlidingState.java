package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import kim.biryeong.skriptFabric.mixin.EntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Gliding State")
@Description("Sets of gets gliding state of player. It allows you to set gliding state of entity even if they do not have an <a href=\"https://minecraft.wiki/w/Elytra\">Elytra</a> equipped.")
@Example("set gliding of player to off")
@Since("2.2-dev21")
public class ExprGlidingState extends SimplePropertyExpression<LivingEntity, Boolean> {
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
            ((EntityAccessor)entity).callSetSharedFlag(EntityAccessor.getFLAG_FALL_FLYING(), state);
        }
    }
}
