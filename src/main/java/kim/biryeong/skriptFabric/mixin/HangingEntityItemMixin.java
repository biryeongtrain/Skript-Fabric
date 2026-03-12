package kim.biryeong.skriptFabric.mixin;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HangingEntityItem.class)
abstract class HangingEntityItemMixin {

    @Shadow @Final private EntityType<? extends HangingEntity> type;

    @Inject(method = "useOn", at = @At("RETURN"))
    private void skript$dispatchHangingPlace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (type != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        BlockPos anchorPos = context.getClickedPos();
        ItemFrame itemFrame = level.getEntitiesOfClass(
                        ItemFrame.class,
                        new AABB(anchorPos).inflate(2.0D),
                        entity -> entity.getType() == type
                ).stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(
                        anchorPos.getX() + 0.5D,
                        anchorPos.getY() + 0.5D,
                        anchorPos.getZ() + 0.5D
                )))
                .orElse(null);
        if (itemFrame != null) {
            SkriptFabricEventBridge.dispatchHangingPlace(level, itemFrame, context.getPlayer());
        }
    }
}
