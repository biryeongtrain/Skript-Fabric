package ch.njol.skript.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
public final class EffectEntityClosureCompatibilityTest {

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
            registerClassInfo(Entity.class, "entity");
            registerClassInfo(LivingEntity.class, "livingentity");
            registerClassInfo(Number.class, "number");
            registerClassInfo(Vec3.class, "vector");
            registerClassInfo(FabricLocation.class, "location");
            registerClassInfo(FabricItemType.class, "itemtype");
            registerClassInfo(ItemEntity.class, "itementity");
            registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestVectorExpression.class, Vec3.class, "lane-f-test-vector");
            Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-f-test-location");
            Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-f-test-itemtype");
            Skript.registerExpression(TestItemEntityExpression.class, ItemEntity.class, "lane-f-test-itementity");
            Skript.registerExpression(TestTimespanExpression.class, ch.njol.skript.util.Timespan.class, "lane-f-test-timespan");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        EffAllayCanDuplicate.register();
        EffAllayDuplicate.register();
        EffCharge.register();
        EffDancing.register();
        EffDropLeash.register();
        EffExplodeCreeper.register();
        EffFireResistant.register();
        EffGoatHorns.register();
        EffGoatRam.register();
        EffIncendiary.register();
        EffItemDespawn.register();
        EffKnockback.register();
        EffPush.register();
        EffMakeEggHatch.register();
        EffSwingHand.register();
        EffLightning.register();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void allayAndChargeEffectsBindExpectedExpressions() throws Exception {
        EffAllayCanDuplicate allow = parseEffect("allow lane-f-test-livingentity to duplicate", EffAllayCanDuplicate.class);
        EffAllayDuplicate duplicate = parseEffect("make lane-f-test-livingentity duplicate", EffAllayDuplicate.class);
        EffCharge charge = parseEffect("charge lane-f-test-entity", EffCharge.class);

        assertTrue(readBoolean(allow, "duplicate"));
        assertEquals("lane-f-test-livingentity", expression(duplicate, "entities").toString(null, false));
        assertTrue(readBoolean(charge, "charge"));
    }

    @Test
    void movementEffectsBindVectorLocationAndStrengthInputs() throws Exception {
        EffKnockback knockback = parseEffect("knockback lane-f-test-livingentity lane-f-test-vector with strength lane-f-test-number", EffKnockback.class);
        EffPush pushVector = parseEffect("push lane-f-test-entity along lane-f-test-vector at speed lane-f-test-number", EffPush.class);
        EffPush pushLocation = parseEffect("push lane-f-test-entity away from lane-f-test-location at speed lane-f-test-number", EffPush.class);
        EffLightning lightning = parseEffect("strike lightning effect lane-f-test-location", EffLightning.class);

        assertEquals("lane-f-test-vector", expression(knockback, "direction").toString(null, false));
        assertEquals("lane-f-test-number", expression(pushVector, "speed").toString(null, false));
        assertTrue(readBoolean(pushLocation, "awayFrom"));
        assertTrue(readBoolean(lightning, "effectOnly"));
    }

    @Test
    void entityControlEffectsTrackModeBits() throws Exception {
        EffDancing dance = parseEffect("make lane-f-test-livingentity dance", EffDancing.class);
        EffExplodeCreeper stop = parseEffect("stop ignition of lane-f-test-livingentity", EffExplodeCreeper.class);
        EffGoatHorns regrow = parseEffect("regrow the both horns of lane-f-test-livingentity", EffGoatHorns.class);
        EffGoatRam ram = parseEffect("make lane-f-test-livingentity ram lane-f-test-livingentity", EffGoatRam.class);
        EffSwingHand offHand = parseEffect("make lane-f-test-livingentity swing offhand", EffSwingHand.class);

        assertTrue(readBoolean(dance, "start"));
        assertTrue(readBoolean(stop, "stop"));
        assertFalse(readBoolean(regrow, "remove"));
        assertEquals("lane-f-test-livingentity", expression(ram, "target").toString(null, false));
        assertFalse(readBoolean(offHand, "isMainHand"));
    }

    @Test
    void fireResistantAndItemDespawnEffectsBindItemInputs() throws Exception {
        EffFireResistant resistant = parseEffect("make lane-f-test-itemtype fire resistant", EffFireResistant.class);
        EffItemDespawn prevent = parseEffect("prevent lane-f-test-itementity from naturally despawning", EffItemDespawn.class);

        assertFalse(readBoolean(resistant, "not"));
        assertTrue(readBoolean(prevent, "prevent"));
    }

    @Test
    void incendiaryEventVariantRequiresExplosionPrimeMarker() {
        EffIncendiary effect = new EffIncendiary();

        assertFalse(effect.init(new Expression<?>[0], 2, Kleenean.FALSE, new ParseResult()));

        ParserInstance.get().setCurrentEvent("explosion prime", FabricEffectEventHandles.ExplosionPrime.class);
        assertTrue(effect.init(new Expression<?>[0], 2, Kleenean.FALSE, new ParseResult()));
    }

    @Test
    void eggAndLeashEventEffectsRequireTheirMarkers() {
        EffMakeEggHatch egg = new EffMakeEggHatch();
        EffDropLeash leash = new EffDropLeash();

        assertFalse(egg.init(new Expression<?>[0], 0, Kleenean.FALSE, new ParseResult()));
        assertFalse(leash.init(new Expression<?>[0], 0, Kleenean.FALSE, new ParseResult()));

        ParserInstance.get().setCurrentEvent("egg throw", FabricEffectEventHandles.PlayerEggThrow.class);
        assertTrue(egg.init(new Expression<?>[0], 0, Kleenean.FALSE, new ParseResult()));

        ParserInstance.get().setCurrentEvent("unleash", FabricEffectEventHandles.EntityUnleash.class);
        assertTrue(leash.init(new Expression<?>[0], 0, Kleenean.FALSE, new ParseResult()));
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

    public static final class TestVectorExpression extends SimpleExpression<Vec3> {
        @Override
        protected Vec3 @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Vec3> getReturnType() {
            return Vec3.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-vector";
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

    public static final class TestItemTypeExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return new FabricItemType[]{new FabricItemType(Items.DIAMOND)};
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
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

    public static final class TestItemEntityExpression extends SimpleExpression<ItemEntity> {
        @Override
        protected ItemEntity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends ItemEntity> getReturnType() {
            return ItemEntity.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-itementity";
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
