package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.allay.Allay;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Allay.class)
public interface AllayAccessor {

    @Accessor("duplicationCooldown")
    long skript$getDuplicationCooldown();

    @Accessor("duplicationCooldown")
    void skript$setDuplicationCooldown(long duplicationCooldown);

    @Accessor("jukeboxPos")
    @Nullable BlockPos skript$getJukeboxPos();

    @Accessor("jukeboxPos")
    void skript$setJukeboxPos(@Nullable BlockPos jukeboxPos);

    @Invoker("canDuplicate")
    boolean skript$invokeCanDuplicate();

    @Accessor("DATA_CAN_DUPLICATE")
    static EntityDataAccessor<Boolean> skript$getCanDuplicateTrackedData() {
        throw new AssertionError();
    }
}
