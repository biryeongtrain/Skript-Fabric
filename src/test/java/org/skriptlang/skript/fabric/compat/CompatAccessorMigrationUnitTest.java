package org.skriptlang.skript.fabric.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.junit.jupiter.api.Test;

final class CompatAccessorMigrationUnitTest {

    @Test
    void privateEntityAccessNoLongerCachesReflectionMembers() {
        assertNoReflectionCaches(PrivateEntityAccess.class);
    }

    @Test
    void privateBeaconAccessNoLongerCachesReflectionMembers() {
        assertNoReflectionCaches(PrivateBeaconAccess.class);
    }

    @Test
    void privateBeaconAccessStillExposesBeaconStateMutators() throws Exception {
        assertEquals(int.class, PrivateBeaconAccess.class.getDeclaredMethod("levels", BeaconBlockEntity.class).getReturnType());
        assertEquals(
                void.class,
                PrivateBeaconAccess.class.getDeclaredMethod("setLevels", BeaconBlockEntity.class, int.class).getReturnType()
        );
        assertEquals(
                void.class,
                PrivateBeaconAccess.class.getDeclaredMethod("setPrimaryPower", BeaconBlockEntity.class, Holder.class).getReturnType()
        );
        assertEquals(
                void.class,
                PrivateBeaconAccess.class.getDeclaredMethod("setSecondaryPower", BeaconBlockEntity.class, Holder.class).getReturnType()
        );
    }

    @Test
    void privateBellAccessNoLongerCachesReflectionMembers() {
        assertNoReflectionCaches(PrivateBellAccess.class);
    }

    @Test
    void privateBellAccessStillExposesBellStateMutators() throws Exception {
        assertEquals(boolean.class, PrivateBellAccess.class.getDeclaredMethod("isRinging", BellBlockEntity.class).getReturnType());
        assertEquals(
                void.class,
                PrivateBellAccess.class.getDeclaredMethod("setRinging", BellBlockEntity.class, boolean.class).getReturnType()
        );
        assertEquals(
                boolean.class,
                PrivateBellAccess.class.getDeclaredMethod("isResonating", BellBlockEntity.class).getReturnType()
        );
        assertEquals(
                void.class,
                PrivateBellAccess.class.getDeclaredMethod("setResonating", BellBlockEntity.class, boolean.class).getReturnType()
        );
    }

    private static void assertNoReflectionCaches(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            assertFalse(Field.class.isAssignableFrom(fieldType), () -> type.getSimpleName() + " still caches reflection fields");
            assertFalse(Method.class.isAssignableFrom(fieldType), () -> type.getSimpleName() + " still caches reflection methods");
        }
    }
}
