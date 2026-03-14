package kim.biryeong.skriptFabric.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobSoundAccessor {

	@Invoker("getAmbientSound")
	@Nullable SoundEvent skript$getAmbientSound();

}
