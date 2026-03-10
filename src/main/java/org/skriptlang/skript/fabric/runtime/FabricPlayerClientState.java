package org.skriptlang.skript.fabric.runtime;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class FabricPlayerClientState {

    private static final Map<ServerPlayer, String> RESOURCE_PACK_STATUS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private FabricPlayerClientState() {
    }

    public static void setResourcePackStatus(@Nullable ServerPlayer player, @Nullable Enum<?> status) {
        if (player == null) {
            return;
        }
        if (status == null) {
            RESOURCE_PACK_STATUS.remove(player);
            return;
        }
        RESOURCE_PACK_STATUS.put(player, status.name().toLowerCase(Locale.ENGLISH));
    }

    public static boolean hasLoadedResourcePack(@Nullable ServerPlayer player) {
        return player != null && "successfully_loaded".equals(RESOURCE_PACK_STATUS.get(player));
    }
}
