package org.skriptlang.skript.fabric.compat;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Interaction;
import org.jetbrains.annotations.Nullable;

public final class FabricInteractionState {

    private static final Map<Interaction, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private FabricInteractionState() {
    }

    public static void recordAttack(Interaction interaction, ServerPlayer player) {
        state(interaction).attack = new Action(player.getGameProfile(), System.currentTimeMillis());
    }

    public static void recordInteract(Interaction interaction, ServerPlayer player) {
        state(interaction).interact = new Action(player.getGameProfile(), System.currentTimeMillis());
    }

    public static @Nullable Action lastAttack(Interaction interaction) {
        return state(interaction).attack;
    }

    public static @Nullable Action lastInteract(Interaction interaction) {
        return state(interaction).interact;
    }

    public static @Nullable Action lastClick(Interaction interaction) {
        Action attack = state(interaction).attack;
        Action interact = state(interaction).interact;
        if (attack == null) {
            return interact;
        }
        if (interact == null) {
            return attack;
        }
        return attack.timestamp() >= interact.timestamp() ? attack : interact;
    }

    public record Action(GameProfile player, long timestamp) {
    }

    private static State state(Interaction interaction) {
        return STATES.computeIfAbsent(interaction, ignored -> new State());
    }

    private static final class State {
        private @Nullable Action attack;
        private @Nullable Action interact;
    }
}
