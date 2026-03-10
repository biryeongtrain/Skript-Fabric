package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;

@Name("Enderman Has Been Stared At")
@Description({
        "Checks to see if an enderman has been stared at.",
        "This will return true as long as the entity that stared at the enderman is still alive."
})
@Example("if last spawned enderman has been stared at:")
@Since("2.11")
public class CondEndermanStaredAt extends PropertyCondition<LivingEntity> {

    static {
        register(CondEndermanStaredAt.class, PropertyType.HAVE, "been stared at", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity instanceof EnderMan enderman && enderman.hasBeenStaredAt();
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.HAVE;
    }

    @Override
    protected String getPropertyName() {
        return "stared at";
    }
}
