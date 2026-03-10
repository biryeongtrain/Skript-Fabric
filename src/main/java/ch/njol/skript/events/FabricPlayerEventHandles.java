package ch.njol.skript.events;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

final class FabricPlayerEventHandles {

    private FabricPlayerEventHandles() {
    }

    record Command(String command) {
    }

    record FirstJoin(boolean firstJoin) {
    }

    record Level(int oldLevel, int newLevel) {
    }

    record Move(
            Entity entity,
            @Nullable FabricLocation from,
            @Nullable FabricLocation to,
            float fromYaw,
            float fromPitch,
            float toYaw,
            float toPitch
    ) {
    }

    static final class CommandSend {
        private final Set<String> commands = new LinkedHashSet<>();

        CommandSend(Collection<String> commands) {
            if (commands != null) {
                this.commands.addAll(commands);
            }
        }

        Set<String> commands() {
            return commands;
        }

        Set<String> snapshot() {
            return Collections.unmodifiableSet(new LinkedHashSet<>(commands));
        }
    }

    record Spectate(
            SpectateAction action,
            @Nullable Entity currentTarget,
            @Nullable Entity newTarget
    ) {
    }

    enum SpectateAction {
        START,
        SWAP,
        STOP
    }

    record Teleport(
            Entity entity,
            @Nullable FabricLocation from,
            @Nullable FabricLocation to
    ) {
    }

    record ExperienceChange(ServerPlayer player, int amount) {
    }
}
