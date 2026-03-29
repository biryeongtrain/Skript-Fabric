package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

class ExpressionWorldTimeBundleCompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsPrioritizedWorldSubsetSyntax() {
        assertInstanceOf(ExprWorld.class, parseExpression("world of lane-s2-first-location", ServerLevel.class));
        assertInstanceOf(ExprWorld.class, parseExpression("lane-s2-first-location's world", ServerLevel.class));
        assertInstanceOf(ExprWorld.class, parseExpression("world of lane-s2-first-entity", ServerLevel.class));
        assertInstanceOf(ExprWorld.class, parseExpression("world of lane-s2-first-chunk", ServerLevel.class));
        assertInstanceOf(ExprWorlds.class, parseExpression("all worlds", ServerLevel.class));
        assertInstanceOf(ExprWorldFromName.class, parseExpression("world named lane-s2-first-string", ServerLevel.class));
        assertInstanceOf(ExprWorldEnvironment.class, parseExpression("environment of lane-s2-first-world", String.class));
        assertInstanceOf(ExprTemperature.class, parseExpression("temperature of lane-s2-first-block", Number.class));
        assertInstanceOf(ExprTime.class, parseExpression("time within lane-s2-first-world", Time.class));
    }

    @Test
    void worldBundleFailsClosedWithoutServerContext() {
        assertArrayEquals(new ServerLevel[0], new ExprWorlds().getArray(SkriptEvent.EMPTY));

        ExprWorldFromName worldFromName = new ExprWorldFromName();
        assertTrue(worldFromName.init(
                new Expression[]{new SimpleLiteral<>("overworld", false)},
                0,
                Kleenean.FALSE,
                parseResult("world named overworld")
        ));
        assertNull(worldFromName.getSingle(SkriptEvent.EMPTY));

        ExprWorld world = new ExprWorld();
        assertTrue(world.init(
                new Expression[]{new SimpleLiteral<>(new FabricLocation(null, Vec3.ZERO), false)},
                0,
                Kleenean.FALSE,
                parseResult("world of lane-s2-first-location")
        ));
        assertNull(world.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void worldExpressionMutatesMutableLocations() throws Exception {
        MutableLocationExpression.current = new FabricLocation(null, new Vec3(4.0D, 5.0D, 6.0D));

        ExprWorld world = new ExprWorld();
        assertTrue(world.init(
                new Expression[]{new MutableLocationExpression()},
                0,
                Kleenean.FALSE,
                parseResult("world of lane-s2-first-location")
        ));
        assertArrayEquals(new Class[]{ServerLevel.class}, world.acceptChange(ChangeMode.SET));

        ServerLevel target = allocate(ServerLevel.class);
        world.change(SkriptEvent.EMPTY, new Object[]{target}, ChangeMode.SET);

        assertSame(target, MutableLocationExpression.current.level());
        assertEquals(4.0D, MutableLocationExpression.current.position().x);
        assertEquals(5.0D, MutableLocationExpression.current.position().y);
        assertEquals(6.0D, MutableLocationExpression.current.position().z);
    }

    @Test
    void environmentTemperatureAndTimeHelpersRetainExpectedSemantics() {
        assertEquals("normal", ExprWorldEnvironment.environmentOf(Level.OVERWORLD));
        assertEquals("nether", ExprWorldEnvironment.environmentOf(Level.NETHER));
        assertEquals("the_end", ExprWorldEnvironment.environmentOf(Level.END));
        assertEquals(
                "custom",
                ExprWorldEnvironment.environmentOf(ResourceKey.create(Registries.DIMENSION, Identifier.parse("skript:custom")))
        );

        assertEquals(0.8F, ExprTemperature.temperature(new MethodBackedBiome()));
        assertEquals(1.25D, ExprTemperature.temperature(new FieldBackedBiome()));

        assertEquals(2_500L, ExprTime.ticksForChange(new Time(20_500), ChangeMode.ADD));
        assertEquals(40L, ExprTime.ticksForChange(new Timespan(Timespan.TimePeriod.TICK, 40), ChangeMode.REMOVE));
        assertEquals(7_000L, ExprTime.ticksForChange(new Timeperiod(7_000), ChangeMode.SET));
        assertEquals(73_000L, ExprTime.rebaseTimeOfDay(72_345L, 1_000L));
    }

    @Test
    void timeExpressionAcceptsExpectedChangeTypes() {
        ExprTime time = new ExprTime();
        assertArrayEquals(new Class[]{Time.class, Timespan.class}, time.acceptChange(ChangeMode.ADD));
        assertArrayEquals(new Class[]{Time.class, Timespan.class}, time.acceptChange(ChangeMode.REMOVE));
        assertArrayEquals(new Class[]{Time.class, Timeperiod.class}, time.acceptChange(ChangeMode.SET));
        assertNull(time.acceptChange(ChangeMode.RESET));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(FabricLocation.class, "location");
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(LevelChunk.class, "chunk");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(String.class, "string");

        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-s2-first-world");
        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-s2-first-location");
        Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-s2-first-block");
        Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-s2-first-entity");
        Skript.registerExpression(TestChunkExpression.class, LevelChunk.class, "lane-s2-first-chunk");
        Skript.registerExpression(TestStringExpression.class, String.class, "lane-s2-first-string");

        new ExprWorld();
        new ExprWorlds();
        new ExprWorldFromName();
        new ExprWorldEnvironment();
        new ExprTemperature();
        new ExprTime();
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

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        return (T) unsafe().allocateInstance(type);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class MethodBackedBiome {
        public MethodBackedClimateSettings getModifiedClimateSettings() {
            return new MethodBackedClimateSettings();
        }
    }

    private static final class MethodBackedClimateSettings {
        public float temperature() {
            return 0.8F;
        }
    }

    private static final class FieldBackedBiome {
        private final FieldBackedClimateSettings climateSettings = new FieldBackedClimateSettings();
    }

    private static final class FieldBackedClimateSettings {
        private final double temperature = 1.25D;
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

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{new FabricLocation((ServerLevel) null, Vec3.ZERO)};
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

    public static final class MutableLocationExpression extends SimpleExpression<FabricLocation> {
        private static FabricLocation current = new FabricLocation(null, Vec3.ZERO);

        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{current};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricLocation> getReturnType() {
            return FabricLocation.class;
        }

        @Override
        public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{FabricLocation.class} : null;
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
            if (mode == ChangeMode.SET && delta != null && delta.length > 0 && delta[0] instanceof FabricLocation location) {
                current = location;
            }
        }
    }

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return new FabricBlock[]{new FabricBlock((ServerLevel) null, BlockPos.ZERO)};
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

    public static final class TestStringExpression extends SimpleExpression<String> {
        @Override
        protected String @Nullable [] get(SkriptEvent event) {
            return new String[]{"overworld"};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }
    }
}
