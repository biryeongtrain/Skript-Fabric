package ch.njol.skript.expressions;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

final class TabListExpressionSupport {

    private static final Map<Object, String> DISPLAY_NAMES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Object, String> HEADERS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Object, String> FOOTERS = Collections.synchronizedMap(new WeakHashMap<>());

    private TabListExpressionSupport() {
    }

    static @Nullable String getDisplayName(Object player) {
        return DISPLAY_NAMES.get(player);
    }

    static void setDisplayName(Object player, @Nullable String name) {
        if (name == null)
            DISPLAY_NAMES.remove(player);
        else
            DISPLAY_NAMES.put(player, name);
    }

    static @Nullable String getHeader(Object player) {
        return HEADERS.get(player);
    }

    static void setHeader(Object player, @Nullable String header) {
        if (header == null)
            HEADERS.remove(player);
        else
            HEADERS.put(player, header);
    }

    static @Nullable String getFooter(Object player) {
        return FOOTERS.get(player);
    }

    static void setFooter(Object player, @Nullable String footer) {
        if (footer == null)
            FOOTERS.remove(player);
        else
            FOOTERS.put(player, footer);
    }
}
