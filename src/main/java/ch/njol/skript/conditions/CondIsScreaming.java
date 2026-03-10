package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.goat.Goat;

@Name("Is Screaming")
@Description("Check whether a goat or enderman is screaming.")
@Example("""
		if last spawned goat is not screaming:
			make last spawned goat scream
	""")
@Example("""
		if {_enderman} is screaming:
			force {_enderman} to stop screaming
	""")
@Since("2.11")
public class CondIsScreaming extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsScreaming.class, "screaming", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity instanceof Goat goat && goat.isScreamingGoat();
    }

    @Override
    protected String getPropertyName() {
        return "screaming";
    }
}
