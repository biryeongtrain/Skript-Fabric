package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerExperienceMixin {

    @Unique
    private int skript$experienceOldLevel;

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"))
    private void skript$captureExperiencePoints(int amount, CallbackInfo callbackInfo) {
        Player self = (Player) (Object) this;
        skript$experienceOldLevel = self.experienceLevel;
    }

    @Inject(method = "giveExperiencePoints", at = @At("TAIL"))
    private void skript$dispatchExperiencePoints(int amount, CallbackInfo callbackInfo) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer player) || amount == 0) {
            return;
        }
        SkriptFabricEventBridge.dispatchExperienceChange(player, amount);
        if (skript$experienceOldLevel != player.experienceLevel) {
            SkriptFabricEventBridge.dispatchLevelChange(player, skript$experienceOldLevel, player.experienceLevel);
        }
    }

    @Inject(method = "giveExperienceLevels", at = @At("HEAD"))
    private void skript$captureExperienceLevels(int amount, CallbackInfo callbackInfo) {
        Player self = (Player) (Object) this;
        skript$experienceOldLevel = self.experienceLevel;
    }

    @Inject(method = "giveExperienceLevels", at = @At("TAIL"))
    private void skript$dispatchExperienceLevels(int amount, CallbackInfo callbackInfo) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer player) || amount == 0) {
            return;
        }
        if (skript$experienceOldLevel != player.experienceLevel) {
            SkriptFabricEventBridge.dispatchLevelChange(player, skript$experienceOldLevel, player.experienceLevel);
        }
    }
}
