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
import ch.njol.skript.expressions.ExprLocationFromVector;
import ch.njol.skript.expressions.ExprLocationOf;
import ch.njol.skript.expressions.ExprLocationVectorOffset;
import ch.njol.skript.expressions.ExprMidpoint;
import ch.njol.skript.expressions.ExprRedstoneBlockPower;
import ch.njol.skript.expressions.ExprSeaLevel;
import ch.njol.skript.expressions.ExprSeed;
import ch.njol.skript.expressions.ExprSimulationDistance;
import ch.njol.skript.expressions.ExprSpawn;
import ch.njol.skript.expressions.ExprVectorBetweenLocations;
import ch.njol.skript.expressions.ExprVectorCrossProduct;
import ch.njol.skript.expressions.ExprVectorDotProduct;
import ch.njol.skript.expressions.ExprVectorLength;
import ch.njol.skript.expressions.ExprVectorNormalize;
import ch.njol.skript.expressions.ExprXYZComponent;
import ch.njol.skript.expressions.ExprYawPitch;
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
import net.minecraft.world.phys.Vec3;
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
    void laneAVectorLocationExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprLocationFromVector.class, parseExpression("location of lane-a-vector in lane-a-world", FabricLocation.class));
        assertInstanceOf(ExprLocationVectorOffset.class, parseExpression("lane-a-location offset by lane-a-vector", FabricLocation.class));
        assertInstanceOf(ExprMidpoint.class, parseExpression("midpoint between lane-a-location and lane-a-second-location", Object.class));
        assertInstanceOf(ExprVectorBetweenLocations.class, parseExpression("vector from lane-a-location to lane-a-second-location", Vec3.class));
        assertInstanceOf(ExprVectorCrossProduct.class, parseExpression("vector(1, 0, 0) cross vector(0, 1, 0)", Vec3.class));
        assertInstanceOf(ExprVectorDotProduct.class, parseExpression("vector(1, 2, 3) dot vector(4, 5, 6)", Number.class));
        assertInstanceOf(ExprVectorLength.class, parseExpression("vector length of lane-a-mutable-vector", Number.class));
        assertInstanceOf(ExprVectorNormalize.class, parseExpression("normalized vector(0, 3, 4)", Vec3.class));
        assertInstanceOf(ExprXYZComponent.class, parseExpression("x component of lane-a-mutable-vector", Number.class));
        assertInstanceOf(ExprYawPitch.class, parseExpression("yaw of lane-a-mutable-vector", Float.class));
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
        registerClassInfo(Vec3.class, "vector");

        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-a-location");
        Skript.registerExpression(TestSecondLocationExpression.class, FabricLocation.class, "lane-a-second-location");
        Skript.registerExpression(TestVectorExpression.class, Vec3.class, "lane-a-vector");
        Skript.registerExpression(MutableVectorExpression.class, Vec3.class, "lane-a-mutable-vector");
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

    public static final class TestSecondLocationExpression extends SimpleExpression<FabricLocation> {
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

    public static final class TestVectorExpression extends SimpleExpression<Vec3> {
        @Override
        protected Vec3 @Nullable [] get(SkriptEvent event) {
            return new Vec3[0];
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

    public static final class MutableVectorExpression extends SimpleExpression<Vec3> {
        @Override
        protected Vec3 @Nullable [] get(SkriptEvent event) {
            return new Vec3[0];
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
}
