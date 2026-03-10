package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionBlockWorldLocationCompatibilityTest {

    private static boolean syntaxRegistered;
    private static List<SyntaxInfo<?>> originalExpressions = List.of();

    @BeforeAll
    static void bootstrapSyntax() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        originalExpressions = new ArrayList<>();
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            originalExpressions.add(info);
        }
        ensureSyntax();
    }

    @AfterAll
    static void restoreSyntax() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
        for (SyntaxInfo<?> info : originalExpressions) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EXPRESSION, info);
        }
    }

    private static void ensureSyntax() {
        if (!syntaxRegistered) {
            registerClassInfo(FabricLocation.class, "location");
            registerClassInfo(FabricBlock.class, "block");
            registerClassInfo(ServerLevel.class, "world");
            registerBlockStateInfo();
            registerDifficultyInfo();
            Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-e-test-location");
            Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-e-test-world");
            Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-e-test-block");
            Skript.registerExpression(TestBlockStateExpression.class, BlockState.class, "lane-e-test-block-data");
            syntaxRegistered = true;
        }
        new ExprAltitude();
        new ExprBlock();
        new ExprBlockData();
        new ExprCoordinate();
        new ExprDifficulty();
        new ExprDistance();
        new ExprLightLevel();
        new ExprMiddleOfLocation();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static void registerDifficultyInfo() {
        if (Classes.getExactClassInfo(Difficulty.class) == null) {
            Classes.registerClassInfo(new EnumClassInfo<>(Difficulty.class, "difficulty", "types.difficulty"));
        }
    }

    private static void registerBlockStateInfo() {
        if (Classes.getExactClassInfo(BlockState.class) == null) {
            Classes.registerClassInfo(new ClassInfo<>(BlockState.class, "blockdata"));
        }
    }

    @Test
    void fabricLocationValueExpressionsMatchCompatSemantics() {
        FabricLocation location = new FabricLocation(null, new Vec3(12.25D, 64.75D, -8.9D));

        ExprAltitude altitude = new ExprAltitude();
        altitude.init(new Expression[]{new SimpleLiteral<>(location, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult(""));
        assertEquals(64.75D, altitude.getSingle(SkriptEvent.EMPTY));

        ExprDistance distance = new ExprDistance();
        distance.init(new Expression[]{
                new SimpleLiteral<>(location, false),
                new SimpleLiteral<>(new FabricLocation(null, new Vec3(15.25D, 68.75D, -8.9D)), false)
        }, 0, ch.njol.util.Kleenean.FALSE, parseResult(""));
        assertEquals(5.0D, distance.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.000001D);

        ExprMiddleOfLocation middle = new ExprMiddleOfLocation();
        middle.init(new Expression[]{new SimpleLiteral<>(location, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult(""));
        FabricLocation centered = middle.getSingle(SkriptEvent.EMPTY);
        assertNotNull(centered);
        assertEquals(12.5D, centered.position().x);
        assertEquals(64.0D, centered.position().y);
        assertEquals(-8.5D, centered.position().z);
    }

    @Test
    void coordinateExpressionChangesLocationThroughSetter() {
        MutableLocationExpression locationExpression = new MutableLocationExpression(new FabricLocation(null, new Vec3(1.0D, 2.0D, 3.0D)));
        ExprCoordinate coordinate = new ExprCoordinate();
        SkriptParser.ParseResult parse = parseResult("");
        parse.mark = 1;
        assertTrue(coordinate.init(new Expression[]{locationExpression}, 0, ch.njol.util.Kleenean.FALSE, parse));
        assertArrayEquals(new Class[]{Number.class}, coordinate.acceptChange(ChangeMode.SET));

        coordinate.change(SkriptEvent.EMPTY, new Object[]{7}, ChangeMode.SET);
        assertEquals(7.0D, locationExpression.value.position().y);

        coordinate.change(SkriptEvent.EMPTY, new Object[]{2}, ChangeMode.ADD);
        assertEquals(9.0D, locationExpression.value.position().y);
    }

    @Test
    void blockAndWorldExpressionsParseAndBindAsChangeTargets() throws Exception {
        assertInstanceOf(ExprBlock.class, parseExpression("block at lane-e-test-location", FabricBlock.class));
        assertInstanceOf(ExprBlock.class, parseExpressionInEvent("event-block", new Class[]{FabricBlock.class}, TestBlockHandle.class));
        assertInstanceOf(ExprBlockData.class, parseExpression("block data of lane-e-test-block", BlockState.class));
        assertInstanceOf(ExprDifficulty.class, parseExpression("difficulty of lane-e-test-world", Difficulty.class));
        assertInstanceOf(ExprAltitude.class, parseExpression("altitude of lane-e-test-location", Number.class));
        assertInstanceOf(ExprCoordinate.class, parseExpression("x-coordinate of lane-e-test-location", Number.class));
        assertInstanceOf(ExprDistance.class, parseExpression("distance between lane-e-test-location and lane-e-test-location", Number.class));
        assertInstanceOf(ExprLightLevel.class, parseExpression("sky light level of lane-e-test-location", Byte.class));
        assertInstanceOf(ExprMiddleOfLocation.class, parseExpression("center of lane-e-test-location", FabricLocation.class));

        Statement coordinateStatement = parseStatement("set x-coordinate of lane-e-test-location to 2");
        assertInstanceOf(EffChange.class, coordinateStatement);
        assertEquals("the x-coordinate of lane-e-test-location", expression(coordinateStatement, "changed").toString(null, false));

        Statement blockDataStatement = parseStatement("set block data of lane-e-test-block to lane-e-test-block-data");
        assertInstanceOf(EffChange.class, blockDataStatement);
        assertEquals("block data of lane-e-test-block", expression(blockDataStatement, "changed").toString(null, false));
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static Expression<?> parseExpressionInEvent(String input, Class<?>[] returnTypes, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return parseExpression(input, returnTypes);
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }

    private static Statement parseStatement(String input) {
        return Statement.parse(input, "failed");
    }

    private static Expression<?> expression(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return (Expression<?>) field.get(owner);
    }

    private static Field findField(Class<?> owner, String fieldName) throws NoSuchFieldException {
        Class<?> current = owner;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{new FabricLocation(null, new Vec3(1.0D, 2.0D, 3.0D))};
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
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-location";
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

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-world";
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

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-block";
        }
    }

    public static final class TestBlockStateExpression extends SimpleExpression<BlockState> {
        @Override
        protected BlockState @Nullable [] get(SkriptEvent event) {
            return new BlockState[]{Blocks.STONE.defaultBlockState()};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends BlockState> getReturnType() {
            return BlockState.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-block-data";
        }
    }

    private static final class MutableLocationExpression extends SimpleExpression<FabricLocation> {
        private FabricLocation value;

        private MutableLocationExpression(FabricLocation value) {
            this.value = value;
        }

        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{value};
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
            if (mode == ChangeMode.SET && delta != null && delta.length > 0) {
                value = (FabricLocation) delta[0];
            }
        }

        @Override
        public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{FabricLocation.class} : null;
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

    private record TestBlockHandle(ServerLevel level, BlockPos position) implements FabricBlockEventHandle {
    }
}
