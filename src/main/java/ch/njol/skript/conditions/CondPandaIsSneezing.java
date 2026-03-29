package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.panda.Panda;

@Name("Panda Is Sneezing")
@Description("Whether a panda is sneezing.")
@Example("""
	if last spawned panda is sneezing:
		make last spawned panda stop sneezing
	""")
@Since("2.11")
public class CondPandaIsSneezing extends PropertyCondition<LivingEntity> {

    static {
        register(CondPandaIsSneezing.class, "sneezing", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity instanceof Panda panda && panda.isSneezing();
    }

    @Override
    protected String getPropertyName() {
        return "sneezing";
    }
}
