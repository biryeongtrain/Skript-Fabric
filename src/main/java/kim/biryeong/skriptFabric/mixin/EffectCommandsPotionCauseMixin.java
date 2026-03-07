package kim.biryeong.skriptFabric.mixin;

import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.commands.EffectCommands;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EffectCommands.class)
abstract class EffectCommandsPotionCauseMixin {

    @Inject(method = "giveEffect", at = @At("HEAD"))
    private static void skript$pushCommandPotionGiveCause(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            Holder<MobEffect> effect,
            Integer seconds,
            int amplifier,
            boolean ambient,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.COMMAND);
    }

    @Inject(method = "giveEffect", at = @At("RETURN"))
    private static void skript$popCommandPotionGiveCause(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            Holder<MobEffect> effect,
            Integer seconds,
            int amplifier,
            boolean ambient,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.COMMAND);
    }

    @Inject(method = "clearEffects", at = @At("HEAD"))
    private static void skript$pushCommandPotionClearAllCause(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.COMMAND);
    }

    @Inject(method = "clearEffects", at = @At("RETURN"))
    private static void skript$popCommandPotionClearAllCause(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.COMMAND);
    }

    @Inject(method = "clearEffect", at = @At("HEAD"))
    private static void skript$pushCommandPotionClearCause(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            Holder<MobEffect> effect,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.COMMAND);
    }

    @Inject(method = "clearEffect", at = @At("RETURN"))
    private static void skript$popCommandPotionClearCause(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            Holder<MobEffect> effect,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.COMMAND);
    }
}
