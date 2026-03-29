package ch.njol.skript.entity;

import net.minecraft.world.entity.animal.rabbit.Rabbit;

public final class RabbitData extends ExactEntityData<Rabbit> {

    public RabbitData() {
        super("rabbit", Rabbit.class, "rabbit");
    }
}
