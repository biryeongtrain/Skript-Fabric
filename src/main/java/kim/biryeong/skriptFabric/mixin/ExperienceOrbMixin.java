package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(ExperienceOrb.class)
abstract class ExperienceOrbMixin {

    @Inject(
            method = "playerTouch",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Player;takeXpDelay:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void skript$dispatchExperienceCooldownChange(Player player, CallbackInfo callbackInfo) {
        if (player instanceof ServerPlayer serverPlayer) {
            SkriptFabricEventBridge.dispatchExperienceCooldownChange(serverPlayer, "pickup");
        }
    }
}
