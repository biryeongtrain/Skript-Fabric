package org.skriptlang.skript.fabric.compat;

import java.lang.reflect.Field;
import java.util.UUID;
import kim.biryeong.skriptFabric.mixin.ItemEntityAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;

public final class PrivateItemEntityAccess {

    private PrivateItemEntityAccess() {
    }

    public static int pickupDelay(ItemEntity item) {
        if (item instanceof ItemEntityAccessor accessor) {
            return accessor.skript$getPickupDelay();
        }
        try {
            return field("pickupDelay").getInt(item);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read item pickup delay.", exception);
        }
    }

    public static void setPickupDelay(ItemEntity item, int pickupDelay) {
        if (item instanceof ItemEntityAccessor accessor) {
            accessor.skript$setPickupDelay(pickupDelay);
            return;
        }
        try {
            field("pickupDelay").setInt(item, pickupDelay);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to set item pickup delay.", exception);
        }
    }

    public static @Nullable UUID owner(ItemEntity item) {
        if (item instanceof ItemEntityAccessor accessor) {
            return accessor.skript$getOwner();
        }
        try {
            return (UUID) field("target", "owner").get(item);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read item owner.", exception);
        }
    }

    public static void setOwner(ItemEntity item, @Nullable UUID owner) {
        if (item instanceof ItemEntityAccessor accessor) {
            accessor.skript$setOwner(owner);
            return;
        }
        try {
            field("target", "owner").set(item, owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to set item owner.", exception);
        }
    }

    private static ItemEntityAccessor accessor(ItemEntity item) {
        return (ItemEntityAccessor) item;
    }

    private static Field field(String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                Field field = ItemEntity.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(String.join(", ", names));
    }
}
