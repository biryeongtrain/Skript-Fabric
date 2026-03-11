package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BellBlockEntity.class)
public interface BellBlockEntityAccessor {

    @Accessor("resonating")
    boolean skript$isResonating();

    @Accessor("resonating")
    void skript$setResonating(boolean resonating);
}
