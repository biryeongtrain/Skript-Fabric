package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;

@Name("Is Saddled")
@Description({
        "Checks whether a given entity (horse or steerable) is saddled.",
        "If 'properly' is used, this will only return true if the entity is wearing specifically a saddle item."
})
@Example("send whether {_horse} is saddled")
@Since("2.10")
public class CondIsSaddled extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsSaddled.class, "[:properly] saddled", "livingentities");
    }

    private boolean properly;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        properly = parseResult.hasTag("properly");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public boolean check(LivingEntity entity) {
        try {
            Object saddled = entity.getClass().getMethod("isSaddled").invoke(entity);
            return saddled instanceof Boolean value && value;
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    @Override
    protected String getPropertyName() {
        return properly ? "properly saddled" : "saddled";
    }
}
