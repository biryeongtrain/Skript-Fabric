package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.skriptlang.skript.fabric.runtime.SpawnReasonCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(EntityType.class)
abstract class EntityTypeCreateMixin<T extends Entity> {

    @Inject(
            method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;",
            at = @At("HEAD")
    )
    private void skript$captureSpawnReason1(Level level, EntitySpawnReason reason, CallbackInfoReturnable<T> cir) {
        SpawnReasonCapture.set(reason);
    }

    @Inject(
            method = "create(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;",
            at = @At("HEAD")
    )
    private void skript$captureSpawnReason2(ServerLevel level, Consumer<T> consumer, BlockPos pos, EntitySpawnReason reason, boolean bl, boolean bl2, CallbackInfoReturnable<T> cir) {
        SpawnReasonCapture.set(reason);
    }

    @Inject(
            method = "create(Lnet/minecraft/world/level/storage/ValueInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Ljava/util/Optional;",
            at = @At("HEAD")
    )
    private static void skript$captureSpawnReason3(ValueInput input, Level level, EntitySpawnReason reason, CallbackInfoReturnable<Optional<Entity>> cir) {
        SpawnReasonCapture.set(reason);
    }
}
