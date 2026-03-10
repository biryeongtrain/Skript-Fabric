package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

final class ExpressionEventContextBundleCompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
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
    void eventHandleExpressionsReadCompatHandles() {
        LivingEntity pig = allocateEntity(Pig.class, EntityType.PIG);
        LivingEntity cow = allocateEntity(Cow.class, EntityType.COW);

        ParserInstance parser = ParserInstance.get();

        parser.setCurrentEvent("area cloud effect", FabricEventCompatHandles.AreaEffectCloudApply.class);
        ExprAffectedEntities affected = new ExprAffectedEntities();
        assertTrue(affected.init(new Expression[0], 0, Kleenean.FALSE, parseResult("affected entities")));
        assertEquals(2, affected.getArray(new SkriptEvent(
                new FabricEventCompatHandles.AreaEffectCloudApply(List.of(pig, cow)),
                null,
                null,
                null
        )).length);

        parser.setCurrentEvent("consume", FabricEventCompatHandles.Item.class);
        ExprConsumedItem consumed = new ExprConsumedItem();
        assertTrue(consumed.init(new Expression[0], 0, Kleenean.FALSE, parseResult("consumed item")));
        assertEquals(Items.CARROT, consumed.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.Item(FabricEventCompatHandles.ItemAction.CONSUME, new ItemStack(Items.CARROT), false),
                null,
                null,
                null
        )).getItem());

        parser.setCurrentEvent("experience cooldown change", FabricEventCompatHandles.ExperienceCooldownChange.class);
        ExprExperienceCooldownChangeReason cooldownReason = new ExprExperienceCooldownChangeReason();
        assertTrue(cooldownReason.init(new Expression[0], 0, Kleenean.FALSE, parseResult("xp cooldown change reason")));
        assertEquals("orb pickup", cooldownReason.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.ExperienceCooldownChange("orb pickup"),
                null,
                null,
                null
        )));

        parser.setCurrentEvent("heal", FabricEventCompatHandles.Healing.class);
        ExprHealAmount healAmount = new ExprHealAmount();
        assertTrue(healAmount.init(new Expression[0], 0, Kleenean.FALSE, parseResult("heal amount")));
        assertEquals(4.5F, healAmount.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.Healing(pig, "magic", 4.5F),
                null,
                null,
                null
        )));

        parser.setCurrentEvent("explode", FabricEventCompatHandles.Explosion.class);
        ExprExplodedBlocks explodedBlocks = new ExprExplodedBlocks();
        assertTrue(explodedBlocks.init(new Expression[0], 0, Kleenean.FALSE, parseResult("exploded blocks")));
        assertEquals(2, explodedBlocks.getArray(new SkriptEvent(
                new FabricEventCompatHandles.Explosion(List.of(
                        new FabricBlock(null, BlockPos.ZERO),
                        new FabricBlock(null, BlockPos.ZERO.above())
                )),
                null,
                null,
                null
        )).length);

        parser.setCurrentEvent("piglin barter", FabricEventCompatHandles.PiglinBarter.class);
        ExprBarterInput barterInput = new ExprBarterInput();
        assertTrue(barterInput.init(new Expression[0], 0, Kleenean.FALSE, parseResult("barter input")));
        assertEquals(Items.GOLD_INGOT, barterInput.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.PiglinBarter(new ItemStack(Items.GOLD_INGOT)),
                null,
                null,
                null
        )).getItem());
    }

    @Test
    void eggThrowExpressionsReadAndMutateCompatHandle() {
        ParserInstance.get().setCurrentEvent("egg throw", eggThrowEventClass());
        MutableEggThrowHandle handle = new MutableEggThrowHandle(true, (byte) 1, EntityType.CHICKEN);
        SkriptEvent event = new SkriptEvent(handle, null, null, null);

        ExprHatchingNumber number = new ExprHatchingNumber();
        assertTrue(number.init(new Expression[0], 0, Kleenean.FALSE, parseResult("hatching number")));
        assertEquals((byte) 1, number.getSingle(event));
        number.change(event, new Object[]{2}, ChangeMode.ADD);
        assertEquals((byte) 3, handle.hatches());

        ExprHatchingType type = new ExprHatchingType();
        assertTrue(type.init(new Expression[0], 0, Kleenean.FALSE, parseResult("hatching entity type")));
        assertNotNull(type.getSingle(event));
        type.change(event, new Object[]{EntityData.parse("pig")}, ChangeMode.SET);
        assertEquals(EntityType.PIG, handle.hatchingType());
    }

    @Test
    void propertyExpressionsParseWithRegisteredSources() {
        assertInstanceOf(ExprLastAttacker.class, parseExpression("last attacker of lane-e-m3-livingentity", Entity.class));
        assertInstanceOf(ExprLeashHolder.class, parseExpression("leash holder of lane-e-m3-livingentity", Entity.class));
    }

    @Test
    void eventExpressionsParseInExpectedContexts() {
        ParserInstance parser = ParserInstance.get();

        parser.setCurrentEvent("shoot bow", FabricEventCompatHandles.EntityShootBow.class);
        assertInstanceOf(ExprConsumedItem.class, parseExpression("consumed item", ItemStack.class));

        parser.setCurrentEvent("experience cooldown change", FabricEventCompatHandles.ExperienceCooldownChange.class);
        assertInstanceOf(ExprExperienceCooldownChangeReason.class, parseExpression("xp cooldown change reason", String.class));

        parser.setCurrentEvent("explode", FabricEventCompatHandles.Explosion.class);
        assertInstanceOf(ExprExplodedBlocks.class, parseExpression("exploded blocks", FabricBlock.class));
    }

    @Test
    void propertyExpressionsReturnNullWithoutBackingState() {
        ExprLastAttacker lastAttacker = new ExprLastAttacker();
        assertTrue(lastAttacker.init(new Expression[]{new TestLivingEntityExpression()}, 0, Kleenean.FALSE, parseResult("")));
        assertNull(lastAttacker.getSingle(SkriptEvent.EMPTY));

        ExprLeashHolder leashHolder = new ExprLeashHolder();
        assertTrue(leashHolder.init(new Expression[]{new TestLivingEntityExpression()}, 0, Kleenean.FALSE, parseResult("")));
        assertNull(leashHolder.getSingle(SkriptEvent.EMPTY));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(LivingEntity.class, "livingentity");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-e-m3-livingentity");
        new ExprAffectedEntities();
        new ExprConsumedItem();
        new ExprExplodedBlocks();
        new ExprExperienceCooldownChangeReason();
        new ExprHealAmount();
        new ExprLastAttacker();
        new ExprLeashHolder();
        new ExprHatchingNumber();
        new ExprHatchingType();
        new ExprBarterInput();
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
    private static <T extends LivingEntity> T allocateEntity(Class<T> type, EntityType<?> entityType) {
        try {
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
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Unsafe unsafe() throws ReflectiveOperationException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static Class<?> eggThrowEventClass() {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerEggThrow");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static final class MutableEggThrowHandle implements org.skriptlang.skript.fabric.runtime.FabricEggThrowEventHandle {

        private boolean hatching;
        private byte hatches;
        private EntityType<?> hatchingType;

        private MutableEggThrowHandle(boolean hatching, byte hatches, EntityType<?> hatchingType) {
            this.hatching = hatching;
            this.hatches = hatches;
            this.hatchingType = hatchingType;
        }

        @Override
        public boolean hatching() {
            return hatching;
        }

        @Override
        public void setHatching(boolean hatching) {
            this.hatching = hatching;
        }

        @Override
        public byte hatches() {
            return hatches;
        }

        @Override
        public void setHatches(byte hatches) {
            this.hatches = hatches;
        }

        @Override
        public EntityType<?> hatchingType() {
            return hatchingType;
        }

        @Override
        public void setHatchingType(EntityType<?> hatchingType) {
            this.hatchingType = hatchingType;
        }
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) {
            return new LivingEntity[0];
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
            return "lane-e-m3-livingentity";
        }
    }
}
