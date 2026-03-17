package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
abstract class TamableAnimalMixin {

    @Inject(method = "tame", at = @At("HEAD"))
    private void skript$dispatchTame(Player player, CallbackInfo ci) {
        TamableAnimal animal = (TamableAnimal) (Object) this;
        if (animal.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchTame(level, animal, player);
        }
    }
}
