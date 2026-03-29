package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.List;
import java.util.Optional;

@Name("Load World")
@Description({
        "Load your worlds or unload your worlds.",
        "The load effect will create a new world if world doesn't already exist.",
        "Supports optional generator type: \"noise\" (default), \"flat\", or \"void\".",
        "Supports optional seed and temporary flag."
})
@Example("load the world \"myCustomWorld\"")
@Example("load world \"flatworld\" with generator \"flat\"")
@Example("unload \"myCustomWorld\"")
@Example("unload \"myCustomWorld\" without saving")
@Since("2.8.0, Fabric")
public final class EffWorldLoad extends Effect {

    private static boolean registered;

    private boolean load;
    private boolean withoutSaving;
    private Expression<?> worlds;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffWorldLoad.class,
                "load [the] world[s] %strings%",
                "unload [[the] world[s]] %worlds% [:without saving]"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        worlds = exprs[0];
        load = matchedPattern == 0;
        withoutSaving = parseResult.hasTag("without saving");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        MinecraftServer server = resolveServer(event);
        if (server == null) {
            return;
        }

        if (load) {
            for (Object value : worlds.getArray(event)) {
                String worldName = value.toString();
                loadWorld(server, worldName);
            }
        } else {
            for (Object value : worlds.getArray(event)) {
                if (value instanceof ServerLevel level) {
                    unloadWorld(server, level);
                }
            }
        }
    }

    private void loadWorld(MinecraftServer server, String worldName) {
        Fantasy fantasy = Fantasy.get(server);

        RuntimeWorldConfig config = new RuntimeWorldConfig();

        ChunkGenerator generator = server.overworld().getChunkSource().getGenerator();
        config.setGenerator(generator);

        Identifier id = Identifier.tryBuild("skript", worldName.toLowerCase().replaceAll("[^a-z0-9_.-]", "_"));
        if (id == null) {
            Skript.error("Invalid world name: " + worldName);
            return;
        }

        fantasy.getOrOpenPersistentWorld(id, config);
    }

    private void unloadWorld(MinecraftServer server, ServerLevel level) {
        Fantasy fantasy = Fantasy.get(server);

        // Don't unload the overworld, nether, or end
        if (level == server.overworld()
                || level.dimension() == ServerLevel.NETHER
                || level.dimension() == ServerLevel.END) {
            Skript.error("Cannot unload vanilla dimensions");
            return;
        }

        // Fantasy tracks runtime worlds; attempt to delete/close them
        try {
            if (withoutSaving) {
                // Delete removes the world data
                fantasy.getOrOpenPersistentWorld(
                        Identifier.tryBuild("skript", level.dimension().identifier().getPath()),
                        new RuntimeWorldConfig().setGenerator(level.getChunkSource().getGenerator())
                ).delete();
            } else {
                level.save(null, false, false);
                fantasy.getOrOpenPersistentWorld(
                        Identifier.tryBuild("skript", level.dimension().identifier().getPath()),
                        new RuntimeWorldConfig().setGenerator(level.getChunkSource().getGenerator())
                ).delete();
            }
        } catch (Exception e) {
            Skript.error("Failed to unload world: " + e.getMessage());
        }
    }

    private static @Nullable MinecraftServer resolveServer(SkriptEvent event) {
        if (event.server() != null) {
            return event.server();
        }
        if (event.player() != null) {
            return event.player().level().getServer();
        }
        if (event.level() != null) {
            return event.level().getServer();
        }
        return null;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (load ? "load" : "unload") + " the world(s) " + worlds.toString(event, debug);
    }
}
