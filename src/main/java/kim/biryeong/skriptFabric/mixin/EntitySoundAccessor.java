package kim.biryeong.skriptFabric.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntitySoundAccessor {

	@Invoker("getSwimSound")
	SoundEvent skript$getSwimSound();

	@Invoker("getSwimSplashSound")
	SoundEvent skript$getSwimSplashSound();

	@Invoker("getSwimHighSpeedSplashSound")
	SoundEvent skript$getSwimHighSpeedSplashSound();

}
