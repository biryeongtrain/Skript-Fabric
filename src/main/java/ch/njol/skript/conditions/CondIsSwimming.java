package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;

@Name("Is Swimming")
@Description("Checks whether a living entity is swimming.")
@Example("player is swimming")
@Since("2.3")
public class CondIsSwimming extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsSwimming.class, "swimming", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity.isSwimming();
    }

    @Override
    protected String getPropertyName() {
        return "swimming";
    }
}
