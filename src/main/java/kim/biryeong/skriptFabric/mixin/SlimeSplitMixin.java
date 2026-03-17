package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
abstract class SlimeSplitMixin {

    @Shadow
    public abstract int getSize();

    @Inject(method = "remove", at = @At("HEAD"))
    private void skript$dispatchSlimeSplit(Entity.RemovalReason reason, CallbackInfo ci) {
        Slime slime = (Slime) (Object) this;
        if (reason == Entity.RemovalReason.KILLED && getSize() > 1 && slime.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchSlimeSplit(level, slime);
        }
    }
}
