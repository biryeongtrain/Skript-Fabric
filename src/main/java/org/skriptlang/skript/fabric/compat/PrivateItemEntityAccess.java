package org.skriptlang.skript.fabric.compat;

import java.util.UUID;
import kim.biryeong.skriptFabric.mixin.ItemEntityAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;

public final class PrivateItemEntityAccess {

    private PrivateItemEntityAccess() {
    }

    public static int pickupDelay(ItemEntity item) {
        return accessor(item).skript$getPickupDelay();
    }

    public static void setPickupDelay(ItemEntity item, int pickupDelay) {
        accessor(item).skript$setPickupDelay(pickupDelay);
    }

    public static @Nullable UUID owner(ItemEntity item) {
        return accessor(item).skript$getOwner();
    }

    public static void setOwner(ItemEntity item, @Nullable UUID owner) {
        accessor(item).skript$setOwner(owner);
    }

    private static ItemEntityAccessor accessor(ItemEntity item) {
        return (ItemEntityAccessor) item;
    }
}
