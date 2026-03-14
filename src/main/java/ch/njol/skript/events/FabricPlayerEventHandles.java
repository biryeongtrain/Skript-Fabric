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
            @Nullable FabricLocation to,
            @Nullable TeleportCause cause
    ) {
        return new Teleport(entity, from, to, cause);
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

    public record Spectate(
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

    public record Teleport(
            Entity entity,
            @Nullable FabricLocation from,
            @Nullable FabricLocation to,
            @Nullable TeleportCause cause
    ) implements FabricEntityEventHandle {
    }

    record ExperienceChange(ServerPlayer player, int amount) {
    }

    public static Object join() {
        return new Join();
    }

    public static Object connect() {
        return new Connect();
    }

    public static Object kick(@Nullable net.minecraft.network.chat.Component reason) {
        return new Kick(reason);
    }

    public static Object quit() {
        return new Quit();
    }

    static final class Join {
        private @Nullable net.minecraft.network.chat.Component message;

        Join() {
        }

        public @Nullable net.minecraft.network.chat.Component message() {
            return message;
        }

        public void setMessage(@Nullable net.minecraft.network.chat.Component message) {
            this.message = message;
        }
    }

    record Connect() {
    }

    static final class Kick {
        private @Nullable net.minecraft.network.chat.Component reason;

        Kick(@Nullable net.minecraft.network.chat.Component reason) {
            this.reason = reason;
        }

        public @Nullable net.minecraft.network.chat.Component reason() {
            return reason;
        }

        public void setReason(@Nullable net.minecraft.network.chat.Component reason) {
            this.reason = reason;
        }
    }

    static final class Quit {
        private @Nullable net.minecraft.network.chat.Component message;

        Quit() {
        }

        public @Nullable net.minecraft.network.chat.Component message() {
            return message;
        }

        public void setMessage(@Nullable net.minecraft.network.chat.Component message) {
            this.message = message;
        }
    }
}
