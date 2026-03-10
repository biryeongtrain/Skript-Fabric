package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

@Tag("isolated-registry")
final class ExpressionEventPayloadBundleCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        EntityData.register();
        if (Classes.getExactClassInfo(FabricItemType.class) == null) {
            Classes.registerClassInfo(new ch.njol.skript.classes.ClassInfo<>(FabricItemType.class, "itemtype"));
        }
        if (Classes.getExactClassInfo(FabricBlock.class) == null) {
            Classes.registerClassInfo(new ch.njol.skript.classes.ClassInfo<>(FabricBlock.class, "block"));
        }
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
        ch.njol.skript.effects.EffDrop.lastSpawned = null;
        ch.njol.skript.effects.EffLightning.lastSpawned = null;
        ch.njol.skript.effects.EffFireworkLaunch.lastSpawned = null;
    }

    @Test
    void barterDropsReadsAndMutatesCompatHandle() {
        ParserInstance.get().setCurrentEvent("piglin barter", FabricEventCompatHandles.PiglinBarter.class);
        ExprBarterDrops expression = new ExprBarterDrops();
        assertTrue(expression.init(new Expression[0], 0, Kleenean.FALSE, parseResult("barter drops")));

        List<ItemStack> outcome = new ArrayList<>();
        outcome.add(new ItemStack(Items.APPLE));
        SkriptEvent event = new SkriptEvent(new FabricEventCompatHandles.PiglinBarter(new ItemStack(Items.GOLD_INGOT), outcome), null, null, null);

        assertEquals(1, expression.getArray(event).length);
        expression.change(event, new Object[]{new FabricItemType(Items.STICK)}, ChangeMode.ADD);
        assertEquals(2, outcome.size());
        expression.change(event, new Object[]{new FabricItemType(Items.APPLE)}, ChangeMode.REMOVE);
        assertEquals(1, outcome.size());
        assertEquals(Items.STICK, outcome.get(0).getItem());
    }

    @Test
    void clickedExplosionFertilizedAndHangingExpressionsReadCompatHandles() {
        ExprClicked clickedBlock = new ExprClicked();
        assertTrue(clickedBlock.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STONE), false)}, 0, Kleenean.FALSE, parseResult("clicked block")));
        Object clicked = clickedBlock.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.Click(null, BlockPos.ZERO, FabricEventCompatHandles.ClickType.RIGHT, null, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), null),
                null,
                null,
                null
        ));
        assertTrue(clicked instanceof FabricBlock);

        ExprClicked clickedEntity = new ExprClicked();
        assertTrue(clickedEntity.init(new Expression[]{null}, 0, Kleenean.FALSE, parseResult("clicked entity")));
        ArmorStand armorStand = allocateEntity(ArmorStand.class, EntityType.ARMOR_STAND);
        assertEquals(armorStand, clickedEntity.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.Click(null, BlockPos.ZERO, FabricEventCompatHandles.ClickType.RIGHT, armorStand, null, null),
                null,
                null,
                null
        )));

        ExprExplosionBlockYield explosionBlockYield = new ExprExplosionBlockYield();
        assertTrue(explosionBlockYield.init(new Expression[0], 0, Kleenean.FALSE, parseResult("explosion block yield")));
        FabricEventCompatHandles.Explosion explosion = new FabricEventCompatHandles.Explosion(List.of(new FabricBlock(null, BlockPos.ZERO)), 0.75F);
        SkriptEvent explosionEvent = new SkriptEvent(explosion, null, null, null);
        assertEquals(0.75F, explosionBlockYield.getSingle(explosionEvent).floatValue());
        explosionBlockYield.change(explosionEvent, new Object[]{0.25F}, ChangeMode.REMOVE);
        assertEquals(0.5F, explosion.yield());

        ExprFertilizedBlocks fertilizedBlocks = new ExprFertilizedBlocks();
        assertTrue(fertilizedBlocks.init(new Expression[0], 0, Kleenean.FALSE, parseResult("fertilized blocks")));
        assertEquals(1, fertilizedBlocks.getArray(new SkriptEvent(
                new FabricEventCompatHandles.BlockFertilize(List.of(new FabricBlock(null, BlockPos.ZERO))),
                null,
                null,
                null
        )).length);

        ExprHanging hanging = new ExprHanging();
        assertTrue(hanging.init(new Expression[0], 0, Kleenean.FALSE, parseResult("hanging entity")));
        ArmorStand frame = allocateEntity(ArmorStand.class, EntityType.ARMOR_STAND);
        ArmorStand remover = allocateEntity(ArmorStand.class, EntityType.ARMOR_STAND);
        assertEquals(frame, hanging.getSingle(new SkriptEvent(new HangingBreakHandle(frame, remover), null, null, null)));
    }

    @Test
    void explosionYieldLastSpawnedAndEntityPropertiesMutateState() {
        ExprExplosionYield explosionYield = new ExprExplosionYield();
        assertTrue(explosionYield.init(new Expression[0], 0, Kleenean.FALSE, parseResult("explosion yield")));
        MutableExplosionPrimeHandle explosionPrime = new MutableExplosionPrimeHandle(1.0F, true);
        SkriptEvent explosionEvent = new SkriptEvent(explosionPrime, null, null, null);
        explosionYield.change(explosionEvent, new Object[]{2.0F}, ChangeMode.ADD);
        assertEquals(3.0F, explosionPrime.radius());

        ItemEntity itemEntity = allocateEntity(ItemEntity.class, EntityType.ITEM);
        ch.njol.skript.effects.EffDrop.lastSpawned = itemEntity;
        ExprLastSpawnedEntity lastDropped = new ExprLastSpawnedEntity();
        SkriptParser.ParseResult droppedParse = parseResult("last dropped item");
        droppedParse.mark = 2;
        assertTrue(lastDropped.init(new Expression[0], 1, Kleenean.FALSE, droppedParse));
        assertEquals(itemEntity, lastDropped.getSingle(SkriptEvent.EMPTY));

        Arrow arrow = allocateEntity(Arrow.class, EntityType.ARROW);
        ExprArrowKnockbackStrength knockback = new ExprArrowKnockbackStrength();
        assertTrue(knockback.init(new Expression[]{new SimpleLiteral<>(arrow, false)}, 0, Kleenean.FALSE, parseResult("")));
        knockback.change(SkriptEvent.EMPTY, new Object[]{2}, ChangeMode.SET);
        assertEquals(2L, knockback.getSingle(SkriptEvent.EMPTY));

        ExprArrowPierceLevel pierceLevel = new ExprArrowPierceLevel();
        assertTrue(pierceLevel.init(new Expression[]{new SimpleLiteral<>(arrow, false)}, 0, Kleenean.FALSE, parseResult("")));
        pierceLevel.change(SkriptEvent.EMPTY, new Object[]{3}, ChangeMode.SET);
        assertEquals(3L, pierceLevel.getSingle(SkriptEvent.EMPTY));

        Creeper creeper = allocateEntity(Creeper.class, EntityType.CREEPER);
        ExprExplosiveYield explosiveYield = new ExprExplosiveYield();
        assertTrue(explosiveYield.init(new Expression[]{new SimpleLiteral<>(creeper, false)}, 0, Kleenean.FALSE, parseResult("")));
        explosiveYield.change(SkriptEvent.EMPTY, new Object[]{5}, ChangeMode.SET);
        assertEquals(5, explosiveYield.getSingle(SkriptEvent.EMPTY).intValue());

        LightningBolt lightning = allocateEntity(LightningBolt.class, EntityType.LIGHTNING_BOLT);
        ch.njol.skript.effects.EffLightning.lastSpawned = lightning;
        ExprLastSpawnedEntity lastLightning = new ExprLastSpawnedEntity();
        SkriptParser.ParseResult lightningParse = parseResult("last struck lightning");
        lightningParse.mark = 3;
        assertTrue(lastLightning.init(new Expression[0], 2, Kleenean.FALSE, lightningParse));
        assertEquals(lightning, lastLightning.getSingle(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.entity.Entity> T allocateEntity(Class<T> type, EntityType<?> entityType) {
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
            assertNotNull(entityTypeField);
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

    private static final class MutableExplosionPrimeHandle {

        private float radius;
        private boolean causesFire;

        private MutableExplosionPrimeHandle(float radius, boolean causesFire) {
            this.radius = radius;
            this.causesFire = causesFire;
        }

        public float radius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public boolean causesFire() {
            return causesFire;
        }
    }

    private record HangingBreakHandle(net.minecraft.world.entity.Entity entity, @Nullable net.minecraft.world.entity.Entity remover) {
    }
}
