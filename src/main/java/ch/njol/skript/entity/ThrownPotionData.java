package ch.njol.skript.entity;

import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;

public final class ThrownPotionData extends ClassEntityData<AbstractThrownPotion> {

    public ThrownPotionData() {
        super("thrown potion", AbstractThrownPotion.class);
    }
}
