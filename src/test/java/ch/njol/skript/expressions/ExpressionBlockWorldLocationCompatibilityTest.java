package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.MoonPhase;
import ch.njol.util.Kleenean;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ExpressionBlockWorldLocationCompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ensureSyntax();
    }

    @Test
    void localLocationExpressionsOperateWithoutWorldContext() {
        FabricLocation location = new FabricLocation(null, new Vec3(10.25, 64.75, -2.5));

        ExprAltitude altitude = new ExprAltitude();
        altitude.init(new Expression[]{new SimpleLiteral<>(location, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(64.75, altitude.getSingle(SkriptEvent.EMPTY));

        ExprCoordinate x = new ExprCoordinate();
        SkriptParser.ParseResult xParse = parseResult("");
        xParse.mark = 0;
        x.init(new Expression[]{new SimpleLiteral<>(location, false)}, 0, Kleenean.FALSE, xParse);
        assertEquals(10.25, x.getSingle(SkriptEvent.EMPTY));

        ExprMiddleOfLocation middle = new ExprMiddleOfLocation();
        middle.init(new Expression[]{new SimpleLiteral<>(location, false)}, 0, Kleenean.FALSE, parseResult(""));
        FabricLocation centered = middle.getSingle(SkriptEvent.EMPTY);
        assertNotNull(centered);
        assertEquals(10.5, centered.position().x);
        assertEquals(64.0, centered.position().y);
        assertEquals(-2.5, centered.position().z);

        ExprDistance distance = new ExprDistance();
        distance.init(new Expression[]{
                new SimpleLiteral<>(new FabricLocation(null, new Vec3(0, 64, 0)), false),
                new SimpleLiteral<>(new FabricLocation(null, new Vec3(3, 68, 4)), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(6.4031242374328485, distance.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void directionAndSoundExpressionsUseCompatTypes() {
        ExprDirection north = new ExprDirection();
        SkriptParser.ParseResult northParse = parseResult("");
        northParse.mark = 0;
        north.init(new Expression[]{new SimpleLiteral<>(2, false)}, 0, Kleenean.FALSE, northParse);
        Direction direction = north.getSingle(SkriptEvent.EMPTY);
        assertNotNull(direction);
        assertEquals(-2.0, direction.getDirection().z);

        ExprBlockSound sound = new ExprBlockSound();
        SkriptParser.ParseResult soundParse = parseResult("");
        soundParse.mark = 1;
        sound.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STONE), false)}, 0, Kleenean.FALSE, soundParse);
        String resolved = sound.getSingle(SkriptEvent.EMPTY);
        assertTrue(resolved != null && !resolved.isBlank());
    }

    @Test
    void importedExpressionsInstantiate() {
        assertDoesNotThrow(ExprAbsorbedBlocks::new);
        assertDoesNotThrow(ExprAltitude::new);
        assertDoesNotThrow(ExprAttachedBlock::new);
        assertDoesNotThrow(ExprBed::new);
        assertDoesNotThrow(ExprBiome::new);
        assertDoesNotThrow(ExprBlock::new);
        assertDoesNotThrow(ExprBlockData::new);
        assertDoesNotThrow(ExprBlockSound::new);
        assertDoesNotThrow(ExprBlocks::new);
        assertDoesNotThrow(ExprChunk::new);
        assertDoesNotThrow(ExprCoordinate::new);
        assertDoesNotThrow(ExprDifficulty::new);
        assertDoesNotThrow(ExprDirection::new);
        assertDoesNotThrow(ExprDistance::new);
        assertDoesNotThrow(ExprDustedStage::new);
        assertDoesNotThrow(ExprFacing::new);
        assertDoesNotThrow(ExprHumidity::new);
        assertDoesNotThrow(ExprLocation::new);
        assertDoesNotThrow(ExprLocationAt::new);
        assertDoesNotThrow(ExprLocationOf::new);
        assertDoesNotThrow(ExprLightLevel::new);
        assertDoesNotThrow(ExprMiddleOfLocation::new);
        assertDoesNotThrow(ExprMoonPhase::new);
        assertDoesNotThrow(ExprPushedBlocks::new);
        assertDoesNotThrow(ExprRedstoneBlockPower::new);
        assertDoesNotThrow(ExprSeaLevel::new);
        assertDoesNotThrow(ExprSeed::new);
        assertDoesNotThrow(ExprSimulationDistance::new);
        assertDoesNotThrow(ExprSpawn::new);
        assertDoesNotThrow(ExprChunkX::new);
        assertDoesNotThrow(ExprChunkZ::new);
    }

    @Test
    void parserBindsImportedBlockWorldLocationSyntax() {
        assertInstanceOf(ExprAltitude.class, parseExpression("altitude of lane-m5-location", Number.class));
        assertInstanceOf(ExprBiome.class, parseExpression("biome of lane-m5-location", net.minecraft.world.level.biome.Biome.class));
        assertInstanceOf(ExprBlockData.class, parseExpression("block data of lane-m5-block", net.minecraft.world.level.block.state.BlockState.class));
        assertInstanceOf(ExprBlockSound.class, parseExpression("break sound of stone", String.class));
        assertInstanceOf(ExprChunk.class, parseExpression("chunk of lane-m5-location", LevelChunk.class));
        assertInstanceOf(ExprChunkX.class, parseExpression("chunk x-coordinate of lane-m5-chunk", Number.class));
        assertInstanceOf(ExprChunkZ.class, parseExpression("chunk z-coordinate of lane-m5-chunk", Number.class));
        assertInstanceOf(ExprCoordinate.class, parseExpression("x-coordinate of lane-m5-location", Number.class));
        assertInstanceOf(ExprDifficulty.class, parseExpression("difficulty of lane-m5-world", net.minecraft.world.Difficulty.class));
        assertInstanceOf(ExprDirection.class, parseExpression("north", Direction.class));
        assertInstanceOf(ExprDistance.class, parseExpression("distance between lane-m5-location and lane-m5-location", Number.class));
        assertInstanceOf(ExprDustedStage.class, parseExpression("dusted stage of lane-m5-block", Integer.class));
        assertInstanceOf(ExprFacing.class, parseExpression("horizontal facing of lane-m5-entity", Direction.class));
        assertInstanceOf(ExprHumidity.class, parseExpression("humidity of lane-m5-block", Number.class));
        assertInstanceOf(ExprLightLevel.class, parseExpression("block light level of lane-m5-location", Byte.class));
        assertInstanceOf(ExprLocationAt.class, parseExpression("location at x = 1, y = 2, and z = 3 in world lane-m5-world", FabricLocation.class));
        assertInstanceOf(ExprLocationOf.class, parseExpression("location of lane-m5-block", FabricLocation.class));
        assertInstanceOf(ExprMiddleOfLocation.class, parseExpression("center of lane-m5-location", FabricLocation.class));
        assertInstanceOf(ExprMoonPhase.class, parseExpression("moon phase of lane-m5-world", MoonPhase.class));
        assertInstanceOf(ExprAttachedBlock.class, parseExpression("attached blocks of lane-m5-projectile", FabricBlock.class));
        assertInstanceOf(ExprRedstoneBlockPower.class, parseExpression("redstone power of lane-m5-block", Long.class));
        assertInstanceOf(ExprSeaLevel.class, parseExpression("sea level of lane-m5-world", Long.class));
        assertInstanceOf(ExprSeed.class, parseExpression("seed of lane-m5-world", Long.class));
        assertInstanceOf(ExprSimulationDistance.class, parseExpression("simulation distance of lane-m5-world", Integer.class));
        assertInstanceOf(ExprSpawn.class, parseExpression("spawn location of lane-m5-world", FabricLocation.class));
    }

    @Test
    void helperExpressionsRetainExpectedLocalContracts() {
        FabricLocation built = parseExpression(
                "location at x = 1, y = 2, and z = 3",
                ExprLocationAt.class
        ).getSingle(SkriptEvent.EMPTY);
        assertNotNull(built);
        assertEquals(1.0, built.position().x);
        assertEquals(2.0, built.position().y);
        assertEquals(3.0, built.position().z);
        assertNull(built.level());

        ExprLocationOf locationOf = new ExprLocationOf();
        assertTrue(locationOf.init(new Expression[]{new SimpleLiteral<>(new FabricBlock(null, new BlockPos(4, 5, 6)), false)}, 0, Kleenean.FALSE, parseResult("")));
        FabricLocation blockLocation = locationOf.getSingle(SkriptEvent.EMPTY);
        assertNotNull(blockLocation);
        assertEquals(4.0, blockLocation.position().x);
        assertEquals(5.0, blockLocation.position().y);
        assertEquals(6.0, blockLocation.position().z);

        ExprSimulationDistance simulationDistance = new ExprSimulationDistance();
        assertInstanceOf(Class[].class, simulationDistance.acceptChange(ChangeMode.SET));
        assertEquals(Integer.class, simulationDistance.acceptChange(ChangeMode.SET)[0]);
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(FabricLocation.class, "location");
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(LevelChunk.class, "chunk");
        registerClassInfo(Direction.class, "direction");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(Projectile.class, "projectile");
        registerClassInfo(net.minecraft.world.level.block.state.BlockState.class, "blockstate");
        registerClassInfo(net.minecraft.world.Difficulty.class, "difficulty");
        registerClassInfo(net.minecraft.world.level.biome.Biome.class, "biome");
        registerClassInfo(MoonPhase.class, "moonphase");

        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-m5-location");
        Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-m5-block");
        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-m5-world");
        Skript.registerExpression(TestChunkExpression.class, LevelChunk.class, "lane-m5-chunk");
        Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-m5-entity");
        Skript.registerExpression(TestProjectileExpression.class, Projectile.class, "lane-m5-projectile");

        new ExprAbsorbedBlocks();
        new ExprAltitude();
        new ExprAttachedBlock();
        new ExprBed();
        new ExprBiome();
        new ExprBlock();
        new ExprBlockData();
        new ExprBlockSound();
        new ExprBlocks();
        new ExprChunk();
        new ExprCoordinate();
        new ExprDifficulty();
        new ExprDirection();
        new ExprDistance();
        new ExprDustedStage();
        new ExprFacing();
        new ExprHumidity();
        new ExprLocation();
        new ExprLocationAt();
        new ExprLocationOf();
        new ExprLightLevel();
        new ExprMiddleOfLocation();
        new ExprMoonPhase();
        new ExprPushedBlocks();
        new ExprRedstoneBlockPower();
        new ExprSeaLevel();
        new ExprSeed();
        new ExprSimulationDistance();
        new ExprSpawn();
        new ExprChunkX();
        new ExprChunkZ();
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

    private static <T extends Expression<?>> T parseExpression(String input, Class<T> type) {
        Expression<?> parsed = parseExpression(input, Object.class);
        assertInstanceOf(type, parsed);
        return type.cast(parsed);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{new FabricLocation(null, Vec3.ZERO)};
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

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return new FabricBlock[]{new FabricBlock(null, BlockPos.ZERO)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
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

    public static final class TestChunkExpression extends SimpleExpression<LevelChunk> {
        @Override
        protected LevelChunk @Nullable [] get(SkriptEvent event) {
            return new LevelChunk[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends LevelChunk> getReturnType() {
            return LevelChunk.class;
        }
    }

    public static final class TestEntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return new Entity[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Entity> getReturnType() {
            return Entity.class;
        }
    }

    public static final class TestProjectileExpression extends SimpleExpression<Projectile> {
        @Override
        protected Projectile @Nullable [] get(SkriptEvent event) {
            return new Projectile[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Projectile> getReturnType() {
            return Projectile.class;
        }
    }
}
