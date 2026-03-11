package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

final class ExpressionSyntaxS4CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapSyntax() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        EntityData.register();
        ensureSyntax();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void parseCoverageIncludesStrongSyntax4Subset() {
        assertInstanceOf(ExprVehicle.class, parseExpression("vehicle of lane-s4-entity", Object.class));
        assertInstanceOf(ExprPassenger.class, parseExpression("passengers of lane-s4-entity", Object.class));
        assertInstanceOf(ExprTarget.class, parseExpression("target of lane-s4-livingentity", Object.class));
        assertInstanceOf(ExprShooter.class, parseExpression("shooter of lane-s4-projectile", Object.class));
    }

    @Test
    void reasonExpressionsReadCompatHandles() {
        ParserInstance parser = ParserInstance.get();

        parser.setCurrentEvent("entity transform", FabricEventCompatHandles.EntityTransform.class);
        ExprTransformReason transformReason = new ExprTransformReason();
        assertTrue(transformReason.init(new Expression[0], 0, Kleenean.FALSE, parseResult("transform reason")));
        assertEquals("curing", transformReason.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.EntityTransform(null, "curing"),
                null,
                null,
                null
        )));

        parser.setCurrentEvent("unleash", FabricEventCompatHandles.Leash.class);
        ExprUnleashReason unleashReason = new ExprUnleashReason();
        assertTrue(unleashReason.init(new Expression[0], 0, Kleenean.FALSE, parseResult("unleash reason")));
        assertEquals("player_unleash", unleashReason.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.Leash(null, FabricEventCompatHandles.LeashAction.PLAYER_UNLEASH),
                null,
                null,
                null
        )));
    }

    @Test
    void shooterDefaultsToShootBowHandle() {
        ParserInstance.get().setCurrentEvent("shoot bow", FabricEventCompatHandles.EntityShootBow.class);
        LivingEntity shooterEntity = allocateEntity(Pig.class);
        ExprShooter shooter = new ExprShooter();
        assertTrue(shooter.init(new Expression[1], 0, Kleenean.FALSE, parseResult("shooter")));
        assertEquals(shooterEntity, shooter.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.EntityShootBow(shooterEntity, null),
                null,
                null,
                null
        )));
    }

    @Test
    void vehicleAndPassengerExpressionsReadEntityRelationships() throws Exception {
        Entity vehicle = allocateEntity(Pig.class);
        Entity passenger = allocateEntity(Zombie.class);
        vehicleField().set(passenger, vehicle);
        passengerListField().set(vehicle, ImmutableList.of(passenger));

        ExprVehicle vehicleExpression = new ExprVehicle();
        assertTrue(vehicleExpression.init(new Expression[]{new SimpleLiteral<>(passenger, false)}, 0, Kleenean.FALSE, parseResult("vehicle")));
        assertEquals(vehicle, vehicleExpression.getSingle(SkriptEvent.EMPTY));

        ExprPassenger passengerExpression = new ExprPassenger();
        assertTrue(passengerExpression.init(new Expression[]{new SimpleLiteral<>(vehicle, false)}, 0, Kleenean.FALSE, parseResult("passengers")));
        assertEquals(passenger, passengerExpression.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void targetExpressionReadsMobTarget() throws Exception {
        Mob mob = allocateEntity(Zombie.class);
        LivingEntity target = allocateEntity(Pig.class);
        mobTargetField().set(mob, target);

        ExprTarget targetExpression = new ExprTarget();
        assertTrue(targetExpression.init(new Expression[]{new TestLivingEntityExpression(mob)}, 0, Kleenean.FALSE, parseResult("target")));
        assertEquals(target, targetExpression.getSingle(SkriptEvent.EMPTY));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(Projectile.class, "projectile");
        registerClassInfo(Mob.class, "mob");
        Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-s4-entity");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-s4-livingentity");
        Skript.registerExpression(TestProjectileExpression.class, Projectile.class, "lane-s4-projectile");
        new ExprPassenger();
        new ExprShooter();
        new ExprTarget();
        new ExprTransformReason();
        new ExprUnleashReason();
        new ExprVehicle();
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

    private static SkriptParser.ParseResult parseResult(String expression) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expression;
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> T allocateEntity(Class<T> type) {
        try {
            return (T) unsafe().allocateInstance(type);
        } catch (InstantiationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Field vehicleField() {
        for (Field field : Entity.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Entity.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Could not find vehicle field");
    }

    private static Field passengerListField() {
        for (Field field : Entity.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                    && field.getName().toLowerCase().contains("passenger")
                    && field.getType().getName().contains("ImmutableList")) {
                field.setAccessible(true);
                return field;
            }
        }
        for (Field field : Entity.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType().getName().contains("ImmutableList")) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Could not find passenger list field");
    }

    private static Field mobTargetField() {
        for (Field field : Mob.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                    && field.getName().toLowerCase().contains("target")
                    && field.getType() == LivingEntity.class) {
                field.setAccessible(true);
                return field;
            }
        }
        for (Field field : Mob.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == LivingEntity.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Could not find mob target field");
    }

    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static final class TestEntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Entity> getReturnType() {
            return Entity.class;
        }
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {
        private final @Nullable LivingEntity value;

        public TestLivingEntityExpression() {
            this(null);
        }

        private TestLivingEntityExpression(@Nullable LivingEntity value) {
            this.value = value;
        }

        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) {
            return value == null ? null : new LivingEntity[]{value};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }
    }

    public static final class TestProjectileExpression extends SimpleExpression<Projectile> {
        @Override
        protected Projectile @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Projectile> getReturnType() {
            return Projectile.class;
        }
    }
}
