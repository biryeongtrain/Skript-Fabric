package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeehiveBlockEntity.class)
public interface BeehiveBlockEntityFlowerAccessor {
    @Accessor("savedFlowerPos")
    @Nullable BlockPos skript$getSavedFlowerPos();

    @Accessor("savedFlowerPos")
    void skript$setSavedFlowerPos(@Nullable BlockPos pos);
}
