package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprAltitude;
import ch.njol.skript.expressions.ExprBlock;
import ch.njol.skript.expressions.ExprBlockData;
import ch.njol.skript.expressions.ExprChunk;
import ch.njol.skript.expressions.ExprChunkX;
import ch.njol.skript.expressions.ExprChunkZ;
import ch.njol.skript.expressions.ExprDifficulty;
import ch.njol.skript.expressions.ExprDistance;
import ch.njol.skript.expressions.ExprHumidity;
import ch.njol.skript.expressions.ExprLightLevel;
import ch.njol.skript.expressions.ExprLocation;
import ch.njol.skript.expressions.ExprLocationAt;
import ch.njol.skript.expressions.ExprLocationOf;
import ch.njol.skript.expressions.ExprRedstoneBlockPower;
import ch.njol.skript.expressions.ExprSeaLevel;
import ch.njol.skript.expressions.ExprSeed;
import ch.njol.skript.expressions.ExprSimulationDistance;
import ch.njol.skript.expressions.ExprSpawn;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
final class ExpressionLaneABindingTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void laneASpatialExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprAltitude.class, parseExpression("altitude of lane-a-location", Number.class));
        assertInstanceOf(ExprBlock.class, parseExpression("block north of lane-a-location", FabricBlock.class));
        assertInstanceOf(ExprBlockData.class, parseExpression("block data of block north of lane-a-location", BlockState.class));
        assertInstanceOf(ExprChunk.class, parseExpression("chunk of lane-a-location", LevelChunk.class));
        assertInstanceOf(ExprChunkX.class, parseExpression("chunk x-coordinate of chunk of lane-a-location", Number.class));
        assertInstanceOf(ExprChunkZ.class, parseExpression("chunk z-coordinate of chunk of lane-a-location", Number.class));
        assertInstanceOf(ExprDifficulty.class, parseExpression("difficulty of lane-a-world", net.minecraft.world.Difficulty.class));
        assertInstanceOf(ExprDistance.class, parseExpression(
                "distance between lane-a-location and location at x = 1, y = 2, and z = 3 in world lane-a-world",
                Number.class
        ));
        assertInstanceOf(ExprDistance.class, parseExpression(
                "distance between lane-a-location and location at x = {lanea::target_x}, y = {lanea::target_y}, and z = {lanea::target_z} in world lane-a-world",
                Number.class
        ));
        assertInstanceOf(ExprHumidity.class, parseExpression("humidity of block north of lane-a-location", Number.class));
        assertInstanceOf(ExprLightLevel.class, parseExpression("block light level of lane-a-location", Byte.class));
        assertInstanceOf(ExprLocation.class, parseExpression("location north of lane-a-location", FabricLocation.class));
        assertInstanceOf(ExprLocationAt.class, parseExpression(
                "location at x = 1, y = 2, and z = 3 in world lane-a-world",
                FabricLocation.class
        ));
        assertInstanceOf(ExprLocationAt.class, parseExpression(
                "location at x = {lanea::target_x}, y = {lanea::target_y}, and z = {lanea::target_z} in world lane-a-world",
                FabricLocation.class
        ));
        assertInstanceOf(ExprLocationOf.class, parseExpression("location of block north of lane-a-location", FabricLocation.class));
        assertInstanceOf(ExprRedstoneBlockPower.class, parseExpression("redstone power of block north of lane-a-location", Long.class));
        assertInstanceOf(ExprSeaLevel.class, parseExpression("sea level of lane-a-world", Long.class));
        assertInstanceOf(ExprSeed.class, parseExpression("seed of lane-a-world", Long.class));
        assertInstanceOf(ExprSimulationDistance.class, parseExpression("simulation distance of lane-a-world", Integer.class));
        assertInstanceOf(ExprSpawn.class, parseExpression("spawn location of lane-a-world", FabricLocation.class));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(FabricLocation.class, "location");
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(LevelChunk.class, "chunk");
        registerClassInfo(BlockState.class, "blockstate");

        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-a-location");
        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-a-world");
        syntaxRegistered = true;
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[0];
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

    public static final class TestWorldExpression extends SimpleExpression<ServerLevel> {
        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) {
            return new ServerLevel[0];
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
}
