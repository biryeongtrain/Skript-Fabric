package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.FabricDamageEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricDamageSourceEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

class ExpressionCombatContextCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        EntityData.register();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void damageExpressionsRespectCurrentEventRestrictions() {
        ParserInstance parser = ParserInstance.get();

        ExprDamage damage = new ExprDamage();
        assertFalse(damage.init(new Expression[0], 0, Kleenean.FALSE, parseResult("damage")));

        parser.setCurrentEvent("damage", FabricDamageEventHandle.class);

        ExprAttacked attacked = new ExprAttacked();
        assertTrue(attacked.init(new Expression[0], 0, Kleenean.FALSE, parseResult("victim")));

        ExprAttacker attacker = new ExprAttacker();
        assertTrue(attacker.init(new Expression[0], 0, Kleenean.FALSE, parseResult("attacker")));

        assertTrue(damage.init(new Expression[0], 0, Kleenean.FALSE, parseResult("damage")));

        ExprFinalDamage finalDamage = new ExprFinalDamage();
        assertTrue(finalDamage.init(new Expression[0], 0, Kleenean.FALSE, parseResult("final damage")));
    }

    @Test
    void damageContextExpressionsReadCompatHandles() throws Exception {
        LivingEntity victim = allocateEntity(Zombie.class, EntityType.ZOMBIE);
        LivingEntity source = allocateEntity(Skeleton.class, EntityType.SKELETON);
        DamageSource damageSource = newDamageSource(source);
        SkriptEvent event = new SkriptEvent(new TestDamageHandle(victim, damageSource, 6.5F), null, null, null);
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            ExprAttacked attacked = new ExprAttacked();
            assertTrue(attacked.init(new Expression[0], 0, Kleenean.FALSE, parseResult("victim")));

            ExprAttacker attacker = new ExprAttacker();
            assertTrue(attacker.init(new Expression[0], 0, Kleenean.FALSE, parseResult("attacker")));
            assertNotNull(attacker.getSingle(event));

            ExprDamage damage = new ExprDamage();
            parser.setCurrentEvent("damage", FabricDamageEventHandle.class);
            assertTrue(damage.init(new Expression[0], 0, Kleenean.FALSE, parseResult("damage")));
            assertEquals(6.5F, damage.getSingle(event));

            ExprFinalDamage finalDamage = new ExprFinalDamage();
            assertTrue(finalDamage.init(new Expression[0], 0, Kleenean.FALSE, parseResult("final damage")));
            assertEquals(6.5F, finalDamage.getSingle(event));

            ExprDamageCause cause = new ExprDamageCause();
            assertTrue(cause.init(new Expression[0], 0, Kleenean.FALSE, parseResult("damage cause")));
            assertEquals("generic", cause.getSingle(event));
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    @Test
    void experienceAndHealReasonUseCompatHandles() {
        ParserInstance parser = ParserInstance.get();

        parser.setCurrentEvent("experience spawn", FabricEventCompatHandles.ExperienceSpawn.class);
        ExprExperience spawned = new ExprExperience();
        assertTrue(spawned.init(new Expression[0], 0, Kleenean.FALSE, parseResult("experience")));
        assertEquals(9, spawned.getSingle(new SkriptEvent(new FabricEventCompatHandles.ExperienceSpawn(9), null, null, null)).getXP());

        ExprHealReason healReason = new ExprHealReason();
        assertTrue(healReason.init(new Expression[0], 0, Kleenean.FALSE, parseResult("heal reason")));
        assertEquals("magic", healReason.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.Healing(null, "magic", 4.0F),
                null,
                null,
                null
        )));
    }

    @Test
    void lastDamageCauseReturnsNullWithoutRecordedSource() {
        ExprLastDamageCause expression = new ExprLastDamageCause();
        expression.init(new Expression[]{new SimpleLiteral<>(new LivingEntity[0], LivingEntity.class, true)}, 0, Kleenean.FALSE, parseResult(""));
        assertNull(expression.getSingle(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static DamageSource newDamageSource(LivingEntity source) throws Exception {
        Holder<DamageType> holder = null;
        Constructor<DamageSource> constructor = DamageSource.class.getDeclaredConstructor(Holder.class, net.minecraft.world.entity.Entity.class, net.minecraft.world.entity.Entity.class, net.minecraft.world.phys.Vec3.class);
        constructor.setAccessible(true);
        return constructor.newInstance(holder, source, source, null);
    }

    @SuppressWarnings("unchecked")
    private static <T extends LivingEntity> T allocateEntity(Class<T> type, EntityType<?> entityType) throws Exception {
        Unsafe unsafe = unsafe();
        T entity = (T) unsafe.allocateInstance(type);
        Field entityTypeField = null;
        for (Field field : net.minecraft.world.entity.Entity.class.getDeclaredFields()) {
            if (field.getType() == EntityType.class) {
                entityTypeField = field;
                break;
            }
        }
        if (entityTypeField == null) {
            throw new IllegalStateException("Could not find entity type field");
        }
        entityTypeField.setAccessible(true);
        entityTypeField.set(entity, entityType);
        return entity;
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static void restoreEventContext(ParserInstance parser, String previousEventName, Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
            return;
        }
        parser.setCurrentEvent(previousEventName, previousEventClasses);
    }

    private record TestDamageHandle(
            LivingEntity entity,
            DamageSource damageSource,
            float amount
    ) implements FabricDamageEventHandle, FabricDamageSourceEventHandle {
    }
}
