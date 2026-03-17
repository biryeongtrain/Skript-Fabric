package kim.biryeong.skriptFabric.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class PlayerSleepMixin {

    @Inject(method = "startSleepInBed", at = @At("HEAD"))
    private void skript$dispatchBedEnter(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchBedEnter(level, player);
        }
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void skript$dispatchBedLeave(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.isSleeping() && player.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchBedLeave(level, player);
        }
    }
}
