package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.ServerLevelAccessor;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Spider.class)
abstract class SpiderPotionCauseMixin {

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void skript$pushSpiderSpawnPotionCause(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> callbackInfo
    ) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.SPIDER_SPAWN);
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void skript$popSpiderSpawnPotionCause(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> callbackInfo
    ) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.SPIDER_SPAWN);
    }
}
