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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
public final class EffectRuntimeClosureCompatibilityTest {

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

    private static void ensureSyntax() {
        if (!expressionsRegistered) {
            registerClassInfo(Entity.class, "entity");
            registerClassInfo(LivingEntity.class, "livingentity");
            registerClassInfo(ServerPlayer.class, "player");
            registerClassInfo(Number.class, "number");
            registerClassInfo(FabricBlock.class, "block");
            registerClassInfo(FabricLocation.class, "location");
            registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-f-test-block");
            Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-f-test-location");
            Skript.registerExpression(TestTimespanExpression.class, ch.njol.skript.util.Timespan.class, "lane-f-test-timespan");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        EffEndermanTeleport.register();
        EffForceAttack.register();
        EffPathfind.register();
        EffPersistent.register();
        EffToggleFlight.register();
        EffTransform.register();
        EffVehicle.register();
        EffZombify.register();
        EffCommandBlockConditional.register();
        EffGlowingText.register();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void endermanTeleportEffectBindsOptionalTarget() throws Exception {
        EffEndermanTeleport random = parseEffect("make lane-f-test-livingentity teleport randomly", EffEndermanTeleport.class);
        EffEndermanTeleport targeted = parseEffect("force lane-f-test-livingentity to teleport towards lane-f-test-entity", EffEndermanTeleport.class);

        assertEquals("lane-f-test-livingentity", expression(random, "entities").toString(null, false));
        assertEquals("lane-f-test-entity", expression(targeted, "target").toString(null, false));
    }

    @Test
    void forceAttackEffectBindsVictimsAndDamageAmount() throws Exception {
        EffForceAttack attack = parseEffect("make lane-f-test-livingentity attack lane-f-test-entity", EffForceAttack.class);
        EffForceAttack damage = parseEffect("force lane-f-test-livingentity to damage lane-f-test-entity by lane-f-test-number hearts", EffForceAttack.class);

        assertEquals("lane-f-test-entity", expression(attack, "victims").toString(null, false));
        assertEquals("lane-f-test-number", expression(damage, "amount").toString(null, false));
    }

    @Test
    void pathfindEffectBindsTargetAndStopPattern() throws Exception {
        EffPathfind move = parseEffect("make lane-f-test-livingentity pathfind towards lane-f-test-location at speed lane-f-test-number", EffPathfind.class);
        EffPathfind stop = parseEffect("make lane-f-test-livingentity stop pathfinding", EffPathfind.class);

        assertEquals("lane-f-test-location", expression(move, "target").toString(null, false));
        assertEquals("lane-f-test-number", expression(move, "speed").toString(null, false));
        assertEquals(null, readField(stop, "target"));
    }

    @Test
    void persistentEffectTracksNegatedMode() throws Exception {
        EffPersistent persist = parseEffect("make lane-f-test-block persistent", EffPersistent.class);
        EffPersistent prevent = parseEffect("prevent lane-f-test-block from persisting", EffPersistent.class);

        assertTrue(readBoolean(persist, "persist"));
        assertFalse(readBoolean(prevent, "persist"));
    }

    @Test
    void toggleFlightEffectTracksAllowAndDisallowPatterns() throws Exception {
        EffToggleFlight allow = parseEffect("allow flight to lane-f-test-player", EffToggleFlight.class);
        EffToggleFlight disallow = parseEffect("disable flight for lane-f-test-player", EffToggleFlight.class);

        assertTrue(readBoolean(allow, "allow"));
        assertFalse(readBoolean(disallow, "allow"));
    }

    @Test
    void transformEffectParsesListVariableAndMappingExpression() throws Exception {
        EffTransform transform = parseEffect("transform {lane-f-test::*} with 0", EffTransform.class);

        assertEquals("{lane-f-test::*}", readField(transform, "unmappedObjects").toString());
        assertNotNull(readField(transform, "mappingExpr"));
    }

    @Test
    void vehicleEffectTracksRideAndDismountPatterns() throws Exception {
        EffVehicle ride = parseEffect("make lane-f-test-entity ride lane-f-test-entity", EffVehicle.class);
        EffVehicle dismount = parseEffect("make lane-f-test-entity dismount", EffVehicle.class);
        EffVehicle eject = parseEffect("eject passengers of lane-f-test-entity", EffVehicle.class);

        assertNotNull(expression(ride, "passengers"));
        assertEquals(null, readField(dismount, "vehicles"));
        assertEquals(null, readField(eject, "passengers"));
    }

    @Test
    void zombifyEffectTracksModeAndOptionalTimespan() throws Exception {
        EffZombify zombify = parseEffect("zombify lane-f-test-livingentity", EffZombify.class);
        EffZombify unzombify = parseEffect("unzombify lane-f-test-livingentity after lane-f-test-timespan", EffZombify.class);

        assertTrue(readBoolean(zombify, "zombify"));
        assertFalse(readBoolean(unzombify, "zombify"));
        assertEquals("lane-f-test-timespan", expression(unzombify, "timespan").toString(null, false));
    }

    @Test
    void commandBlockConditionalEffectTracksNegatedPattern() throws Exception {
        EffCommandBlockConditional conditional = parseEffect("make command block lane-f-test-block conditional", EffCommandBlockConditional.class);
        EffCommandBlockConditional unconditional = parseEffect("make command block lane-f-test-block unconditional", EffCommandBlockConditional.class);

        assertTrue(readBoolean(conditional, "conditional"));
        assertFalse(readBoolean(unconditional, "conditional"));
    }

    @Test
    void glowingTextEffectTracksGlowingAndNormalPatterns() throws Exception {
        EffGlowingText glowing = parseEffect("make lane-f-test-block have glowing text", EffGlowingText.class);
        EffGlowingText normal = parseEffect("make lane-f-test-block have normal text", EffGlowingText.class);

        assertTrue(readBoolean(glowing, "glowing"));
        assertFalse(readBoolean(normal, "glowing"));
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

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {

        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
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
