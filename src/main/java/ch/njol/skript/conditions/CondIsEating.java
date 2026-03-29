package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

@Name("Is Eating")
@Description("Whether a panda or horse type (horse, camel, donkey, llama, mule) is eating.")
@Example("""
        if last spawned panda is eating:
            force last spawned panda to stop eating
        """)
@Since("2.11")
public class CondIsEating extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsEating.class, "eating", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        if (entity instanceof Panda panda) {
            return panda.isEating();
        }
        return entity instanceof AbstractHorse horse && horse.isEating();
    }

    @Override
    protected String getPropertyName() {
        return "eating";
    }
}
