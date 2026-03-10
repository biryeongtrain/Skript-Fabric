package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Entity AI")
@Description("Returns whether an entity has AI.")
@Example("set artificial intelligence of target entity to false")
@Since("2.5")
public class ExprAI extends SimplePropertyExpression<Entity, Boolean> {

    static {
        register(ExprAI.class, Boolean.class, "(ai|artificial intelligence)", "livingentities");
    }

    @Override
    @Nullable
    public Boolean convert(Entity entity) {
        return entity instanceof Mob mob && !mob.isNoAi();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return mode == Changer.ChangeMode.SET ? new Class[]{Boolean.class} : null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof Boolean value)) {
            return;
        }
        for (Entity entity : getExpr().getArray(event)) {
            if (entity instanceof Mob mob) {
                mob.setNoAi(!value);
            }
        }
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    protected String getPropertyName() {
        return "artificial intelligence";
    }
}
