package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprChunk extends SimpleExpression<LevelChunk> {

    static {
        Skript.registerExpression(ExprChunk.class, LevelChunk.class,
                "[(all [[of] the]|the)] chunk[s] (of|%-directions%) %locations%",
                "%locations%'[s] chunk[s]",
                "[(all [[of] the]|the)] loaded chunks (of|in) %worlds%");
    }

    private int pattern;
    private Expression<FabricLocation> locations;
    private Expression<ServerLevel> worlds;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        if (pattern == 0) {
            locations = (Expression<FabricLocation>) exprs[1];
            if (exprs[0] != null) {
                locations = Direction.combine((Expression<? extends Direction>) exprs[0], locations);
            }
        } else if (pattern == 1) {
            locations = (Expression<FabricLocation>) exprs[0];
        } else {
            worlds = (Expression<ServerLevel>) exprs[0];
        }
        return true;
    }

    @Override
    protected LevelChunk @Nullable [] get(SkriptEvent event) {
        if (pattern != 2) {
            return locations.stream(event)
                    .filter(location -> location.level() != null)
                    .map(location -> location.level().getChunkAt(net.minecraft.core.BlockPos.containing(location.position())))
                    .toArray(LevelChunk[]::new);
        }
        List<LevelChunk> chunks = new ArrayList<>();
        for (ServerLevel world : worlds.getArray(event)) {
            try {
                Method method = world.getChunkSource().chunkMap.getClass().getMethod("getChunks");
                Iterable<?> iterable = (Iterable<?>) method.invoke(world.getChunkSource().chunkMap);
                for (Object holder : iterable) {
                    Method chunkMethod = holder.getClass().getMethod("getTickingChunk");
                    Object chunk = chunkMethod.invoke(holder);
                    if (chunk instanceof LevelChunk levelChunk) {
                        chunks.add(levelChunk);
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return chunks.toArray(LevelChunk[]::new);
    }

    @Override
    public boolean isSingle() {
        return pattern != 2 && locations.isSingle();
    }

    @Override
    public Class<? extends LevelChunk> getReturnType() {
        return LevelChunk.class;
    }
}
