package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractArrow.class)
abstract class AbstractArrowMixin {

    @Unique
    private int skript$knockbackStrength;

    public int getKnockback() {
        return skript$knockbackStrength;
    }

    public void setKnockback(int value) {
        skript$knockbackStrength = Math.max(0, value);
    }
}
