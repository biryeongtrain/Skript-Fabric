package kim.biryeong.skriptFabric.mixin;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinAi.class)
abstract class PiglinAiMixin {

    @Unique
    private static final ThreadLocal<ItemStack> SKRIPT$BARTER_INPUT = new ThreadLocal<>();

    @Shadow
    private static List<ItemStack> getBarterResponseItems(Piglin piglin) {
        throw new AssertionError();
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At("HEAD"))
    private static void skript$captureBarterInput(ServerLevel level, Piglin piglin, boolean shouldBarter, CallbackInfo callbackInfo) {
        SKRIPT$BARTER_INPUT.set(piglin.getItemInHand(InteractionHand.OFF_HAND));
    }

    @Redirect(
            method = "stopHoldingOffHandItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;getBarterResponseItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Ljava/util/List;"
            )
    )
    private static List<ItemStack> skript$dispatchPiglinBarter(Piglin piglin, ServerLevel level, Piglin ignoredPiglin, boolean shouldBarter) {
        List<ItemStack> outcome = getBarterResponseItems(piglin);
        ItemStack input = SKRIPT$BARTER_INPUT.get();
        SkriptFabricEventBridge.dispatchPiglinBarter(level, piglin, input, outcome);
        return outcome;
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At("RETURN"))
    private static void skript$clearBarterInput(ServerLevel level, Piglin piglin, boolean shouldBarter, CallbackInfo callbackInfo) {
        SKRIPT$BARTER_INPUT.remove();
    }
}
