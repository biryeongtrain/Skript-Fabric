package kim.biryeong.skriptFabric.mixin;

import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {

    @Accessor("pickupDelay")
    int skript$getPickupDelay();

    @Accessor("pickupDelay")
    void skript$setPickupDelay(int pickupDelay);

    @Accessor("target")
    @Nullable UUID skript$getOwner();

    @Accessor("target")
    void skript$setOwner(@Nullable UUID owner);
}
