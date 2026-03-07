package kim.biryeong.skriptFabric.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
abstract class LootTableMixin {

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN")
    )
    private void skript$dispatchLootGenerate(
            LootContext context,
            CallbackInfoReturnable<ObjectArrayList<ItemStack>> callbackInfo
    ) {
        ObjectArrayList<ItemStack> loot = callbackInfo.getReturnValue();
        if (loot == null) {
            return;
        }
        SkriptFabricEventBridge.dispatchLootGenerate(context, (LootTable) (Object) this, loot);
    }
}
