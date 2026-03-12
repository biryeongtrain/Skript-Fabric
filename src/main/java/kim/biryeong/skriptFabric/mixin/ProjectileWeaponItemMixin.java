package kim.biryeong.skriptFabric.mixin;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileWeaponItem.class)
abstract class ProjectileWeaponItemMixin {

    @Inject(method = "shoot", at = @At("HEAD"))
    private void skript$dispatchEntityShootBow(
            ServerLevel level,
            LivingEntity shooter,
            InteractionHand hand,
            ItemStack weapon,
            List<ItemStack> projectiles,
            float velocity,
            float inaccuracy,
            boolean isCrit,
            @Nullable LivingEntity target,
            CallbackInfo callbackInfo
    ) {
        SkriptFabricEventBridge.dispatchEntityShootBow(level, shooter, skript$firstConsumable(projectiles));
    }

    private static @Nullable ItemStack skript$firstConsumable(List<ItemStack> projectiles) {
        for (ItemStack projectile : projectiles) {
            if (!projectile.isEmpty()) {
                return projectile;
            }
        }
        return null;
    }
}
