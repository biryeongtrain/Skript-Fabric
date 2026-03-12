package kim.biryeong.skriptFabric.mixin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaEffectCloud.class)
abstract class AreaEffectCloudMixin {

    @Shadow @Final private Map<Entity, Integer> victims;

    @Unique
    private @Nullable Set<Entity> skript$victimsBeforeTick;

    @Inject(method = "tick", at = @At("HEAD"))
    private void skript$captureVictimsBeforeTick(CallbackInfo callbackInfo) {
        skript$victimsBeforeTick = new HashSet<>(victims.keySet());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void skript$dispatchAreaCloudEffect(CallbackInfo callbackInfo) {
        try {
            AreaEffectCloud self = (AreaEffectCloud) (Object) this;
            if (!(self.level() instanceof ServerLevel serverLevel) || victims.isEmpty()) {
                return;
            }
            Set<Entity> victimsBeforeTick = skript$victimsBeforeTick;
            ArrayList<LivingEntity> affectedEntities = new ArrayList<>();
            for (Entity entity : victims.keySet()) {
                if (entity instanceof LivingEntity livingEntity
                        && (victimsBeforeTick == null || !victimsBeforeTick.contains(entity))) {
                    affectedEntities.add(livingEntity);
                }
            }
            if (!affectedEntities.isEmpty()) {
                SkriptFabricEventBridge.dispatchAreaCloudEffect(serverLevel, affectedEntities);
            }
        } finally {
            skript$victimsBeforeTick = null;
        }
    }
}
