package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Gravity")
@Description("If entity is affected by gravity or not, i.e. if it has Minecraft 1.10+ NoGravity flag.")
@Example("set gravity of player off")
@Since("2.2-dev21")
public class ExprGravity extends SimplePropertyExpression<Entity, Boolean> {

    static {
        register(ExprGravity.class, Boolean.class, "gravity", "entities");
    }

    @Override
    public Boolean convert(Entity entity) {
        return !entity.isNoGravity();
    }

    @Override
    protected String getPropertyName() {
        return "gravity";
    }

    @Override
    public Class<Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
            return new Class[]{Boolean.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) throws UnsupportedOperationException {
        boolean value = delta == null || delta.length == 0 || (Boolean) delta[0];
        for (Entity entity : getExpr().getArray(event)) {
            entity.setNoGravity(!value);
        }
    }
}
