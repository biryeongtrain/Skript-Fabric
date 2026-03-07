package org.skriptlang.skript.fabric.runtime;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public final class FabricPotionEffectCauseContext {

    private static final ThreadLocal<Deque<FabricPotionEffectCause>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private FabricPotionEffectCauseContext() {
    }

    public static void push(FabricPotionEffectCause cause) {
        STACK.get().push(cause);
    }

    public static void pop(FabricPotionEffectCause cause) {
        Deque<FabricPotionEffectCause> stack = STACK.get();
        if (stack.isEmpty()) {
            return;
        }
        if (stack.peek() == cause) {
            stack.pop();
        } else {
            stack.removeFirstOccurrence(cause);
        }
        if (stack.isEmpty()) {
            STACK.remove();
        }
    }

    public static @Nullable FabricPotionEffectCause current() {
        Deque<FabricPotionEffectCause> stack = STACK.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    public static void run(FabricPotionEffectCause cause, Runnable runnable) {
        push(cause);
        try {
            runnable.run();
        } finally {
            pop(cause);
        }
    }

    public static <T> T call(FabricPotionEffectCause cause, Supplier<T> supplier) {
        push(cause);
        try {
            return supplier.get();
        } finally {
            pop(cause);
        }
    }
}
