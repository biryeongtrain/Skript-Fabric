package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.FabricEventCompatHandles;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityArmorChangeMixin {

    @Inject(method = "onEquipItem", at = @At("TAIL"))
    private void skript$dispatchArmorChange(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo callbackInfo) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }
        FabricEventCompatHandles.ArmorSlot compatSlot = switch (slot) {
            case HEAD -> FabricEventCompatHandles.ArmorSlot.HELMET;
            case CHEST -> FabricEventCompatHandles.ArmorSlot.CHESTPLATE;
            case LEGS -> FabricEventCompatHandles.ArmorSlot.LEGGINGS;
            case FEET -> FabricEventCompatHandles.ArmorSlot.BOOTS;
            default -> null;
        };
        if (compatSlot == null) {
            return;
        }
        if (oldItem.getCount() == newItem.getCount() && ItemStack.isSameItemSameComponents(oldItem, newItem)) {
            return;
        }
        SkriptFabricEventBridge.dispatchArmorChange(player, compatSlot);
    }
}
