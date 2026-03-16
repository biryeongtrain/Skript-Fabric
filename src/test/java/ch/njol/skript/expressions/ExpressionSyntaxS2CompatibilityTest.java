package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ExpressionSyntaxS2CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsWorldBorderBundleSyntax() {
        assertInstanceOf(ExprWorlds.class, parseExpression("all worlds", ServerLevel.class));
        assertInstanceOf(ExprWorldFromName.class, parseExpression("world named lane-s2-string", ServerLevel.class));
        assertInstanceOf(ExprWorldBorder.class, parseExpression("world border of lane-s2-world", WorldBorder.class));
        assertInstanceOf(ExprWorldBorder.class, parseExpression("world border of lane-s2-player", WorldBorder.class));
        assertInstanceOf(ExprWorldBorderCenter.class, parseExpression("world border center of lane-s2-worldborder", FabricLocation.class));
        assertInstanceOf(ExprWorldBorderSize.class, parseExpression("world border radius of lane-s2-worldborder", Double.class));
        assertInstanceOf(ExprWorldBorderDamageAmount.class, parseExpression("world border damage amount of lane-s2-worldborder", Double.class));
        assertInstanceOf(ExprWorldBorderDamageBuffer.class, parseExpression("world border damage buffer of lane-s2-worldborder", Double.class));
        assertInstanceOf(ExprWorldBorderWarningDistance.class, parseExpression("world border warning distance of lane-s2-worldborder", Integer.class));
    }

    @Test
    void worldsAndWorldFromNameFailClosedWithoutServerContext() {
        assertArrayEquals(new ServerLevel[0], new ExprWorlds().getArray(SkriptEvent.EMPTY));

        ExprWorldFromName expression = new ExprWorldFromName();
        assertTrue(expression.init(
                new Expression[]{new SimpleLiteral<>("overworld", false)},
                0,
                Kleenean.FALSE,
                parseResult("world named overworld")
        ));
        assertNull(expression.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void worldBorderExpressionCopiesAndResets() {
        WorldBorder source = new WorldBorder();
        source.setCenter(10.0D, -4.0D);
        source.setSize(32.0D);
        source.setDamagePerBlock(1.5D);
        source.setDamageSafeZone(2.0D);
        source.setWarningBlocks(8);
        source.setWarningTime(20);

        WorldBorder target = new WorldBorder();
        ExprWorldBorder expression = new ExprWorldBorder();
        assertTrue(expression.init(new Expression[]{new SimpleLiteral<>(target, false)}, 0, Kleenean.FALSE, parseResult("world border")));

        expression.change(SkriptEvent.EMPTY, new Object[]{source}, ChangeMode.SET);
        assertEquals(10.0D, target.getCenterX());
        assertEquals(-4.0D, target.getCenterZ());
        assertEquals(32.0D, target.getSize());
        assertEquals(1.5D, target.getDamagePerBlock());
        assertEquals(2.0D, target.getDamageSafeZone());
        assertEquals(8, target.getWarningBlocks());
        assertEquals(20, target.getWarningTime());

        expression.change(SkriptEvent.EMPTY, null, ChangeMode.RESET);
        assertEquals(0.0D, target.getCenterX());
        assertEquals(0.0D, target.getCenterZ());
        assertEquals(59_999_968D, target.getSize());
        assertEquals(0.2D, target.getDamagePerBlock());
        assertEquals(5.0D, target.getDamageSafeZone());
        assertEquals(5, target.getWarningBlocks());
        assertEquals(15, target.getWarningTime());
    }

    @Test
    void worldBorderCenterUsesLocationXZ() {
        WorldBorder border = new WorldBorder();
        ExprWorldBorderCenter expression = new ExprWorldBorderCenter();
        assertTrue(expression.init(new Expression[]{new SimpleLiteral<>(border, false)}, 0, Kleenean.FALSE, parseResult("world border center")));

        FabricLocation initial = expression.getSingle(SkriptEvent.EMPTY);
        assertNotNull(initial);
        assertEquals(0.0D, initial.position().x);
        assertEquals(0.0D, initial.position().z);

        expression.change(SkriptEvent.EMPTY, new Object[]{new FabricLocation(null, new Vec3(12.5D, 70.0D, -8.25D))}, ChangeMode.SET);
        assertEquals(12.5D, border.getCenterX());
        assertEquals(-8.25D, border.getCenterZ());

        expression.change(SkriptEvent.EMPTY, null, ChangeMode.RESET);
        assertEquals(0.0D, border.getCenterX());
        assertEquals(0.0D, border.getCenterZ());
    }

    @Test
    void worldBorderSizeTracksRadiusMutations() {
        WorldBorder border = new WorldBorder();
        ExprWorldBorderSize expression = new ExprWorldBorderSize();
        SkriptParser.ParseResult parseResult = parseResult("world border radius");
        parseResult.tags.add("radius");
        assertTrue(expression.init(new Expression[]{new SimpleLiteral<>(border, false)}, 0, Kleenean.FALSE, parseResult));

        expression.change(SkriptEvent.EMPTY, new Object[]{5}, ChangeMode.SET);
        assertEquals(10.0D, border.getSize());
        assertEquals(5.0D, expression.getSingle(SkriptEvent.EMPTY));

        expression.change(SkriptEvent.EMPTY, new Object[]{2}, ChangeMode.ADD);
        assertEquals(14.0D, border.getSize());

        expression.change(SkriptEvent.EMPTY, new Object[]{1}, ChangeMode.REMOVE);
        assertEquals(12.0D, border.getSize());
    }

    @Test
    void worldBorderDamageAndWarningMutationsClampAtZero() {
        WorldBorder border = new WorldBorder();

        ExprWorldBorderDamageAmount damageAmount = new ExprWorldBorderDamageAmount();
        assertTrue(damageAmount.init(new Expression[]{new SimpleLiteral<>(border, false)}, 0, Kleenean.FALSE, parseResult("world border damage amount")));
        damageAmount.change(SkriptEvent.EMPTY, new Object[]{1.25D}, ChangeMode.SET);
        assertEquals(1.25D, border.getDamagePerBlock());
        damageAmount.change(SkriptEvent.EMPTY, new Object[]{5.0D}, ChangeMode.REMOVE);
        assertEquals(0.0D, border.getDamagePerBlock());

        ExprWorldBorderDamageBuffer damageBuffer = new ExprWorldBorderDamageBuffer();
        assertTrue(damageBuffer.init(new Expression[]{new SimpleLiteral<>(border, false)}, 0, Kleenean.FALSE, parseResult("world border damage buffer")));
        damageBuffer.change(SkriptEvent.EMPTY, new Object[]{7.0D}, ChangeMode.SET);
        assertEquals(7.0D, border.getDamageSafeZone());
        damageBuffer.change(SkriptEvent.EMPTY, new Object[]{10.0D}, ChangeMode.REMOVE);
        assertEquals(0.0D, border.getDamageSafeZone());

        ExprWorldBorderWarningDistance warningDistance = new ExprWorldBorderWarningDistance();
        assertTrue(warningDistance.init(new Expression[]{new SimpleLiteral<>(border, false)}, 0, Kleenean.FALSE, parseResult("world border warning distance")));
        warningDistance.change(SkriptEvent.EMPTY, new Object[]{3}, ChangeMode.SET);
        assertEquals(3, border.getWarningBlocks());
        warningDistance.change(SkriptEvent.EMPTY, new Object[]{10}, ChangeMode.REMOVE);
        assertEquals(0, border.getWarningBlocks());
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(WorldBorder.class, "worldborder");
        registerClassInfo(FabricLocation.class, "location");
        registerClassInfo(Number.class, "number");
        registerClassInfo(String.class, "string");
        registerClassInfo(Timespan.class, "timespan");

        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-s2-world");
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-s2-player");
        Skript.registerExpression(TestWorldBorderExpression.class, WorldBorder.class, "lane-s2-worldborder");
        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-s2-location");
        Skript.registerExpression(TestStringExpression.class, String.class, "lane-s2-string");

        new ExprWorlds();
        new ExprWorldFromName();
        new ExprWorldBorder();
        new ExprWorldBorderCenter();
        new ExprWorldBorderSize();
        new ExprWorldBorderDamageAmount();
        new ExprWorldBorderDamageBuffer();
        new ExprWorldBorderWarningDistance();
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

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {
        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return new ServerPlayer[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }
    }

    public static final class TestWorldBorderExpression extends SimpleExpression<WorldBorder> {
        @Override
        protected WorldBorder @Nullable [] get(SkriptEvent event) {
            return new WorldBorder[]{new WorldBorder()};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends WorldBorder> getReturnType() {
            return WorldBorder.class;
        }
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
