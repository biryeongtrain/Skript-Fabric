package kim.biryeong.skriptFabric.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.skriptlang.skript.fabric.compat.PrivateFurnaceAccess;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract class AbstractFurnaceBlockEntityMixin {

    private static final ThreadLocal<Deque<SmeltSnapshot>> SKRIPT$SMELT_SNAPSHOTS = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
            method = "serverTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;litTotalTime:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private static void skript$dispatchFurnaceBurn(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo callbackInfo
    ) {
        SkriptFabricEventBridge.dispatchFurnaceBurn(level, pos, furnace, furnace.getItem(0), furnace.getItem(1));
    }

    @Inject(
            method = "serverTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingTimer:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private static void skript$dispatchFurnaceStart(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo callbackInfo
    ) {
        if (furnace.getItem(0).isEmpty()) {
            return;
        }
        if (PrivateFurnaceAccess.cookingTimer(furnace) != 1) {
            return;
        }
        SkriptFabricEventBridge.dispatchFurnaceSmeltingStart(level, pos, furnace, furnace.getItem(0), furnace.getItem(1));
    }

    @Inject(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/NonNullList;I)Z"
            )
    )
    private static void skript$captureSmeltSnapshot(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo callbackInfo
    ) {
        SKRIPT$SMELT_SNAPSHOTS.get().push(new SmeltSnapshot(
                pos.immutable(),
                furnace.getItem(0).copy(),
                furnace.getItem(1).copy(),
                furnace.getItem(2).copy()
        ));
    }

    @Inject(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/NonNullList;I)Z",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void skript$dispatchFurnaceSmelt(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo callbackInfo,
            boolean wasLit,
            boolean changed,
            ItemStack fuel,
            ItemStack source,
            boolean hasSource,
            boolean hasFuel,
            net.minecraft.world.item.crafting.RecipeHolder<?> recipe,
            net.minecraft.world.item.crafting.SingleRecipeInput input,
            int maxStackSize
    ) {
        Deque<SmeltSnapshot> snapshots = SKRIPT$SMELT_SNAPSHOTS.get();
        SmeltSnapshot snapshot = snapshots.poll();
        if (snapshots.isEmpty()) {
            SKRIPT$SMELT_SNAPSHOTS.remove();
        }
        if (snapshot == null || recipe == null) {
            return;
        }

        ItemStack currentResult = furnace.getItem(2).copy();
        if (currentResult.isEmpty() || !resultAdvanced(snapshot.result(), currentResult)) {
            return;
        }

        net.minecraft.world.item.crafting.AbstractCookingRecipe cookingRecipe =
                (net.minecraft.world.item.crafting.AbstractCookingRecipe) recipe.value();
        ItemStack recipeResult = cookingRecipe.assemble(input, level.registryAccess());
        if (recipeResult.isEmpty()) {
            recipeResult = currentResult.copy();
        }
        SkriptFabricEventBridge.dispatchFurnaceSmelt(
                level,
                snapshot.position(),
                furnace,
                snapshot.source(),
                snapshot.fuel(),
                recipeResult
        );
    }

    private static boolean resultAdvanced(ItemStack before, ItemStack after) {
        if (after.isEmpty()) {
            return false;
        }
        if (before.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(before, after)) {
            return true;
        }
        return after.getCount() > before.getCount();
    }

    private record SmeltSnapshot(BlockPos position, ItemStack source, ItemStack fuel, ItemStack result) {
    }
}
