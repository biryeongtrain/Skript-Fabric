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

@Name("Fall Distance")
@Description({"The distance an entity has fallen for."})
@Example("set all entities' fall distance to 10")
@Example("""
    on damage:
        send "%victim's fall distance%" to victim
    """)
@Since("2.5")
public class ExprFallDistance extends SimplePropertyExpression<Entity, Number> {

    static {
        register(ExprFallDistance.class, Number.class, "fall[en] (distance|height)", "entities");
    }

    @Override
    @Nullable
    public Number convert(Entity entity) {
        return entity.fallDistance;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0) {
            return;
        }
        float amount = ((Number) delta[0]).floatValue();
        for (Entity entity : getExpr().getArray(event)) {
            switch (mode) {
                case ADD -> entity.fallDistance += amount;
                case SET -> entity.fallDistance = amount;
                case REMOVE -> entity.fallDistance -= amount;
                case DELETE, RESET -> entity.fallDistance = 0;
            }
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "fall distance";
    }
}
