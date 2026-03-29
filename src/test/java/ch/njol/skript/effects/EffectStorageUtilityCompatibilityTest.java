package ch.njol.skript.effects;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
public final class EffectStorageUtilityCompatibilityTest {

    private static boolean expressionsRegistered;
    private static List<SyntaxInfo<?>> originalEffects = List.of();
    private static final MutableBooleanExpression MUTABLE_BOOLEAN = new MutableBooleanExpression();

    @BeforeAll
    public static void bootstrapSyntax() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        originalEffects = new ArrayList<>();
        for (SyntaxInfo<?> effectInfo : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT)) {
            originalEffects.add(effectInfo);
        }
        ensureSyntax();
    }

    @AfterAll
    public static void restoreRuntimeSyntax() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        for (SyntaxInfo<?> effectInfo : originalEffects) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EFFECT, effectInfo);
        }
        Variables.clearAll();
    }

    @AfterEach
    public void clearVariables() {
        Variables.clearAll();
        MUTABLE_BOOLEAN.value = false;
    }

    private static void ensureSyntax() {
        if (!expressionsRegistered) {
            registerClassInfo(Object.class, "object");
            registerClassInfo(String.class, "string");
            registerClassInfo(Boolean.class, "boolean");
            registerClassInfo(Number.class, "number");
            registerClassInfo(Entity.class, "entity");
            registerClassInfo(LivingEntity.class, "livingentity");
            registerClassInfo(ServerPlayer.class, "player");
            registerClassInfo(FabricBlock.class, "block");
            registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
            Skript.registerExpression(TestBooleanExpression.class, Boolean.class, "lane-f-test-boolean");
            Skript.registerExpression(TestSortKeyExpression.class, Number.class, "lane-f-test-sort-key of %objects%");
            Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-f-test-block");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            Skript.registerExpression(TestTimespanExpression.class, ch.njol.skript.util.Timespan.class, "lane-f-test-timespan");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        EffCopy.register();
        EffSort.register();
        EffToggle.register();
        EffExceptionDebug.register();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    public void copyEffectCopiesNestedListBranchesIndependently() {
        Variables.setVariable("source::plain", "emerald_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("source::nested::value", "gold_block", SkriptEvent.EMPTY, false);

        EffCopy effect = parseEffect("copy {source::*} to {target::*}", EffCopy.class);
        runEffect(effect);

        Object copiedPlain = Variables.getVariable("target::plain", SkriptEvent.EMPTY, false);
        Object copiedNested = Variables.getVariable("target::nested::value", SkriptEvent.EMPTY, false);
        assertEquals("emerald_block", copiedPlain);
        assertEquals("gold_block", copiedNested);

        Object rawSource = Variables.getVariable("source::*", SkriptEvent.EMPTY, false);
        Object rawTarget = Variables.getVariable("target::*", SkriptEvent.EMPTY, false);
        assertNotNull(rawSource);
        assertNotNull(rawTarget);
        assertNotSame(rawSource, rawTarget);
    }

    @Test
    public void sortEffectParsesMappingExpressionAndReordersListVariable() throws Exception {
        Variables.setVariable("values::1", "bbb", SkriptEvent.EMPTY, false);
        Variables.setVariable("values::2", "a", SkriptEvent.EMPTY, false);
        Variables.setVariable("values::3", "cc", SkriptEvent.EMPTY, false);

        EffSort effect = parseEffect("sort {values::*} by lane-f-test-sort-key of input", EffSort.class);
        assertNotNull(readField(effect, "mappingExpr"));

        runEffect(effect);

        assertArrayEquals(
                new Object[]{"a", "cc", "bbb"},
                new Object[]{
                        Variables.getVariable("values::1", SkriptEvent.EMPTY, false),
                        Variables.getVariable("values::2", SkriptEvent.EMPTY, false),
                        Variables.getVariable("values::3", SkriptEvent.EMPTY, false)
                }
        );
    }

    @Test
    public void toggleEffectMutatesBooleanExpressionsInPlace() {
        EffToggle effect = parseEffect("toggle lane-f-test-boolean", EffToggle.class);

        assertFalse(MUTABLE_BOOLEAN.value);
        runEffect(effect);
        assertTrue(MUTABLE_BOOLEAN.value);
    }

    @Test
    public void exceptionDebugEffectThrowsIntentionalFailure() {
        EffExceptionDebug effect = new EffExceptionDebug();
        assertTrue(effect.init(new Expression<?>[0], 0, Kleenean.FALSE, new ParseResult()));
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> runEffect(effect));
        assertEquals("Created by a script (debugging)...", thrown.getMessage());
    }

    @Test
    public void blockedEntityStorageAndVisibilityEffectsFailInit() {
        assertFalse(new EffClearEntityStorage().init(
                new Expression[]{new TestBlockExpression()},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertFalse(new EffInsertEntityStorage().init(
                new Expression[]{new TestLivingEntityExpression(), new TestBlockExpression()},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertFalse(new EffReleaseEntityStorage().init(
                new Expression[]{new TestBlockExpression(), new TestTimespanExpression()},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertFalse(new EffEntityVisibility().init(
                new Expression[]{new TestEntityExpression(), new TestPlayerExpression()},
                1,
                Kleenean.FALSE,
                new ParseResult()
        ));
    }

    @Test
    public void blockedEntityStorageEffectsStillReportStableSyntaxStrings() {
        EffClearEntityStorage clear = new EffClearEntityStorage();
        EffInsertEntityStorage insert = new EffInsertEntityStorage();
        EffReleaseEntityStorage release = new EffReleaseEntityStorage();

        inject(clear, "blocks", new TestBlockExpression());
        inject(insert, "entities", new TestLivingEntityExpression());
        inject(insert, "block", new TestBlockExpression());
        inject(release, "blocks", new TestBlockExpression());
        inject(release, "timespan", new TestTimespanExpression());

        assertEquals("clear the stored entities of lane-f-test-block", clear.toString(SkriptEvent.EMPTY, false));
        assertEquals(
                "add lane-f-test-livingentity into the entity storage of lane-f-test-block",
                insert.toString(SkriptEvent.EMPTY, false)
        );
        assertEquals(
                "release the stored entities of lane-f-test-block for lane-f-test-timespan",
                release.toString(SkriptEvent.EMPTY, false)
        );
    }

    private <T extends Effect> T parseEffect(String input, Class<T> effectClass) {
        Statement statement = Statement.parse(input, "failed");
        assertNotNull(statement);
        assertInstanceOf(effectClass, statement);
        return effectClass.cast(statement);
    }

    private static Object readField(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(owner);
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

    private static void inject(Object owner, String fieldName, Object value) {
        try {
            Field field = findField(owner.getClass(), fieldName);
            field.setAccessible(true);
            field.set(owner, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void runEffect(Effect effect) {
        try {
            var method = Effect.class.getDeclaredMethod("run", SkriptEvent.class);
            method.setAccessible(true);
            method.invoke(effect, SkriptEvent.EMPTY);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(e);
        }
    }

    public static final class TestBooleanExpression extends SimpleExpression<Boolean> {
        @Override
        protected Boolean @Nullable [] get(SkriptEvent event) {
            return new Boolean[]{MUTABLE_BOOLEAN.value};
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{Boolean.class} : null;
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
            MUTABLE_BOOLEAN.value = delta != null && delta.length > 0 && Boolean.TRUE.equals(delta[0]);
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Boolean> getReturnType() {
            return Boolean.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-boolean";
        }
    }

    public static final class TestSortKeyExpression extends SimpleExpression<Number> {
        private Expression<?> values;

        @Override
        protected Number @Nullable [] get(SkriptEvent event) {
            Object value = values.getSingle(event);
            return value == null ? null : new Number[]{value.toString().length()};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Number> getReturnType() {
            return Number.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            values = expressions[0];
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-sort-key of " + values.toString(event, debug);
        }
    }

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
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
            return "lane-f-test-block";
        }
    }

    public static final class TestEntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public boolean isSingle() {
            return true;
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
            return true;
        }

        @Override
        public boolean isSingle() {
            return true;
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
            return true;
        }

        @Override
        public boolean isSingle() {
            return true;
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

    public static final class TestTimespanExpression extends SimpleExpression<ch.njol.skript.util.Timespan> {
        @Override
        protected ch.njol.skript.util.Timespan @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public boolean isSingle() {
            return true;
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

    private static final class MutableBooleanExpression {
        private boolean value;
    }
}
