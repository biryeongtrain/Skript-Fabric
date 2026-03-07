package kim.biryeong.skriptFabric.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
abstract class BrewingStandBlockEntityMixin {

    @Inject(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
            )
    )
    private static void skript$dispatchBrewingFuel(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo callbackInfo) {
        if (level instanceof ServerLevel serverLevel) {
            SkriptFabricEventBridge.dispatchBrewingFuel(serverLevel, pos, blockEntity, true);
        }
    }

    @Inject(
            method = "serverTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;ingredient:Lnet/minecraft/world/item/Item;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private static void skript$dispatchBrewingStart(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo callbackInfo) {
        if (level instanceof ServerLevel serverLevel) {
            SkriptFabricEventBridge.dispatchBrewingStart(serverLevel, pos, blockEntity);
        }
    }

    @Inject(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;doBrew(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void skript$dispatchBrewingComplete(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo callbackInfo) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        List<ItemStack> results = new ArrayList<>(3);
        for (int slot = 0; slot < 3; slot++) {
            results.add(blockEntity.getItem(slot).copy());
        }

        SkriptFabricEventBridge.dispatchBrewingComplete(serverLevel, pos, blockEntity, results);

        for (int slot = 0; slot < 3; slot++) {
            ItemStack updated = slot < results.size() ? results.get(slot).copy() : ItemStack.EMPTY;
            blockEntity.setItem(slot, updated);
        }
    }
}
