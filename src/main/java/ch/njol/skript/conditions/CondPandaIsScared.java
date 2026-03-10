package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Panda;

@Name("Panda Is Scared")
@Description("Whether a panda is scared.")
@Example("if last spawned panda is scared:")
@Since("2.11")
public class CondPandaIsScared extends PropertyCondition<LivingEntity> {

    static {
        register(CondPandaIsScared.class, "scared", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity instanceof Panda panda && panda.isScared();
    }

    @Override
    protected String getPropertyName() {
        return "scared";
    }
}
