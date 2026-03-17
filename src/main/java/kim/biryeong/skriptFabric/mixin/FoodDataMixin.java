package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
abstract class FoodDataMixin {

    @Shadow
    private int foodLevel;

    @Unique
    private int skript$previousFoodLevel;

    @Inject(method = "tick", at = @At("HEAD"))
    private void skript$captureFoodLevel(ServerPlayer player, CallbackInfo ci) {
        skript$previousFoodLevel = foodLevel;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void skript$dispatchFoodLevelChange(ServerPlayer player, CallbackInfo ci) {
        if (foodLevel != skript$previousFoodLevel && player.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchFoodLevelChange(level, player, skript$previousFoodLevel, foodLevel);
        }
    }
}
