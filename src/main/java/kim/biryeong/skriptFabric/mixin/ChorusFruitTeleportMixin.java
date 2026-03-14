package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.TeleportCause;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import net.minecraft.world.level.Level;
import org.skriptlang.skript.fabric.runtime.TeleportCauseCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeleportRandomlyConsumeEffect.class)
abstract class ChorusFruitTeleportMixin {

    @Inject(method = "apply", at = @At("HEAD"))
    private void skript$setChorusFruitCause(Level level, ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        TeleportCauseCapture.set(TeleportCause.CHORUS_FRUIT);
    }
}
