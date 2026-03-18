package ch.njol.skript.effects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Executable;

@Tag("isolated-registry")
final class EffectWorldServerCompatibilityTest {

    private static boolean expressionsRegistered;
    private static List<SyntaxInfo<?>> originalEffects = List.of();

    @BeforeAll
    static void bootstrapSyntax() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        originalEffects = new ArrayList<>();
        for (SyntaxInfo<?> effectInfo : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT)) {
            originalEffects.add(effectInfo);
        }
        ensureSyntax();
    }

    @AfterAll
    static void restoreRuntimeSyntax() {
        ParserInstance.get().setCurrentScript(null);
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        for (SyntaxInfo<?> effectInfo : originalEffects) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EFFECT, effectInfo);
        }
    }

    private static void ensureSyntax() {
        if (!expressionsRegistered) {
            registerClassInfo(Entity.class, "entity");
            registerClassInfo(LivingEntity.class, "livingentity");
            registerClassInfo(ServerPlayer.class, "player");
            registerClassInfo(Number.class, "number");
            registerClassInfo(String.class, "string");
            registerClassInfo(Object.class, "object");
            registerClassInfo(FabricLocation.class, "location");
            registerClassInfo(WorldBorder.class, "worldborder");
            registerClassInfo(Script.class, "script");
            registerClassInfo(Executable.class, "executable");
            registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestStringExpression.class, String.class, "lane-f-test-string");
            Skript.registerExpression(TestObjectExpression.class, Object.class, "lane-f-test-object");
            Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-f-test-location");
            Skript.registerExpression(TestWorldBorderExpression.class, WorldBorder.class, "lane-f-test-worldborder");
            Skript.registerExpression(TestScriptExpression.class, Script.class, "lane-f-test-script");
            Skript.registerExpression(TestExecutableExpression.class, Executable.class, "lane-f-test-executable");
            Skript.registerExpression(TestTimespanExpression.class, ch.njol.skript.util.Timespan.class, "lane-f-test-timespan");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        EffDetonate.register();
        EffWorldBorderExpand.register();
        EffLog.register();
        EffRun.register();
        EffSuppressWarnings.register();
        EffSuppressTypeHints.register();
        EffMakeSay.register();
        EffConnect.register();
        EffScriptFile.register();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void detonateEffectBindsEntityExpression() throws Exception {
        EffDetonate effect = parseEffect("detonate lane-f-test-entity", EffDetonate.class);

        assertEquals("lane-f-test-entity", expression(effect, "entities").toString(null, false));
    }

    @Test
    void worldBorderExpandTracksRadiusShrinkAndTimespan() throws Exception {
        EffWorldBorderExpand effect = parseEffect(
                "shrink lane-f-test-worldborder's radius to lane-f-test-number over lane-f-test-timespan",
                EffWorldBorderExpand.class
        );

        assertTrue(readBoolean(effect, "shrink"));
        assertTrue(readBoolean(effect, "radius"));
        assertTrue(readBoolean(effect, "to"));
        assertEquals("lane-f-test-timespan", expression(effect, "timespan").toString(null, false));
    }

    @Test
    void logEffectTracksOptionalFileAndSeverity() throws Exception {
        EffLog effect = parseEffect(
                "log lane-f-test-string to file lane-f-test-string with a severity of severe",
                EffLog.class
        );

        assertEquals("lane-f-test-string", expression(effect, "messages").toString(null, false));
        assertEquals("lane-f-test-string", expression(effect, "files").toString(null, false));
        assertEquals("SEVERE", readField(effect, "logLevel").toString());
    }

    @Test
    void runEffectTracksArgumentsTag() throws Exception {
        EffRun withArguments = parseEffect("run lane-f-test-executable with arguments lane-f-test-object", EffRun.class);
        EffRun withoutArguments = parseEffect("execute lane-f-test-executable", EffRun.class);

        assertTrue(readBoolean(withArguments, "hasArguments"));
        assertFalse(readBoolean(withoutArguments, "hasArguments"));
        assertEquals("lane-f-test-object", expression(withArguments, "arguments").toString(null, false));
    }

    @Test
    void suppressWarningsAddsWarningToCurrentScript() {
        Script script = new Script(null, List.of());
        ParserInstance.get().setCurrentScript(script);
        EffSuppressWarnings effect = new EffSuppressWarnings();

        ParseResult parseResult = new ParseResult();
        parseResult.mark = ScriptWarning.MISSING_CONJUNCTION.ordinal();

        assertTrue(effect.init(new Expression<?>[0], 0, Kleenean.FALSE, parseResult));
        assertTrue(script.suppressesWarning(ScriptWarning.MISSING_CONJUNCTION));
    }

    @Test
    void suppressTypeHintsTogglesHintManager() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, List.of()));
        parser.getHintManager().setActive(true);
        EffSuppressTypeHints start = new EffSuppressTypeHints();
        EffSuppressTypeHints stop = new EffSuppressTypeHints();

        assertTrue(start.init(new Expression<?>[0], 0, Kleenean.FALSE, new ParseResult()));
        assertFalse(parser.getHintManager().isActive());

        ParseResult stopResult = new ParseResult();
        stopResult.tags.add("stop");
        assertTrue(stop.init(new Expression<?>[0], 1, Kleenean.FALSE, stopResult));
        assertTrue(parser.getHintManager().isActive());
    }

    @Test
    void unsupportedMakeSayConnectAndScriptFileEffectsFailInit() {
        assertFalse(new EffMakeSay().init(
                new Expression[]{new TestPlayerExpression(), new TestStringExpression()},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertTrue(new EffConnect().init(
                new Expression[]{new TestPlayerExpression(), new TestStringExpression(), new TestNumberExpression()},
                2,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertFalse(new EffScriptFile().init(
                new Expression[]{new TestScriptExpression()},
                2,
                Kleenean.FALSE,
                parseResultWithMark(2)
        ));
    }

    private <T extends Effect> T parseEffect(String input, Class<T> effectClass) {
        Statement statement = Statement.parse(input, "failed");
        assertNotNull(statement);
        assertInstanceOf(effectClass, statement);
        return effectClass.cast(statement);
    }

    private ParseResult parseResultWithMark(int mark) {
        ParseResult parseResult = new ParseResult();
        parseResult.mark = mark;
        return parseResult;
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Object readField(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(owner);
    }

    private Field findField(Class<?> owner, String fieldName) throws NoSuchFieldException {
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

    public static final class TestEntityExpression extends SimpleExpression<Entity> {

        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Entity> getReturnType() {
            return Entity.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-entity";
        }
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {

        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-livingentity";
        }
    }

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {

        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-player";
        }
    }

    public static final class TestNumberExpression extends SimpleExpression<Number> {

        @Override
        protected Number @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Number> getReturnType() {
            return Number.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-number";
        }
    }

    public static final class TestStringExpression extends SimpleExpression<String> {

        @Override
        protected String @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-string";
        }
    }

    public static final class TestObjectExpression extends SimpleExpression<Object> {

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-object";
        }
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {

        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends FabricLocation> getReturnType() {
            return FabricLocation.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-location";
        }
    }

    public static final class TestWorldBorderExpression extends SimpleExpression<WorldBorder> {

        @Override
        protected WorldBorder @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends WorldBorder> getReturnType() {
            return WorldBorder.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-worldborder";
        }
    }

    public static final class TestScriptExpression extends SimpleExpression<Script> {

        @Override
        protected Script @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Script> getReturnType() {
            return Script.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-script";
        }
    }

    public static final class TestExecutableExpression extends SimpleExpression<Executable> {

        @Override
        protected Executable @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Executable> getReturnType() {
            return Executable.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-executable";
        }
    }

    public static final class TestTimespanExpression extends SimpleExpression<ch.njol.skript.util.Timespan> {

        @Override
        protected ch.njol.skript.util.Timespan @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends ch.njol.skript.util.Timespan> getReturnType() {
            return ch.njol.skript.util.Timespan.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-timespan";
        }
    }
}
