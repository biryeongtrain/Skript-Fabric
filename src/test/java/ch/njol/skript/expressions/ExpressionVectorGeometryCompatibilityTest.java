package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionVectorGeometryCompatibilityTest {

    private static final FabricLocation UNIT_LOCATION = new FabricLocation(null, new Vec3(2.0D, 4.0D, 6.0D));
    private static List<SyntaxInfo<?>> originalExpressions = List.of();
    private static boolean syntaxRegistered;

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

    @Test
    void vectorGeometryExpressionsParseWithRegisteredSources() {
        assertInstanceOf(ExprVectorAngleBetween.class, parseExpression(
                "angle between vector(1, 0, 0) and vector(0, 1, 0)",
                Number.class
        ));
        assertInstanceOf(ExprVectorFromXYZ.class, parseExpression("vector 1, 2 and 3", Vec3.class));
        assertInstanceOf(ExprVectorOfLocation.class, parseExpression("vector of lane-c-unit-location", Vec3.class));
        assertInstanceOf(ExprVectorProjection.class, parseExpression(
                "vector projection of vector(2, 2, 0) onto vector(1, 0, 0)",
                Vec3.class
        ));
        assertInstanceOf(ExprVectorRandom.class, parseExpression("random vector", Vec3.class));
        assertInstanceOf(ExprVectorSquaredLength.class, parseExpression("squared length of vector(1, 2, 2)", Number.class));
    }

    @Test
    void vectorGeometryExpressionsProduceExpectedValues() {
        ExprVectorAngleBetween angle = assertInstanceOf(
                ExprVectorAngleBetween.class,
                parseExpression("angle between vector(1, 0, 0) and vector(0, 1, 0)", Number.class)
        );
        assertEquals(90.0D, angle.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.0001D);

        ExprVectorFromXYZ xyz = assertInstanceOf(
                ExprVectorFromXYZ.class,
                parseExpression("vector 1, 2 and 3", Vec3.class)
        );
        assertVec3(xyz.getSingle(SkriptEvent.EMPTY), 1.0D, 2.0D, 3.0D);

        ExprVectorOfLocation ofLocation = assertInstanceOf(
                ExprVectorOfLocation.class,
                parseExpression("vector of lane-c-unit-location", Vec3.class)
        );
        assertVec3(ofLocation.getSingle(SkriptEvent.EMPTY), 2.0D, 4.0D, 6.0D);

        ExprVectorProjection projection = assertInstanceOf(
                ExprVectorProjection.class,
                parseExpression("vector projection of vector(2, 2, 0) onto vector(1, 0, 0)", Vec3.class)
        );
        assertVec3(projection.getSingle(SkriptEvent.EMPTY), 2.0D, 0.0D, 0.0D);

        ExprVectorRandom random = assertInstanceOf(
                ExprVectorRandom.class,
                parseExpression("random vector", Vec3.class)
        );
        Vec3 randomVector = random.getSingle(SkriptEvent.EMPTY);
        assertNotNull(randomVector);
        assertEquals(1.0D, randomVector.length(), 0.00001D);

        ExprVectorSquaredLength squaredLength = assertInstanceOf(
                ExprVectorSquaredLength.class,
                parseExpression("squared length of vector(1, 2, 2)", Number.class)
        );
        assertEquals(9.0D, squaredLength.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.00001D);
    }

    private static void ensureSyntax() throws Exception {
        if (syntaxRegistered) {
            return;
        }
        // Re-register vector expressions with ExprVectorFromDirection last so that
        // more specific patterns (ExprVectorFromXYZ, ExprVectorOfLocation) match first.
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
        List<SyntaxInfo<?>> reordered = new ArrayList<>();
        List<SyntaxInfo<?>> directionEntries = new ArrayList<>();
        for (SyntaxInfo<?> info : originalExpressions) {
            if (info.type() == ExprVectorFromDirection.class) {
                directionEntries.add(info);
            } else {
                reordered.add(info);
            }
        }
        reordered.addAll(directionEntries);
        for (SyntaxInfo<?> info : reordered) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EXPRESSION, info);
        }
        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-c-unit-location");
        syntaxRegistered = true;
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static void assertVec3(@Nullable Vec3 vector, double x, double y, double z) {
        assertNotNull(vector);
        assertEquals(x, vector.x, 0.00001D);
        assertEquals(y, vector.y, 0.00001D);
        assertEquals(z, vector.z, 0.00001D);
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
}
