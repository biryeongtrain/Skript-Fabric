package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FishingHook.class)
public interface FishingHookAccessor {

    @Accessor("timeUntilHooked")
    int skript$getTimeUntilHooked();

    @Accessor("timeUntilHooked")
    void skript$setTimeUntilHooked(int value);

    @Accessor("timeUntilLured")
    int skript$getTimeUntilLured();

    @Accessor("timeUntilLured")
    void skript$setTimeUntilLured(int value);

    @Accessor("nibble")
    int skript$getNibble();

    @Accessor("nibble")
    void skript$setNibble(int value);

    @Accessor("biting")
    boolean skript$isBiting();

    @Accessor("biting")
    void skript$setBiting(boolean value);

    @Accessor("lureSpeed")
    int skript$getLureSpeed();

    @Accessor("hookedIn")
    @Nullable Entity skript$getHookedIn();

    @Accessor("hookedIn")
    void skript$setHookedIn(@Nullable Entity entity);

    @Invoker("onHitEntity")
    void skript$invokeOnHitEntity(EntityHitResult hitResult);

    @Invoker("onHitBlock")
    void skript$invokeOnHitBlock(BlockHitResult hitResult);

    @Invoker("catchingFish")
    void skript$invokeCatchingFish(BlockPos position);

    @Invoker("pullEntity")
    void skript$invokePullEntity(Entity entity);

    @Accessor("currentState")
    FishingHook.FishHookState skript$getCurrentState();

    @Accessor("currentState")
    void skript$setCurrentState(FishingHook.FishHookState state);
}
