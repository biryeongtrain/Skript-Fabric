package org.skriptlang.skript.fabric.runtime;

import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestHelper;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class GameTestRuntimeContext {

    private static final ThreadLocal<@Nullable GameTestHelper> CURRENT_HELPER = new ThreadLocal<>();

    private GameTestRuntimeContext() {
    }

    public static void withHelper(@Nullable GameTestHelper helper, Runnable action) {
        GameTestHelper previous = CURRENT_HELPER.get();
        CURRENT_HELPER.set(helper);
        try {
            action.run();
        } finally {
            if (previous == null) {
                CURRENT_HELPER.remove();
            } else {
                CURRENT_HELPER.set(previous);
            }
        }
    }

    public static <T> T withHelper(@Nullable GameTestHelper helper, Supplier<T> action) {
        GameTestHelper previous = CURRENT_HELPER.get();
        CURRENT_HELPER.set(helper);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT_HELPER.remove();
            } else {
                CURRENT_HELPER.set(previous);
            }
        }
    }

    public static @Nullable GameTestHelper get() {
        return CURRENT_HELPER.get();
    }

    public static @Nullable GameTestHelper resolve(SkriptEvent event) {
        if (event.handle() instanceof GameTestHelper helper) {
            return helper;
        }
        return get();
    }
}
