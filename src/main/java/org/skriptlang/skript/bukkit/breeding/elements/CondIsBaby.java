package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

public final class CondIsBaby extends AbstractBreedingEntityCondition {

    public CondIsBaby() {
        super(PropertyType.BE, "a baby");
    }

    @Override
    protected boolean checkEntity(Entity entity) {
        return entity instanceof AgeableMob ageable && ageable.isBaby();
    }
}
