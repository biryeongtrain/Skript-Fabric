package ch.njol.skript.effects;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class EffectMutationCompatibilityTest {

    private static boolean expressionsRegistered;
    private static List<SyntaxInfo<?>> originalEffects = List.of();

    @BeforeAll
    static void bootstrapSyntax() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        originalEffects = new ArrayList<>();
        for (SyntaxInfo<?> effectInfo : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT)) {
            originalEffects.add(effectInfo);
        }
        ensureSyntax();
    }

    @AfterAll
    static void restoreRuntimeSyntax() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        for (SyntaxInfo<?> effectInfo : originalEffects) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EFFECT, effectInfo);
        }
    }

    @AfterEach
    void clearCurrentEvent() {
        ParserInstance.get().deleteCurrentEvent();
    }

    private static void ensureSyntax() {
        if (!expressionsRegistered) {
            registerClassInfo(Number.class, "number");
            registerClassInfo(String.class, "string");
            registerClassInfo(FabricBlock.class, "block");
            registerClassInfo(FabricItemType.class, "itemtype");
            registerClassInfo(FabricInventory.class, "inventory");
            registerClassInfo(LivingEntity.class, "livingentity");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestStringExpression.class, String.class, "lane-f-test-string");
            Skript.registerExpression(MutableStringExpression.class, String.class, "lane-f-test-mutable-string");
            Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-f-test-block");
            Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-f-test-itemtype");
            Skript.registerExpression(TestInventoryExpression.class, FabricInventory.class, "lane-f-test-inventory");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        EffApplyBoneMeal.register();
        EffEntityUnload.register();
        EffForceEnchantmentGlint.register();
        EffKeepInventory.register();
        EffReplace.register();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void boneMealAndEntityUnloadEffectsBindExpectedInputs() throws Exception {
        EffApplyBoneMeal boneMeal = parseEffect("apply lane-f-test-number bone meal to lane-f-test-block", EffApplyBoneMeal.class);
        EffEntityUnload unload = parseEffect("prevent lane-f-test-livingentity from despawning", EffEntityUnload.class);

        assertEquals("lane-f-test-number", expression(boneMeal, "amount").toString(null, false));
        assertEquals("lane-f-test-block", expression(boneMeal, "blocks").toString(null, false));
        assertFalse(readBoolean(unload, "despawn"));
    }

    @Test
    void glintEffectTracksEnableDisableAndClearPatterns() throws Exception {
        EffForceEnchantmentGlint start = parseEffect("force lane-f-test-itemtype to glint", EffForceEnchantmentGlint.class);
        EffForceEnchantmentGlint stop = parseEffect("force lane-f-test-itemtype to stop glinting", EffForceEnchantmentGlint.class);
        EffForceEnchantmentGlint clear = parseEffect("clear the enchantment glint override of lane-f-test-itemtype", EffForceEnchantmentGlint.class);

        assertEquals(0, readInt(start, "pattern"));
        assertEquals(1, readInt(stop, "pattern"));
        assertEquals(2, readInt(clear, "pattern"));
    }

    @Test
    void replaceEffectTracksRegexFirstAndInventoryBranches() throws Exception {
        EffReplace first = parseEffect(
                "replace the first lane-f-test-string in lane-f-test-mutable-string with lane-f-test-string with case sensitivity",
                EffReplace.class
        );
        EffReplace regex = parseEffect(
                "regex replace lane-f-test-string in lane-f-test-mutable-string with lane-f-test-string",
                EffReplace.class
        );
        EffReplace inventory = parseEffect(
                "replace all lane-f-test-itemtype in lane-f-test-inventory with lane-f-test-itemtype",
                EffReplace.class
        );

        assertTrue(readBoolean(first, "replaceString"));
        assertTrue(readBoolean(first, "replaceFirst"));
        assertTrue(readBoolean(first, "caseSensitive"));
        assertTrue(readBoolean(regex, "replaceRegex"));
        assertFalse(readBoolean(inventory, "replaceString"));
    }

    @Test
    void keepInventoryRequiresDeathEventContext() {
        EffKeepInventory effect = new EffKeepInventory();
        ParseResult parseResult = new ParseResult();
        parseResult.mark = 1;

        assertFalse(effect.init(new Expression<?>[0], 0, Kleenean.FALSE, parseResult));

        ParserInstance.get().setCurrentEvent("death", FabricEffectEventHandles.EntityDeath.class);
        assertTrue(effect.init(new Expression<?>[0], 0, Kleenean.FALSE, parseResult));
    }

    private <T extends Effect> T parseEffect(String input, Class<T> effectClass) {
        Statement statement = Statement.parse(input, "failed");
        assertNotNull(statement);
        assertInstanceOf(effectClass, statement);
        return effectClass.cast(statement);
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

    private int readInt(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.getInt(owner);
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

    public static final class TestNumberExpression extends SimpleExpression<Number> {
        @Override
        protected Number @Nullable [] get(SkriptEvent event) {
            return new Number[]{1};
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
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-number";
        }
    }

    public static class TestStringExpression extends SimpleExpression<String> {
        @Override
        protected String @Nullable [] get(SkriptEvent event) {
            return new String[]{"value"};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-string";
        }
    }

    public static final class MutableStringExpression extends TestStringExpression {
        @Override
        public Class<?>[] acceptChange(ch.njol.skript.classes.Changer.ChangeMode mode) {
            return mode == ch.njol.skript.classes.Changer.ChangeMode.SET ? new Class[]{String.class} : null;
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ch.njol.skript.classes.Changer.ChangeMode mode) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-mutable-string";
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
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-block";
        }
    }

    public static final class TestItemTypeExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-itemtype";
        }
    }

    public static final class TestInventoryExpression extends SimpleExpression<FabricInventory> {
        @Override
        protected FabricInventory @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public Class<? extends FabricInventory> getReturnType() {
            return FabricInventory.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-inventory";
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
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-livingentity";
        }
    }
}
