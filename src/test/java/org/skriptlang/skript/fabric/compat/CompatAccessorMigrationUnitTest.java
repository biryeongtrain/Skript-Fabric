package org.skriptlang.skript.fabric.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

final class CompatAccessorMigrationUnitTest {

    @Test
    void privateFishingHookAccessNoLongerCachesReflectionMembers() {
        assertNoReflectionCaches(PrivateFishingHookAccess.class);
    }

    @Test
    void privateEntityAccessNoLongerCachesReflectionMembers() {
        assertNoReflectionCaches(PrivateEntityAccess.class);
    }

    private static void assertNoReflectionCaches(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            assertFalse(Field.class.isAssignableFrom(fieldType), () -> type.getSimpleName() + " still caches reflection fields");
            assertFalse(Method.class.isAssignableFrom(fieldType), () -> type.getSimpleName() + " still caches reflection methods");
        }
    }
}
