package kim.biryeong.skriptFabric.mixin;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerWorldInitMixin {

    @Inject(
            method = "createLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void skript$dispatchOverworldInitialization(
            ChunkProgressListener chunkProgressListener,
            CallbackInfo callbackInfo,
            ServerLevelData serverLevelData,
            boolean debug,
            Registry<LevelStem> registry,
            WorldOptions worldOptions,
            long seed,
            long obfuscatedSeed,
            List<?> customSpawners,
            LevelStem levelStem,
            ServerLevel serverLevel
    ) {
        SkriptFabricEventBridge.dispatchWorldInit((MinecraftServer) (Object) this, serverLevel);
    }

    @Inject(
            method = "createLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 1
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void skript$dispatchDimensionInitialization(
            ChunkProgressListener chunkProgressListener,
            CallbackInfo callbackInfo,
            ServerLevelData serverLevelData,
            boolean debug,
            Registry<LevelStem> registry,
            WorldOptions worldOptions,
            long seed,
            long obfuscatedSeed,
            List<?> customSpawners,
            LevelStem levelStem,
            ServerLevel overworld,
            DimensionDataStorage dimensionDataStorage,
            WorldBorder worldBorder,
            RandomSequences randomSequences,
            java.util.Iterator<?> iterator,
            Map.Entry<ResourceKey<LevelStem>, LevelStem> entry,
            ResourceKey<LevelStem> levelStemKey,
            ResourceKey<Level> levelKey,
            DerivedLevelData derivedLevelData,
            ServerLevel serverLevel
    ) {
        SkriptFabricEventBridge.dispatchWorldInit((MinecraftServer) (Object) this, serverLevel);
    }
}
