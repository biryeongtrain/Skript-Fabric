package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Last Attacker")
@Description("The last entity that attacked an entity.")
@Example("send \"%last attacker of event-entity%\"")
@Since("2.5.1")
public class ExprLastAttacker extends PropertyExpression<LivingEntity, Entity> {

    static {
        register(ExprLastAttacker.class, Entity.class, "last attacker", "livingentities");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<LivingEntity>) expressions[0]);
        return true;
    }

    @Override
    protected Entity[] get(SkriptEvent event, LivingEntity[] source) {
        return get(source, entity -> entity.getLastDamageSource() == null ? null : entity.getLastDamageSource().getEntity());
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "last attacker of " + getExpr().toString(event, debug);
    }
}
