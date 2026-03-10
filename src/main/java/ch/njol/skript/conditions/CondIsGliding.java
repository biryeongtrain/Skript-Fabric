package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;

@Name("Is Gliding")
@Description("Checks whether a living entity is gliding.")
@Example("if player is gliding")
@Since("2.7")
public class CondIsGliding extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsGliding.class, "gliding", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity.isFallFlying();
    }

    @Override
    protected String getPropertyName() {
        return "gliding";
    }
}
