package kim.biryeong.skriptFabric.mixin;

import java.util.OptionalInt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class InventoryOpenCloseMixin {

    @Inject(method = "openMenu", at = @At("HEAD"))
    private void skript$dispatchInventoryOpen(MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchInventoryOpen(level, player);
        }
    }

    @Inject(method = "doCloseContainer", at = @At("HEAD"))
    private void skript$dispatchInventoryClose(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchInventoryClose(level, player);
        }
    }
}
