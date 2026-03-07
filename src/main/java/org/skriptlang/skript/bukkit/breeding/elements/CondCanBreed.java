package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.skriptlang.skript.fabric.compat.FabricBreedingState;

public final class CondCanBreed extends AbstractBreedingEntityCondition {

    public CondCanBreed() {
        super(PropertyType.CAN, "breed");
    }

    @Override
    protected boolean checkEntity(Entity entity) {
        return entity instanceof Animal animal
                && !animal.isBaby()
                && animal.getAge() == 0
                && FabricBreedingState.canBreed(animal);
    }
}
