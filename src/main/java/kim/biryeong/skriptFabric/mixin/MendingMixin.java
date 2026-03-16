package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.FabricEventCompatHandles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrb.class)
abstract class MendingMixin {

    @Redirect(
            method = "repairPlayerItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"
            )
    )
    private void skript$dispatchMending(ItemStack item, int newDamageValue, ServerPlayer player, int experience) {
        int repairAmount = item.getDamageValue() - newDamageValue;
        if (repairAmount <= 0) {
            item.setDamageValue(newDamageValue);
            return;
        }
        ExperienceOrb orb = (ExperienceOrb) (Object) this;
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            item.setDamageValue(newDamageValue);
            return;
        }
        FabricEventCompatHandles.Mending handle = SkriptFabricEventBridge.dispatchMending(
                serverLevel, player, item, repairAmount, orb
        );
        int modifiedRepairAmount = handle.repairAmount();
        if (modifiedRepairAmount <= 0) {
            // Cancelled or repair amount set to 0 — skip the repair
            return;
        }
        int actualRepair = Math.min(modifiedRepairAmount, item.getDamageValue());
        item.setDamageValue(item.getDamageValue() - actualRepair);
    }
}
