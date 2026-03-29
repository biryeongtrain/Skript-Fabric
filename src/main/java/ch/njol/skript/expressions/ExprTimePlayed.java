package ch.njol.skript.expressions;

import com.mojang.authlib.GameProfile;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.util.Timespan;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTimePlayed extends PropertyExpression<GameProfile, Timespan> {

    static final Stat<?> PLAY_TIME = Stats.CUSTOM.get(Stats.PLAY_TIME);

    static {
        register(ExprTimePlayed.class, Timespan.class, "(time played|play[ ]time)", "offlineplayers");
    }

    @Override
    protected Timespan[] get(SkriptEvent event, GameProfile[] source) {
        List<Timespan> values = new ArrayList<>();
        for (GameProfile profile : source) {
            Integer ticks = readTicks(event, profile);
            if (ticks != null) {
                values.add(new Timespan(Timespan.TimePeriod.TICK, ticks));
            }
        }
        return values.toArray(Timespan[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Timespan.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(delta != null && delta[0] instanceof Timespan timespan)) {
            return;
        }
        int amount = (int) timespan.getAs(Timespan.TimePeriod.TICK);
        for (GameProfile profile : getExpr().getArray(event)) {
            Integer current = readTicks(event, profile);
            if (current == null) {
                continue;
            }
            int next = switch (mode) {
                case SET -> amount;
                case ADD -> current + amount;
                case REMOVE -> current - amount;
                default -> current;
            };
            writeTicks(event, profile, Math.max(0, next));
        }
    }

    static @Nullable Integer readTicks(SkriptEvent event, GameProfile profile) {
        MinecraftServer server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null || profile.id() == null) {
            return null;
        }
        ServerPlayer player = resolvePlayer(server, profile);
        if (player != null) {
            return readTicks(player.getStats());
        }
        Path statsFile = statsFile(server, profile);
        if (!Files.exists(statsFile)) {
            return null;
        }
        return readTicks(new ServerStatsCounter(server, statsFile));
    }

    static int readTicks(ServerStatsCounter stats) {
        return stats.getValue(PLAY_TIME);
    }

    static void writeTicks(ServerStatsCounter stats, @Nullable Player owner, int ticks) {
        stats.setValue(owner, PLAY_TIME, Math.max(0, ticks));
    }

    private static void writeTicks(SkriptEvent event, GameProfile profile, int ticks) {
        MinecraftServer server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null || profile.id() == null) {
            return;
        }
        ServerPlayer player = resolvePlayer(server, profile);
        if (player != null) {
            writeTicks(player.getStats(), player, ticks);
            return;
        }
        ServerStatsCounter stats = new ServerStatsCounter(server, statsFile(server, profile));
        writeTicks(stats, null, ticks);
        stats.save();
    }

    private static @Nullable ServerPlayer resolvePlayer(MinecraftServer server, GameProfile profile) {
        ServerPlayer player = server.getPlayerList().getPlayer(profile.id());
        if (player == null && profile.name() != null) {
            player = server.getPlayerList().getPlayerByName(profile.name());
        }
        return player;
    }

    private static Path statsFile(MinecraftServer server, GameProfile profile) {
        return server.getWorldPath(LevelResource.PLAYER_STATS_DIR).resolve(profile.id() + ".json");
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "time played of " + getExpr().toString(event, debug);
    }
}
