package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.classes.Changer.ChangeMode;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionLocationAndVectorGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);
    private static final Vec3 LOC_VEC_VECTOR = new Vec3(2.0D, 4.0D, 6.0D);
    private static final Vec3 LOC_VEC_SECOND_LOCATION_OFFSET = new Vec3(2.0D, 2.0D, 2.0D);

    @GameTest
    public void laneASpatialLocationExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            loadScripts(
                    runtime,
                    "skript/gametest/expression/location-and-vector/altitude_records_value.sk",
                    "skript/gametest/expression/location-and-vector/location_records_value.sk",
                    "skript/gametest/expression/location-and-vector/location_of_records_value.sk"
            );

            BlockPos relative = new BlockPos(0, 1, 0);
            BlockPos absolute = helper.absolutePos(relative);
            helper.getLevel().setBlockAndUpdate(absolute, Blocks.STONE.defaultBlockState());
            loadTemplateScript(
                    runtime,
                    "skript/gametest/expression/location-and-vector/location_at_records_value.sk",
                    coordinateReplacements("__TARGET_", absolute.getX() + 3, absolute.getY(), absolute.getZ())
            );
            loadTemplateScript(
                    runtime,
                    "skript/gametest/expression/location-and-vector/distance_records_value.sk",
                    coordinateReplacements("__TARGET_", absolute.getX() + 3, absolute.getY(), absolute.getZ())
            );

            int executed = dispatch(runtime, helper, new LocationAndVectorHandle(helper.getLevel(), absolute));
            assertExecuted(helper, executed, 5, "lane A spatial location bundle");

            assertNumber(helper, "locvec::altitude", absolute.getY());
            assertLocation(helper, "locvec::location", absolute.getX(), absolute.getY(), absolute.getZ());
            assertLocation(helper, "locvec::location_of", absolute.getX(), absolute.getY(), absolute.getZ());
            assertLocation(helper, "locvec::location_at", absolute.getX() + 3.0D, absolute.getY(), absolute.getZ());
            assertNumber(helper, "locvec::distance", 3.0D);
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
                    "skript/gametest/expression/location-and-vector/block_marks_above.sk",
                    "skript/gametest/expression/location-and-vector/block_data_copies_neighbor.sk",
                    "skript/gametest/expression/location-and-vector/chunk_x_records_value.sk",
                    "skript/gametest/expression/location-and-vector/chunk_z_records_value.sk"
            );

            BlockPos relative = new BlockPos(1, 1, 0);
            BlockPos absolute = helper.absolutePos(relative);
            helper.getLevel().setBlockAndUpdate(absolute, Blocks.GOLD_BLOCK.defaultBlockState());

            int executed = dispatch(runtime, helper, new LocationAndVectorHandle(helper.getLevel(), absolute));
            assertExecuted(helper, executed, 4, "lane A block and chunk bundle");

            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, relative.above());
            helper.assertTrue(
                    Variables.getVariable("locvec::event_block_data", null, false) instanceof BlockState state
                            && state.is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected block data expression to read the event block's gold block state.")
            );
            assertNumber(helper, "locvec::chunk_x", helper.getLevel().getChunkAt(absolute).getPos().x());
            assertNumber(helper, "locvec::chunk_z", helper.getLevel().getChunkAt(absolute).getPos().z());
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

            BlockPos originalSpawn = helper.getLevel().getRespawnData().pos();
            LocationAndVectorHandle handle = new LocationAndVectorHandle(helper.getLevel(), absolute);

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/difficulty_records_value.sk", "lane A difficulty");

            helper.assertTrue(
                    Variables.getVariable("locvec::difficulty", null, false) == helper.getLevel().getDifficulty(),
                    Component.literal("Expected difficulty expression to expose the world difficulty.")
            );

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/humidity_records_value.sk", "lane A humidity");
            Object humidity = Variables.getVariable("locvec::humidity", null, false);
            helper.assertTrue(
                    humidity instanceof Number
                            && Double.compare(((Number) humidity).doubleValue(), readHumidity(helper, absolute)) == 0,
                    Component.literal("Expected humidity expression to record the biome humidity at the event block.")
            );

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/light_level_records_value.sk", "lane A light level");
            assertNumber(helper, "locvec::light_level", helper.getLevel().getBrightness(LightLayer.BLOCK, absolute));

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/redstone_power_records_value.sk", "lane A redstone power");
            assertNumber(helper, "locvec::redstone_power", helper.getLevel().getBestNeighborSignal(absolute));

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/sea_level_records_value.sk", "lane A sea level");
            assertNumber(helper, "locvec::sea_level", helper.getLevel().getSeaLevel());

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/seed_records_value.sk", "lane A seed");
            assertNumber(helper, "locvec::seed", helper.getLevel().getSeed());

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/simulation_distance_records_value.sk", "lane A simulation distance");
            assertNumber(helper, "locvec::simulation_distance", helper.getLevel().getServer().getPlayerList().getSimulationDistance());

            executeTemplateScript(
                    runtime,
                    helper,
                    handle,
                    "skript/gametest/expression/location-and-vector/spawn_changes_world_spawn.sk",
                    coordinateReplacements("__SPAWN_", absolute.getX() + 7, absolute.getY() + 1, absolute.getZ()),
                    "lane A spawn"
            );
            BlockPos expectedSpawn = new BlockPos(absolute.getX() + 7, absolute.getY() + 1, absolute.getZ());
            helper.assertTrue(
                    helper.getLevel().getRespawnData().pos().equals(expectedSpawn),
                    Component.literal("Expected spawn location expression to update the world spawn.")
            );
            helper.getLevel().setRespawnData(net.minecraft.world.level.storage.LevelData.RespawnData.of(helper.getLevel().dimension(), originalSpawn, 0.0F, 0.0F));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void laneAVectorAndLocationExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos relative = new BlockPos(3, 1, 0);
            BlockPos absolute = helper.absolutePos(relative);
            LocationAndVectorHandle handle = new LocationAndVectorHandle(helper.getLevel(), absolute);

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/location_from_vector_records_value.sk", "lane A location from vector");
            assertLocation(helper, "locvec::location_from_vector", LOC_VEC_VECTOR.x, LOC_VEC_VECTOR.y, LOC_VEC_VECTOR.z);

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/location_offset_records_value.sk", "lane A location offset");
            assertLocation(
                    helper,
                    "locvec::location_offset",
                    absolute.getX() + LOC_VEC_VECTOR.x,
                    absolute.getY() + LOC_VEC_VECTOR.y,
                    absolute.getZ() + LOC_VEC_VECTOR.z
            );

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/midpoint_records_value.sk", "lane A midpoint");
            assertLocation(helper, "locvec::midpoint", absolute.getX() + 1.0D, absolute.getY() + 1.0D, absolute.getZ() + 1.0D);

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/vector_between_locations_records_value.sk", "lane A vector between locations");
            assertVector(helper, "locvec::vector_between", 2.0D, 2.0D, 2.0D);

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/vector_cross_records_value.sk", "lane A vector cross");
            assertVector(helper, "locvec::vector_cross", 0.0D, 0.0D, 1.0D);

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/vector_dot_records_value.sk", "lane A vector dot");
            assertApproxNumber(helper, "locvec::vector_dot", 32.0D);

            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/vector_normalize_records_value.sk", "lane A vector normalize");
            assertVector(helper, "locvec::vector_normalized", 0.0D, 0.6D, 0.8D);

            LocVecMutableVectorExpression.current = new Vec3(1.0D, 2.0D, 2.0D);
            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/vector_length_changes_mutable_vector.sk", "lane A vector length");
            assertApproxNumber(helper, "locvec::vector_length", 5.0D);
            assertVector(helper, "locvec::vector_after_length", 5.0D / 3.0D, 10.0D / 3.0D, 10.0D / 3.0D);

            LocVecMutableVectorExpression.current = new Vec3(1.0D, 2.0D, 2.0D);
            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/vector_x_component_changes_mutable_vector.sk", "lane A x component");
            assertApproxNumber(helper, "locvec::vector_x_component", 7.0D);
            assertVector(helper, "locvec::vector_after_x_component", 7.0D, 2.0D, 2.0D);

            LocVecMutableVectorExpression.current = new Vec3(0.0D, 0.0D, 1.0D);
            executeScript(runtime, helper, handle, "skript/gametest/expression/location-and-vector/yaw_pitch_changes_mutable_vector.sk", "lane A yaw pitch");
            assertApproxNumber(helper, "locvec::vector_pitch", -90.0D);
            assertApproxNumber(helper, "locvec::vector_yaw_after", 180.0D);
            assertVector(helper, "locvec::vector_after_yaw", 0.0D, 0.0D, -1.0D);

            runtime.clearScripts();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestLocationAndVectorEvent.class, "gametest location and vector context");
        Skript.registerExpression(LocVecLocationExpression.class, FabricLocation.class, "locvec-location");
        Skript.registerExpression(LocVecSecondLocationExpression.class, FabricLocation.class, "locvec-second-location");
        Skript.registerExpression(LocVecVectorExpression.class, Vec3.class, "locvec-vector");
        Skript.registerExpression(LocVecMutableVectorExpression.class, Vec3.class, "locvec-mutable-vector");
        Skript.registerExpression(LocVecWorldExpression.class, ServerLevel.class, "locvec-world");
    }

    private static void loadScripts(SkriptRuntime runtime, String... paths) {
        for (String path : paths) {
            runtime.loadFromResource(path);
        }
    }

    private static void executeScript(SkriptRuntime runtime, GameTestHelper helper, LocationAndVectorHandle handle, String path, String description) {
        runtime.clearScripts();
        runtime.loadFromResource(path);
        int executed = dispatch(runtime, helper, handle);
        assertExecuted(helper, executed, 1, description);
    }

    private static void executeTemplateScript(
            SkriptRuntime runtime,
            GameTestHelper helper,
            LocationAndVectorHandle handle,
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
            classLoader = SkriptFabricExpressionLocationAndVectorGameTest.class.getClassLoader();
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
            tempFile = Files.createTempFile("skript-location-and-vector-", ".sk");
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

    private static void assertApproxNumber(GameTestHelper helper, String variable, double expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number && Math.abs(((Number) value).doubleValue() - expected) < 0.0001D,
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertLocation(GameTestHelper helper, String variable, double x, double y, double z) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof FabricLocation location
                        && Math.abs(location.position().x - x) < 0.0001D
                        && Math.abs(location.position().y - y) < 0.0001D
                        && Math.abs(location.position().z - z) < 0.0001D,
                Component.literal("Expected " + variable + " to resolve to (" + x + ", " + y + ", " + z + ") but got " + value + ".")
        );
    }

    private static void assertVector(GameTestHelper helper, String variable, double x, double y, double z) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Vec3 vector
                        && Math.abs(vector.x - x) < 0.0001D
                        && Math.abs(vector.y - y) < 0.0001D
                        && Math.abs(vector.z - z) < 0.0001D,
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

    private record LocationAndVectorHandle(ServerLevel level, BlockPos position) implements FabricBlockEventHandle {
    }

    public static final class GameTestLocationAndVectorEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof LocationAndVectorHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{LocationAndVectorHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest location and vector context";
        }
    }

    public static final class LocVecWorldExpression extends SimpleExpression<ServerLevel> {
        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) {
            return event.handle() instanceof LocationAndVectorHandle handle ? new ServerLevel[]{handle.level()} : new ServerLevel[0];
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

    public static final class LocVecSecondLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            if (!(event.handle() instanceof LocationAndVectorHandle handle)) {
                return new FabricLocation[0];
            }
            BlockPos position = handle.position();
            return new FabricLocation[]{
                    new FabricLocation(
                            handle.level(),
                            new Vec3(
                                    position.getX() + LOC_VEC_SECOND_LOCATION_OFFSET.x,
                                    position.getY() + LOC_VEC_SECOND_LOCATION_OFFSET.y,
                                    position.getZ() + LOC_VEC_SECOND_LOCATION_OFFSET.z
                            )
                    )
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

    public static final class LocVecLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            if (!(event.handle() instanceof LocationAndVectorHandle handle)) {
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

    public static final class LocVecVectorExpression extends SimpleExpression<Vec3> {
        @Override
        protected Vec3 @Nullable [] get(SkriptEvent event) {
            return new Vec3[]{LOC_VEC_VECTOR};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Vec3> getReturnType() {
            return Vec3.class;
        }
    }

    public static final class LocVecMutableVectorExpression extends SimpleExpression<Vec3> {
        private static Vec3 current = new Vec3(1.0D, 2.0D, 2.0D);

        @Override
        protected Vec3 @Nullable [] get(SkriptEvent event) {
            return new Vec3[]{current};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Vec3> getReturnType() {
            return Vec3.class;
        }

        @Override
        public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{Vec3.class} : null;
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
            if (mode == ChangeMode.SET && delta != null && delta.length > 0 && delta[0] instanceof Vec3 vector) {
                current = vector;
            }
        }
    }

}
