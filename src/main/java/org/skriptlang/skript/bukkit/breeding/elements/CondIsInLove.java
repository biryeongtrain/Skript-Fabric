package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

public final class CondIsInLove extends AbstractBreedingEntityCondition {

    public CondIsInLove() {
        super(PropertyType.BE, "in love");
    }

    @Override
    protected boolean checkEntity(Entity entity) {
        return entity instanceof Animal animal && animal.isInLove();
    }
}
