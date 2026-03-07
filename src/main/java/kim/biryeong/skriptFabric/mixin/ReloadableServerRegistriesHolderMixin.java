package kim.biryeong.skriptFabric.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootTable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableServerRegistries.Holder.class)
abstract class ReloadableServerRegistriesHolderMixin {

    @Inject(method = "getLootTable", at = @At("RETURN"))
    private void skript$rememberLootTableKey(
            ResourceKey<LootTable> key,
            CallbackInfoReturnable<LootTable> callbackInfo
    ) {
        LootTable lootTable = callbackInfo.getReturnValue();
        if (lootTable == null) {
            return;
        }
        SkriptFabricEventBridge.rememberLootTableKey(lootTable, key);
    }
}
