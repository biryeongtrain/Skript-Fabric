package ch.njol.skript.entity;

import net.minecraft.world.entity.npc.villager.Villager;

public final class VillagerData extends ExactEntityData<Villager> {

    public VillagerData() {
        super("villager", Villager.class, "villager");
    }
}
