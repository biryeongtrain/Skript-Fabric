package ch.njol.skript.lang.util;

import ch.njol.skript.lang.Trigger;
import org.jetbrains.annotations.Nullable;
import java.util.function.BiFunction;

/**
 * Utility methods for loading section code in linked contexts.
 */
public final class SectionUtils {

    private SectionUtils() {
    }

    /**
     * Compatibility bridge for linked trigger loading.
     * This simplified implementation currently forwards before/after loading callbacks
     * without additional hint/delay tracking.
     */
    public static @Nullable Trigger loadLinkedCode(String name, BiFunction<Runnable, Runnable, Trigger> triggerSupplier) {
        Runnable beforeLoading = () -> {
        };
        Runnable afterLoading = () -> {
        };
        return triggerSupplier.apply(beforeLoading, afterLoading);
    }
}
