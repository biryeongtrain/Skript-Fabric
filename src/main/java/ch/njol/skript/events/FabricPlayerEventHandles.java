package ch.njol.skript.events;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;

public final class FabricPlayerEventHandles {

    private FabricPlayerEventHandles() {
    }

    public static Object command(String command) {
        return new Command(command);
    }

    public static Object firstJoin(boolean firstJoin) {
        return new FirstJoin(firstJoin);
    }

    public static Object level(int oldLevel, int newLevel) {
        return new Level(oldLevel, newLevel);
    }

    public static Object move(
            Entity entity,
            @Nullable FabricLocation from,
            @Nullable FabricLocation to,
            float fromYaw,
            float fromPitch,
            float toYaw,
            float toPitch
    ) {
        return new Move(entity, from, to, fromYaw, fromPitch, toYaw, toPitch);
    }

    public static Object commandSend(Collection<String> commands) {
        return new CommandSend(commands);
    }

    public static Object spectate(
            SpectateAction action,
            @Nullable Entity currentTarget,
            @Nullable Entity newTarget
    ) {
        return new Spectate(action, currentTarget, newTarget);
    }

    public static Object teleport(
            Entity entity,
            @Nullable FabricLocation from,
            @Nullable FabricLocation to
    ) {
        return new Teleport(entity, from, to);
    }

    public static Object experienceChange(ServerPlayer player, int amount) {
        return new ExperienceChange(player, amount);
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
    ) implements FabricEntityEventHandle {
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

    public enum SpectateAction {
        START,
        SWAP,
        STOP
    }

    record Teleport(
            Entity entity,
            @Nullable FabricLocation from,
            @Nullable FabricLocation to
    ) implements FabricEntityEventHandle {
    }

    record ExperienceChange(ServerPlayer player, int amount) {
    }
}
