package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

@Name("Is Tameable")
@Description("Check if an entity is tameable.")
@Example("""
    on damage:
        if victim is tameable:
            cancel event
    """)
@Since("2.5")
public class CondIsTameable extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsTameable.class, "tameable", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity instanceof TamableAnimal;
    }

    @Override
    protected String getPropertyName() {
        return "tameable";
    }
}
