package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.events.TeleportCause;
import org.jetbrains.annotations.Nullable;

public final class TeleportCauseCapture {

    private static final ThreadLocal<@Nullable TeleportCause> CAPTURE = new ThreadLocal<>();

    private TeleportCauseCapture() {
    }

    public static void set(TeleportCause cause) {
        CAPTURE.set(cause);
    }

    public static @Nullable TeleportCause consume() {
        TeleportCause cause = CAPTURE.get();
        CAPTURE.remove();
        return cause;
    }

    public static TeleportCause getOrDefault(TeleportCause defaultCause) {
        TeleportCause cause = consume();
        return cause != null ? cause : defaultCause;
    }
}
