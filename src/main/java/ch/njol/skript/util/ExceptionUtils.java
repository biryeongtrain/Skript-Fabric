package ch.njol.skript.util;

import ch.njol.skript.localization.Language;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;

public abstract class ExceptionUtils {

    private static final String IO_NODE = "io exceptions";

    private ExceptionUtils() {
    }

    public static @Nullable String toString(IOException exception) {
        if (Language.keyExists(IO_NODE + "." + exception.getClass().getSimpleName())) {
            return Language.format(IO_NODE + "." + exception.getClass().getSimpleName(), exception.getLocalizedMessage());
        }
        return exception.getLocalizedMessage();
    }
}
