package ch.njol.skript.entity;

import net.minecraft.world.entity.animal.cow.Cow;

public final class CowData extends ExactEntityData<Cow> {

    public CowData() {
        super("cow", Cow.class, "cow");
    }
}
