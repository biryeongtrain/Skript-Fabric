package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionLaneAGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void laneASpatialLocationExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            loadScripts(
                    runtime,
                    "skript/gametest/expression/lane-a/altitude_records_value.sk",
                    "skript/gametest/expression/lane-a/location_records_value.sk",
                    "skript/gametest/expression/lane-a/location_of_records_value.sk"
            );

            BlockPos relative = new BlockPos(0, 1, 0);
            BlockPos absolute = helper.absolutePos(relative);
            helper.getLevel().setBlockAndUpdate(absolute, Blocks.STONE.defaultBlockState());
            loadTemplateScript(
                    runtime,
                    "skript/gametest/expression/lane-a/location_at_records_value.sk",
                    coordinateReplacements("__TARGET_", absolute.getX() + 3, absolute.getY(), absolute.getZ())
            );
            loadTemplateScript(
                    runtime,
                    "skript/gametest/expression/lane-a/distance_records_value.sk",
                    coordinateReplacements("__TARGET_", absolute.getX() + 3, absolute.getY(), absolute.getZ())
            );

            int executed = dispatch(runtime, helper, new LaneAContextHandle(helper.getLevel(), absolute));
            assertExecuted(helper, executed, 5, "lane A spatial location bundle");

            assertNumber(helper, "lanea::altitude", absolute.getY());
            assertLocation(helper, "lanea::location", absolute.getX(), absolute.getY(), absolute.getZ());
            assertLocation(helper, "lanea::location_of", absolute.getX(), absolute.getY(), absolute.getZ());
            assertLocation(helper, "lanea::location_at", absolute.getX() + 3.0D, absolute.getY(), absolute.getZ());
            assertNumber(helper, "lanea::distance", 3.0D);
            runtime.clearScripts();
        });
    }

    @GameTest
    public void laneABlockAndChunkExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            loadScripts(
                    runtime,
                    "skript/gametest/expression/lane-a/block_marks_above.sk",
                    "skript/gametest/expression/lane-a/block_data_copies_neighbor.sk",
                    "skript/gametest/expression/lane-a/chunk_x_records_value.sk",
                    "skript/gametest/expression/lane-a/chunk_z_records_value.sk"
            );

            BlockPos relative = new BlockPos(1, 1, 0);
            BlockPos absolute = helper.absolutePos(relative);
            helper.getLevel().setBlockAndUpdate(absolute, Blocks.GOLD_BLOCK.defaultBlockState());

            int executed = dispatch(runtime, helper, new LaneAContextHandle(helper.getLevel(), absolute));
            assertExecuted(helper, executed, 4, "lane A block and chunk bundle");

            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, relative.above());
            helper.assertTrue(
                    Variables.getVariable("lanea::event_block_data", null, false) instanceof BlockState state
                            && state.is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected block data expression to read the event block's gold block state.")
            );
            assertNumber(helper, "lanea::chunk_x", helper.getLevel().getChunkAt(absolute).getPos().x);
            assertNumber(helper, "lanea::chunk_z", helper.getLevel().getChunkAt(absolute).getPos().z);
            runtime.clearScripts();
        });
    }

    @GameTest
    public void laneAWorldPropertyExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            BlockPos relative = new BlockPos(2, 1, 0);
            BlockPos absolute = helper.absolutePos(relative);
            helper.getLevel().setBlockAndUpdate(absolute, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(absolute.east(), Blocks.REDSTONE_BLOCK.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(absolute.above(), Blocks.TORCH.defaultBlockState());

            BlockPos originalSpawn = helper.getLevel().getSharedSpawnPos();
            LaneAContextHandle handle = new LaneAContextHandle(helper.getLevel(), absolute);

            executeScript(runtime, helper, handle, "skript/gametest/expression/lane-a/difficulty_records_value.sk", "lane A difficulty");

            helper.assertTrue(
                    Variables.getVariable("lanea::difficulty", null, false) == helper.getLevel().getDifficulty(),
                    Component.literal("Expected difficulty expression to expose the world difficulty.")
            );

            executeScript(runtime, helper, handle, "skript/gametest/expression/lane-a/humidity_records_value.sk", "lane A humidity");
            Object humidity = Variables.getVariable("lanea::humidity", null, false);
            helper.assertTrue(
                    humidity instanceof Number
                            && Double.compare(((Number) humidity).doubleValue(), readHumidity(helper, absolute)) == 0,
                    Component.literal("Expected humidity expression to record the biome humidity at the event block.")
            );

            executeScript(runtime, helper, handle, "skript/gametest/expression/lane-a/light_level_records_value.sk", "lane A light level");
            assertNumber(helper, "lanea::light_level", helper.getLevel().getBrightness(LightLayer.BLOCK, absolute));

            executeScript(runtime, helper, handle, "skript/gametest/expression/lane-a/redstone_power_records_value.sk", "lane A redstone power");
            assertNumber(helper, "lanea::redstone_power", helper.getLevel().getBestNeighborSignal(absolute));

            executeScript(runtime, helper, handle, "skript/gametest/expression/lane-a/sea_level_records_value.sk", "lane A sea level");
            assertNumber(helper, "lanea::sea_level", helper.getLevel().getSeaLevel());

            executeScript(runtime, helper, handle, "skript/gametest/expression/lane-a/seed_records_value.sk", "lane A seed");
            assertNumber(helper, "lanea::seed", helper.getLevel().getSeed());

            executeScript(runtime, helper, handle, "skript/gametest/expression/lane-a/simulation_distance_records_value.sk", "lane A simulation distance");
            assertNumber(helper, "lanea::simulation_distance", helper.getLevel().getServer().getPlayerList().getSimulationDistance());

            executeTemplateScript(
                    runtime,
                    helper,
                    handle,
                    "skript/gametest/expression/lane-a/spawn_changes_world_spawn.sk",
                    coordinateReplacements("__SPAWN_", absolute.getX() + 7, absolute.getY() + 1, absolute.getZ()),
                    "lane A spawn"
            );
            BlockPos expectedSpawn = new BlockPos(absolute.getX() + 7, absolute.getY() + 1, absolute.getZ());
            helper.assertTrue(
                    helper.getLevel().getSharedSpawnPos().equals(expectedSpawn),
                    Component.literal("Expected spawn location expression to update the world spawn.")
            );
            helper.getLevel().setDefaultSpawnPos(originalSpawn, 0.0F);
            runtime.clearScripts();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestLaneAContextEvent.class, "gametest lane a context");
        Skript.registerExpression(LaneALocationExpression.class, FabricLocation.class, "lane-a-location");
        Skript.registerExpression(LaneAWorldExpression.class, ServerLevel.class, "lane-a-world");
    }

    private static void loadScripts(SkriptRuntime runtime, String... paths) {
        for (String path : paths) {
            runtime.loadFromResource(path);
        }
    }

    private static void executeScript(SkriptRuntime runtime, GameTestHelper helper, LaneAContextHandle handle, String path, String description) {
        runtime.clearScripts();
        runtime.loadFromResource(path);
        int executed = dispatch(runtime, helper, handle);
        assertExecuted(helper, executed, 1, description);
    }

    private static void executeTemplateScript(
            SkriptRuntime runtime,
            GameTestHelper helper,
            LaneAContextHandle handle,
            String path,
            Map<String, String> replacements,
            String description
    ) {
        runtime.clearScripts();
        loadTemplateScript(runtime, path, replacements);
        int executed = dispatch(runtime, helper, handle);
        assertExecuted(helper, executed, 1, description);
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(
                handle,
                helper.getLevel().getServer(),
                helper.getLevel(),
                null
        ));
    }

    private static void assertExecuted(GameTestHelper helper, int executed, int expected, String description) {
        helper.assertTrue(
                executed == expected,
                Component.literal("Expected " + expected + " triggers for " + description + " but got " + executed + ".")
        );
    }

    private static Map<String, String> coordinateReplacements(String prefix, int x, int y, int z) {
        return Map.of(
                prefix + "X__", Integer.toString(x),
                prefix + "Y__", Integer.toString(y),
                prefix + "Z__", Integer.toString(z)
        );
    }

    private static void loadTemplateScript(SkriptRuntime runtime, String resourcePath, Map<String, String> replacements) {
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = SkriptFabricExpressionLaneAGameTest.class.getClassLoader();
        }

        String source;
        try (InputStream stream = classLoader.getResourceAsStream(normalized)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing script resource: " + normalized);
            }
            source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read script resource: " + normalized, exception);
        }

        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            source = source.replace(replacement.getKey(), replacement.getValue());
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("skript-lane-a-", ".sk");
            Files.writeString(tempFile, source, StandardCharsets.UTF_8);
            runtime.loadFromPath(tempFile);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to materialize script resource: " + normalized, exception);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void assertNumber(GameTestHelper helper, String variable, double expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number && Double.compare(((Number) value).doubleValue(), expected) == 0,
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertLocation(GameTestHelper helper, String variable, double x, double y, double z) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof FabricLocation location
                        && Double.compare(location.position().x, x) == 0
                        && Double.compare(location.position().y, y) == 0
                        && Double.compare(location.position().z, z) == 0,
                Component.literal("Expected " + variable + " to resolve to (" + x + ", " + y + ", " + z + ") but got " + value + ".")
        );
    }

    private static double readHumidity(GameTestHelper helper, BlockPos position) {
        Object biome = helper.getLevel().getBiome(position).value();
        try {
            Object climateSettings = biome.getClass().getMethod("getModifiedClimateSettings").invoke(biome);
            Object downfall = climateSettings.getClass().getMethod("downfall").invoke(climateSettings);
            if (downfall instanceof Number number) {
                return number.doubleValue();
            }
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            var field = biome.getClass().getDeclaredField("climateSettings");
            field.setAccessible(true);
            Object climateSettings = field.get(biome);
            Object downfall = climateSettings.getClass().getMethod("downfall").invoke(climateSettings);
            if (downfall instanceof Number number) {
                return number.doubleValue();
            }
        } catch (ReflectiveOperationException ignored) {
        }
        throw new IllegalStateException("Unable to resolve biome humidity for GameTest assertions.");
    }

    private record LaneAContextHandle(ServerLevel level, BlockPos position) implements FabricBlockEventHandle {
    }

    public static final class GameTestLaneAContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof LaneAContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{LaneAContextHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest lane a context";
        }
    }

    public static final class LaneAWorldExpression extends SimpleExpression<ServerLevel> {
        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) {
            return event.handle() instanceof LaneAContextHandle handle ? new ServerLevel[]{handle.level()} : new ServerLevel[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerLevel> getReturnType() {
            return ServerLevel.class;
        }
    }

    public static final class LaneALocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            if (!(event.handle() instanceof LaneAContextHandle handle)) {
                return new FabricLocation[0];
            }
            BlockPos position = handle.position();
            return new FabricLocation[]{
                    new FabricLocation(handle.level(), new net.minecraft.world.phys.Vec3(position.getX(), position.getY(), position.getZ()))
            };
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricLocation> getReturnType() {
            return FabricLocation.class;
        }
    }

}
