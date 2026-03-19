package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityLastHurtAccessor {
    @Accessor("lastHurt")
    float skript$getLastHurt();

    @Accessor("lastHurt")
    void skript$setLastHurt(float value);
}
