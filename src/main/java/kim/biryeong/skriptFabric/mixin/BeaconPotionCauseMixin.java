package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconPotionCauseMixin {

    @Shadow private int levels;

    @Unique
    private boolean skript$initialized;

    @Unique
    private boolean skript$active;

    @Inject(method = "tick", at = @At("TAIL"))
    private static void skript$dispatchBeaconToggle(
            Level level,
            BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            BeaconBlockEntity blockEntity,
            CallbackInfo callbackInfo
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BeaconPotionCauseMixin mixin = (BeaconPotionCauseMixin) (Object) blockEntity;
        boolean active = mixin.levels > 0;
        if (!mixin.skript$initialized) {
            mixin.skript$initialized = true;
            mixin.skript$active = active;
            return;
        }
        if (mixin.skript$active != active) {
            mixin.skript$active = active;
            SkriptFabricEventBridge.dispatchBeaconToggle(serverLevel, pos, active);
        }
    }

    @Inject(method = "applyEffects", at = @At("HEAD"))
    private static void skript$pushBeaconPotionCause(
            Level level,
            BlockPos pos,
            int levels,
            Holder<MobEffect> primaryPower,
            Holder<MobEffect> secondaryPower,
            CallbackInfo callbackInfo
    ) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.BEACON);
        if (level instanceof ServerLevel serverLevel) {
            if (primaryPower != null) {
                SkriptFabricEventBridge.dispatchBeaconEffect(serverLevel, pos, true, primaryPower);
            }
            if (secondaryPower != null && secondaryPower != primaryPower) {
                SkriptFabricEventBridge.dispatchBeaconEffect(serverLevel, pos, false, secondaryPower);
            }
        }
    }

    @Inject(method = "applyEffects", at = @At("RETURN"))
    private static void skript$popBeaconPotionCause(
            Level level,
            BlockPos pos,
            int levels,
            Holder<MobEffect> primaryPower,
            Holder<MobEffect> secondaryPower,
            CallbackInfo callbackInfo
    ) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.BEACON);
    }
}
