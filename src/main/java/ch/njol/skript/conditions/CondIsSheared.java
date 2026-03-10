package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;

@Name("Entity Is Sheared")
@Description("Checks whether entities are sheared.")
@Example("""
	if targeted entity of player is sheared:
		send "This entity has nothing left to shear!" to player
	""")
@Since("2.8.0")
public class CondIsSheared extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsSheared.class, "(sheared|shorn)", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity instanceof Sheep sheep && sheep.isSheared();
    }

    @Override
    protected String getPropertyName() {
        return "sheared";
    }
}
