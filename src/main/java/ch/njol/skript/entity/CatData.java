package ch.njol.skript.entity;

import net.minecraft.world.entity.animal.feline.Cat;

public final class CatData extends ExactEntityData<Cat> {

    public CatData() {
        super("cat", Cat.class, "cat");
    }
}
