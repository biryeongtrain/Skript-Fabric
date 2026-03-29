package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionLaneACompatibilityTest {

    private static final Vec3 UNIT_VECTOR = new Vec3(2.0D, 4.0D, 6.0D);
    private static final FabricLocation UNIT_LOCATION = new FabricLocation(null, UNIT_VECTOR);
    private static final Vec3 SECOND_VECTOR = new Vec3(4.0D, 6.0D, 8.0D);
    private static final FabricLocation SECOND_LOCATION = new FabricLocation(null, SECOND_VECTOR);

    private static boolean syntaxRegistered;
    private static List<SyntaxInfo<?>> originalExpressions = List.of();

    @BeforeAll
    static void bootstrapSyntax() throws Exception {
        TestBootstrap.bootstrap();
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

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
        MutableVectorExpression.current = new Vec3(1.0D, 2.0D, 2.0D);
    }

    @Test
    void laneAExpressionsParseWithRegisteredSources() {
        assertInstanceOf(ExprLocationFromVector.class, parseExpression("location of lane-a-unit-vector in lane-a-unit-world", FabricLocation.class));
        assertInstanceOf(ExprLocationVectorOffset.class, parseExpression("lane-a-unit-location offset by vector(1, 2, 3)", FabricLocation.class));
        assertInstanceOf(ExprMidpoint.class, parseExpression(
                "midpoint between lane-a-unit-location and lane-a-second-location",
                Object.class
        ));
        assertInstanceOf(ExprVectorBetweenLocations.class, parseExpression(
                "vector from lane-a-unit-location to lane-a-second-location",
                Vec3.class
        ));
        assertInstanceOf(ExprVectorCrossProduct.class, parseExpression("vector(1, 0, 0) cross vector(0, 1, 0)", Vec3.class));
        assertInstanceOf(ExprVectorDotProduct.class, parseExpression("vector(1, 2, 3) dot vector(4, 5, 6)", Number.class));
        assertInstanceOf(ExprVectorLength.class, parseExpression("vector length of lane-a-mutable-vector", Number.class));
        assertInstanceOf(ExprVectorNormalize.class, parseExpression("normalized vector(0, 3, 4)", Vec3.class));
        assertInstanceOf(ExprYawPitch.class, parseExpression("yaw of vector(0, 0, 1)", Float.class));
    }

    @Test
    void vectorAndLocationExpressionsProduceExpectedValues() {
        ExprLocationVectorOffset offset = assertInstanceOf(
                ExprLocationVectorOffset.class,
                parseExpression("lane-a-unit-location offset by vector(1, 2, 3)", FabricLocation.class)
        );
        FabricLocation offsetLocation = offset.getSingle(SkriptEvent.EMPTY);
        assertNotNull(offsetLocation);
        assertVec3(offsetLocation.position(), 3.0D, 6.0D, 9.0D);

        ExprMidpoint locationMidpoint = assertInstanceOf(
                ExprMidpoint.class,
                parseExpression("midpoint between lane-a-unit-location and lane-a-second-location", Object.class)
        );
        FabricLocation midpointLocation = (FabricLocation) locationMidpoint.getSingle(SkriptEvent.EMPTY);
        assertNotNull(midpointLocation);
        assertVec3(midpointLocation.position(), 3.0D, 5.0D, 7.0D);

        ExprMidpoint vectorMidpoint = assertInstanceOf(
                ExprMidpoint.class,
                parseExpression("midpoint between vector(2, 4, 6) and vector(4, 6, 8)", Object.class)
        );
        Vec3 midpointVector = (Vec3) vectorMidpoint.getSingle(SkriptEvent.EMPTY);
        assertNotNull(midpointVector);
        assertVec3(midpointVector, 3.0D, 5.0D, 7.0D);

        ExprVectorBetweenLocations between = assertInstanceOf(
                ExprVectorBetweenLocations.class,
                parseExpression("vector from lane-a-unit-location to lane-a-second-location", Vec3.class)
        );
        assertVec3(between.getSingle(SkriptEvent.EMPTY), 2.0D, 2.0D, 2.0D);

        ExprVectorCrossProduct cross = assertInstanceOf(
                ExprVectorCrossProduct.class,
                parseExpression("vector(1, 0, 0) cross vector(0, 1, 0)", Vec3.class)
        );
        assertVec3(cross.getSingle(SkriptEvent.EMPTY), 0.0D, 0.0D, 1.0D);

        ExprVectorDotProduct dot = assertInstanceOf(
                ExprVectorDotProduct.class,
                parseExpression("vector(1, 2, 3) dot vector(4, 5, 6)", Number.class)
        );
        assertEquals(32.0D, dot.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.00001D);

        ExprVectorNormalize normalize = assertInstanceOf(
                ExprVectorNormalize.class,
                parseExpression("normalized vector(0, 3, 4)", Vec3.class)
        );
        assertVec3(normalize.getSingle(SkriptEvent.EMPTY), 0.0D, 0.6D, 0.8D);

        ExprYawPitch yaw = assertInstanceOf(ExprYawPitch.class, parseExpression("yaw of vector(0, 0, 1)", Float.class));
        ExprYawPitch pitch = assertInstanceOf(ExprYawPitch.class, parseExpression("pitch of vector(0, 1, 0)", Float.class));
        assertEquals(0.0F, yaw.getSingle(SkriptEvent.EMPTY), 0.0001F);
        assertEquals(-90.0F, pitch.getSingle(SkriptEvent.EMPTY), 0.0001F);
    }

    @Test
    void changeCapableVectorExpressionsMutateMutableSources() {
        ExprVectorLength length = assertInstanceOf(
                ExprVectorLength.class,
                parseExpression("vector length of lane-a-mutable-vector", Number.class)
        );
        assertEquals(3.0D, length.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.00001D);
        length.change(SkriptEvent.EMPTY, new Object[]{5.0D}, ChangeMode.SET);
        assertEquals(5.0D, MutableVectorExpression.current.length(), 0.00001D);
        MutableVectorExpression.current = new Vec3(1.0D, 2.0D, 2.0D);

        ExprXYZComponent xComponent = new ExprXYZComponent();
        ParseResult xResult = parseResult("x component", "x");
        assertTrue(xComponent.init(new Expression[]{new MutableVectorExpression()}, 0, ch.njol.util.Kleenean.FALSE, xResult));
        assertEquals(1.0D, xComponent.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.00001D);
        xComponent.change(SkriptEvent.EMPTY, new Object[]{7.0D}, ChangeMode.SET);
        assertVec3(MutableVectorExpression.current, 7.0D, 2.0D, 2.0D);

        ExprXYZComponent wComponent = new ExprXYZComponent();
        ParseResult wResult = parseResult("w component", "w");
        assertTrue(wComponent.init(new Expression[]{new FixedQuaternionExpression()}, 0, ch.njol.util.Kleenean.FALSE, wResult));
        assertEquals(4.0D, wComponent.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.00001D);

        ExprYawPitch yaw = new ExprYawPitch();
        ParseResult yawResult = parseResult("yaw", "yaw");
        assertTrue(yaw.init(new Expression[]{new MutableVectorExpression()}, 0, ch.njol.util.Kleenean.FALSE, yawResult));
        yaw.change(SkriptEvent.EMPTY, new Object[]{180.0D}, ChangeMode.SET);
        assertEquals(180.0F, Vec3ExpressionSupport.skriptYaw(MutableVectorExpression.current), 0.0001F);
    }

    private static void ensureSyntax() throws Exception {
        if (syntaxRegistered) {
            return;
        }
        Class.forName(ExprLocationFromVector.class.getName());
        Class.forName(ExprLocationVectorOffset.class.getName());
        Class.forName(ExprMidpoint.class.getName());
        Class.forName(ExprVectorBetweenLocations.class.getName());
        Class.forName(ExprVectorCrossProduct.class.getName());
        Class.forName(ExprVectorDotProduct.class.getName());
        Class.forName(ExprVectorLength.class.getName());
        Class.forName(ExprVectorNormalize.class.getName());
        Class.forName(ExprXYZComponent.class.getName());
        Class.forName(ExprYawPitch.class.getName());

        Skript.registerExpression(TestVectorExpression.class, Vec3.class, "lane-a-unit-vector");
        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-a-unit-location");
        Skript.registerExpression(TestSecondLocationExpression.class, FabricLocation.class, "lane-a-second-location");
        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-a-unit-world");
        Skript.registerExpression(MutableVectorExpression.class, Vec3.class, "lane-a-mutable-vector");
        syntaxRegistered = true;
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static ParseResult parseResult(String expr, String... tags) {
        ParseResult result = new ParseResult();
        result.expr = expr;
        for (String tag : tags) {
            result.tags.add(tag);
        }
        return result;
    }

    private static void assertVec3(@Nullable Vec3 vector, double x, double y, double z) {
        assertNotNull(vector);
        assertEquals(x, vector.x, 0.00001D);
        assertEquals(y, vector.y, 0.00001D);
        assertEquals(z, vector.z, 0.00001D);
    }

    public static final class TestVectorExpression extends SimpleExpression<Vec3> {
        @Override
        protected Vec3 @Nullable [] get(SkriptEvent event) {
            return new Vec3[]{UNIT_VECTOR};
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

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{UNIT_LOCATION};
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

    public static final class TestSecondLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{SECOND_LOCATION};
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

    public static final class MutableVectorExpression extends SimpleExpression<Vec3> {
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

    private static final class FixedQuaternionExpression extends SimpleExpression<Quaternionf> {
        @Override
        protected Quaternionf @Nullable [] get(SkriptEvent event) {
            return new Quaternionf[]{new Quaternionf(1.0F, 2.0F, 3.0F, 4.0F)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Quaternionf> getReturnType() {
            return Quaternionf.class;
        }
    }
}
