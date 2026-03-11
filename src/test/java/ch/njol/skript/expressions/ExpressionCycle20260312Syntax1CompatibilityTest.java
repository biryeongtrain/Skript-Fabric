package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import ch.njol.skript.util.GameruleValue;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.njol.skript.events.FabricEventCompatHandles;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

class ExpressionCycle20260312Syntax1CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void parserBindsLandedSyntaxSubset() {
        assertInstanceOf(ExprGameRule.class, parseExpression("gamerule keepInventory of lane-c12-s1-world", GameruleValue.class));
        assertInstanceOf(ExprWeather.class, parseExpression("weather in lane-c12-s1-world", ExprWeather.WeatherKind.class));
        assertInstanceOf(ExprWorldBorderWarningTime.class, parseExpression("world border warning time of lane-c12-s1-worldborder", Timespan.class));
    }

    @Test
    void gameRuleExpressionReadsAndWritesBooleanAndIntegerRules() throws Exception {
        GameRules rules = new GameRules(FeatureFlags.DEFAULT_FLAGS);
        ServerLevel level = allocateLevel(rules).level();

        ExprGameRule keepInventory = new ExprGameRule();
        assertTrue(keepInventory.init(
                new Expression[]{
                        new SimpleLiteral<>(GameRules.RULE_KEEPINVENTORY, false),
                        new SimpleLiteral<>(level, false)
                },
                0,
                Kleenean.FALSE,
                parseResult("gamerule keepInventory of lane-c12-s1-world")
        ));
        assertEquals(new GameruleValue<>(rules.getBoolean(GameRules.RULE_KEEPINVENTORY)), keepInventory.getSingle(SkriptEvent.EMPTY));
        keepInventory.change(SkriptEvent.EMPTY, new Object[]{Boolean.TRUE}, ChangeMode.SET);
        assertEquals(Boolean.TRUE, keepInventory.getSingle(SkriptEvent.EMPTY).getGameruleValue());
        assertEquals(true, rules.getBoolean(GameRules.RULE_KEEPINVENTORY));

        ExprGameRule randomTickSpeed = new ExprGameRule();
        assertTrue(randomTickSpeed.init(
                new Expression[]{
                        new SimpleLiteral<>(GameRules.RULE_RANDOMTICKING, false),
                        new SimpleLiteral<>(level, false)
                },
                0,
                Kleenean.FALSE,
                parseResult("gamerule randomTickSpeed of lane-c12-s1-world")
        ));
        assertArrayEquals(new Class[]{Boolean.class, Integer.class}, randomTickSpeed.acceptChange(ChangeMode.SET));
        randomTickSpeed.change(SkriptEvent.EMPTY, new Object[]{9}, ChangeMode.SET);
        assertEquals(new GameruleValue<>(9), randomTickSpeed.getSingle(SkriptEvent.EMPTY));
        assertEquals(9, rules.getInt(GameRules.RULE_RANDOMTICKING));
    }

    @Test
    void worldBorderWarningTimeMutatesAndClamps() {
        WorldBorder border = new WorldBorder();
        ExprWorldBorderWarningTime expression = new ExprWorldBorderWarningTime();
        assertTrue(expression.init(
                new Expression[]{new SimpleLiteral<>(border, false)},
                0,
                Kleenean.FALSE,
                parseResult("world border warning time")
        ));

        expression.change(SkriptEvent.EMPTY, new Object[]{new Timespan(Timespan.TimePeriod.SECOND, 5L)}, ChangeMode.SET);
        assertEquals(5, border.getWarningTime());
        assertEquals(5L, expression.getSingle(SkriptEvent.EMPTY).getAs(Timespan.TimePeriod.SECOND));

        expression.change(SkriptEvent.EMPTY, new Object[]{new Timespan(Timespan.TimePeriod.SECOND, 3L)}, ChangeMode.ADD);
        assertEquals(8, border.getWarningTime());

        expression.change(SkriptEvent.EMPTY, new Object[]{new Timespan(Timespan.TimePeriod.SECOND, 99L)}, ChangeMode.REMOVE);
        assertEquals(0, border.getWarningTime());

        expression.change(SkriptEvent.EMPTY, null, ChangeMode.RESET);
        assertEquals(15, border.getWarningTime());
    }

    @Test
    void weatherExpressionReadsWritesAndTracksEventTargetWeather() throws Exception {
        LevelFixture fixture = allocateLevel(new GameRules(FeatureFlags.DEFAULT_FLAGS));
        ServerLevel level = fixture.level();

        ExprWeather expression = new ExprWeather();
        assertTrue(expression.init(
                new Expression[]{new SimpleLiteral<>(level, false)},
                0,
                Kleenean.FALSE,
                parseResult("weather in lane-c12-s1-world")
        ));
        assertArrayEquals(new Class[]{ExprWeather.WeatherKind.class}, expression.acceptChange(ChangeMode.SET));
        assertEquals(ExprWeather.WeatherKind.CLEAR, expression.getSingle(SkriptEvent.EMPTY));

        expression.change(SkriptEvent.EMPTY, new Object[]{ExprWeather.WeatherKind.RAIN}, ChangeMode.SET);
        assertEquals(ExprWeather.WeatherKind.RAIN, expression.getSingle(SkriptEvent.EMPTY));
        assertEquals(true, level.isRaining());
        assertEquals(false, level.isThundering());
        assertEquals(true, fixture.levelData().raining);
        assertEquals(false, fixture.levelData().thundering);

        expression.change(SkriptEvent.EMPTY, new Object[]{ExprWeather.WeatherKind.THUNDER}, ChangeMode.SET);
        assertEquals(ExprWeather.WeatherKind.THUNDER, expression.getSingle(SkriptEvent.EMPTY));
        assertEquals(true, level.isRaining());
        assertEquals(true, level.isThundering());
        assertEquals(true, fixture.levelData().raining);
        assertEquals(true, fixture.levelData().thundering);

        SkriptEvent event = new SkriptEvent(new FabricEventCompatHandles.WeatherChange(true, false), null, level, null);
        assertEquals(ExprWeather.WeatherKind.RAIN, expression.getSingle(event));

        expression.change(SkriptEvent.EMPTY, null, ChangeMode.RESET);
        assertEquals(ExprWeather.WeatherKind.CLEAR, expression.getSingle(SkriptEvent.EMPTY));
        assertEquals(false, level.isRaining());
        assertEquals(false, level.isThundering());
        assertEquals(false, fixture.levelData().raining);
        assertEquals(false, fixture.levelData().thundering);
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(WorldBorder.class, "worldborder");
        registerClassInfo(Timespan.class, "timespan");

        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-c12-s1-world");
        Skript.registerExpression(TestWorldBorderExpression.class, WorldBorder.class, "lane-c12-s1-worldborder");

        new ExprGameRule();
        new ExprWeather();
        new ExprWorldBorderWarningTime();
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
    private static LevelFixture allocateLevel(GameRules rules) throws Exception {
        ServerLevel level = (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
        MutableLevelData levelDataHandler = new MutableLevelData(rules);
        Object levelData = Proxy.newProxyInstance(
                ExpressionCycle20260312Syntax1CompatibilityTest.class.getClassLoader(),
                new Class[]{WritableLevelData.class, ServerLevelData.class},
                levelDataHandler
        );
        levelDataField().set(level, levelData);
        serverLevelDataField().set(level, levelData);
        dimensionTypeField().set(level, Holder.direct(overworldDimensionType()));
        return new LevelFixture(level, levelDataHandler);
    }

    private static Field levelDataField() {
        for (Field field : Level.class.getDeclaredFields()) {
            if (field.getType() == WritableLevelData.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Could not find level data field");
    }

    private static Field serverLevelDataField() {
        for (Field field : ServerLevel.class.getDeclaredFields()) {
            if (field.getType() == ServerLevelData.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Could not find server level data field");
    }

    private static Field dimensionTypeField() {
        for (Field field : Level.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Holder.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Could not find dimension type field");
    }

    private static DimensionType overworldDimensionType() {
        return new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                1.0D,
                true,
                false,
                -64,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                Optional.empty(),
                new DimensionType.MonsterSettings(false, true, ConstantInt.of(0), 0)
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T defaultValue(Class<T> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return (T) Boolean.FALSE;
        }
        if (type == byte.class) {
            return (T) Byte.valueOf((byte) 0);
        }
        if (type == short.class) {
            return (T) Short.valueOf((short) 0);
        }
        if (type == int.class) {
            return (T) Integer.valueOf(0);
        }
        if (type == long.class) {
            return (T) Long.valueOf(0L);
        }
        if (type == float.class) {
            return (T) Float.valueOf(0.0F);
        }
        if (type == double.class) {
            return (T) Double.valueOf(0.0D);
        }
        if (type == char.class) {
            return (T) Character.valueOf('\0');
        }
        return null;
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private record LevelFixture(ServerLevel level, MutableLevelData levelData) {
    }

    private static final class MutableLevelData implements java.lang.reflect.InvocationHandler {

        private final GameRules rules;
        private int clearWeatherTime;
        private int rainTime;
        private int thunderTime;
        private boolean raining;
        private boolean thundering;

        private MutableLevelData(GameRules rules) {
            this.rules = rules;
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            String name = method.getName();
            if (name.equals("getGameRules")) {
                return rules;
            }
            if (name.equals("setClearWeatherTime")) {
                clearWeatherTime = (Integer) args[0];
                return null;
            }
            if (name.equals("setRainTime")) {
                rainTime = (Integer) args[0];
                return null;
            }
            if (name.equals("setThunderTime")) {
                thunderTime = (Integer) args[0];
                return null;
            }
            if (name.equals("setRaining")) {
                raining = (Boolean) args[0];
                return null;
            }
            if (name.equals("setThundering")) {
                thundering = (Boolean) args[0];
                return null;
            }
            if (name.equals("getClearWeatherTime")) {
                return clearWeatherTime;
            }
            if (name.equals("getRainTime")) {
                return rainTime;
            }
            if (name.equals("getThunderTime")) {
                return thunderTime;
            }
            if (name.equals("isRaining")) {
                return raining;
            }
            if (name.equals("isThundering")) {
                return thundering;
            }
            if (name.equals("getGameTime") || name.equals("getDayTime")) {
                return 0L;
            }
            if (name.equals("equals")) {
                return proxy == args[0];
            }
            if (name.equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            if (name.equals("toString")) {
                return "lane-c12-s1-level-data";
            }
            return defaultValue(method.getReturnType());
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
}
