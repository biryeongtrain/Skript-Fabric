package kim.biryeong.skriptFabric.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntitySoundAccessor {

	@Invoker("getDeathSound")
	@Nullable SoundEvent skript$getDeathSound();

	@Invoker("getHurtSound")
	@Nullable SoundEvent skript$getHurtSound(DamageSource source);

	@Invoker("getFallDamageSound")
	SoundEvent skript$getFallDamageSound(int height);

	@Invoker("getFallSounds")
	LivingEntity.Fallsounds skript$getFallSounds();

}
