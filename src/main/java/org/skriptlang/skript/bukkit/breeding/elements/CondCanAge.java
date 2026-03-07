package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AgeableMob;
import org.skriptlang.skript.fabric.compat.FabricBreedingState;

public final class CondCanAge extends AbstractBreedingEntityCondition {

    public CondCanAge() {
        super(PropertyType.CAN, "age");
    }

    @Override
    protected boolean checkEntity(Entity entity) {
        return entity instanceof AgeableMob ageable && FabricBreedingState.canAge(ageable);
    }
}
