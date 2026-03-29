package ch.njol.skript.entity;

import net.minecraft.world.entity.animal.parrot.Parrot;

public final class ParrotData extends ExactEntityData<Parrot> {

    public ParrotData() {
        super("parrot", Parrot.class, "parrot");
    }
}
