package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricFireworkEffect;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
final class ExpressionMixedRuntimeM4CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.bootstrap();
        EntityData.register();
        ensureSyntax();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void assignedExpressionsParseInExpectedContexts() {
        assertInstanceOf(ExprArrowKnockbackStrength.class, parseExpression("arrow knockback strength of lane-m4-projectile"));
        assertInstanceOf(ExprArrowPierceLevel.class, parseExpression("arrow pierce level of lane-m4-projectile"));
        assertInstanceOf(ExprBeaconEffects.class, parseExpression("primary beacon effect of lane-m4-block"));
        assertInstanceOf(ExprBeaconRange.class, parseExpression("beacon range of lane-m4-block"));
        assertInstanceOf(ExprBeaconTier.class, parseExpression("beacon tier of lane-m4-block"));
        assertInstanceOf(ExprDropsOfBlock.class, parseExpression("drops of lane-m4-block"));
        assertInstanceOf(ExprEntityAttribute.class, parseExpression("lane-m4-attribute attribute of lane-m4-livingentity"));
        assertInstanceOf(ExprExplosiveYield.class, parseExpression("explosive yield of lane-m4-entity"));
        assertInstanceOf(ExprFireworkEffect.class, parseExpression("flickering lane-m4-firework-type colored lane-m4-color"));
        assertInstanceOf(ExprLastDeathLocation.class, parseExpression("last death location of lane-m4-offlineplayer"));
        assertInstanceOf(ExprLastSpawnedEntity.class, parseExpression("last spawned zombie"));

        ParserInstance parser = ParserInstance.get();
        parser.setCurrentEvent("barter", FabricEventCompatHandles.PiglinBarter.class);
        assertInstanceOf(ExprBarterDrops.class, parseExpression("barter drops"));

        parser.setCurrentEvent("click", FabricEventCompatHandles.Click.class);
        assertInstanceOf(ExprClicked.class, parseExpression("clicked block"));

        parser.setCurrentEvent("explode", FabricEventCompatHandles.Explosion.class);
        assertInstanceOf(ExprExplosionBlockYield.class, parseExpression("explosion's block yield"));
        assertInstanceOf(ExprFertilizedBlocks.class, parseExpressionInEvent("fertilized blocks", FabricEventCompatHandles.BlockFertilize.class));

        parser.setCurrentEvent("respawn", resolveHandle("PlayerRespawn"));
        assertInstanceOf(ExprRespawnLocation.class, parseExpression("respawn location"));
        assertInstanceOf(ExprRespawnReason.class, parseExpression("respawn reason"));

        parser.setCurrentEvent("death", resolveHandle("EntityDeath"));
        assertInstanceOf(ExprDrops.class, parseExpression("drops"));

        parser.setCurrentEvent("explosion prime", resolveHandle("ExplosionPrime"));
        assertInstanceOf(ExprExplosionYield.class, parseExpression("explosion yield"));

        parser.setCurrentEvent("hanging break", resolveHandle("HangingBreak"));
        assertInstanceOf(ExprHanging.class, parseExpression("hanging remover"));
    }

    @Test
    void eventHandleExpressionsReadAndMutateCompatState() throws Exception {
        ExprBarterDrops barterDrops = new ExprBarterDrops();
        assertTrue(barterDrops.init(new Expression[0], 0, Kleenean.FALSE, parseResult("barter drops")));
        FabricEventCompatHandles.PiglinBarter barter = new FabricEventCompatHandles.PiglinBarter(
                new ItemStack(Items.GOLD_INGOT),
                new ArrayList<>(List.of(new ItemStack(Items.DIRT)))
        );
        SkriptEvent barterEvent = new SkriptEvent(barter, null, null, null);
        assertEquals(1, barterDrops.getArray(barterEvent).length);
        barterDrops.change(barterEvent, new Object[]{new FabricItemType(Items.STICK)}, ChangeMode.ADD);
        assertEquals(2, barter.outcome().size());

        Object deathHandle = newHandle("EntityDeath", List.of(new ItemStack(Items.COBBLESTONE)), 3);
        ParserInstance.get().setCurrentEvent("death", resolveHandle("EntityDeath"));
        ExprDrops drops = new ExprDrops();
        assertTrue(drops.init(new Expression[0], 0, Kleenean.FALSE, parseResult("drops")));
        SkriptEvent deathEvent = new SkriptEvent(deathHandle, null, null, null);
        assertEquals(1, drops.getArray(deathEvent).length);
        drops.change(deathEvent, new Object[]{new FabricItemType(Items.APPLE)}, ChangeMode.ADD);
        assertEquals(2, ((List<?>) invoke(deathHandle, "drops")).size());

        ParserInstance.get().setCurrentEvent("explosion prime", resolveHandle("ExplosionPrime"));
        ExprExplosionYield explosionYield = new ExprExplosionYield();
        assertTrue(explosionYield.init(new Expression[0], 0, Kleenean.FALSE, parseResult("explosion yield")));
        Object primeHandle = newHandle("ExplosionPrime", 4.0F, false);
        SkriptEvent primeEvent = new SkriptEvent(primeHandle, null, null, null);
        assertEquals(4.0F, explosionYield.getSingle(primeEvent));
        explosionYield.change(primeEvent, new Object[]{2}, ChangeMode.ADD);
        assertEquals(6.0F, ((Number) invoke(primeHandle, "radius")).floatValue());

        ExprExplosionBlockYield blockYield = new ExprExplosionBlockYield();
        assertTrue(blockYield.init(new Expression[0], 0, Kleenean.FALSE, parseResult("explosion's block yield")));
        FabricEventCompatHandles.Explosion explosion = new FabricEventCompatHandles.Explosion(List.of(), 0.5F);
        SkriptEvent explosionEvent = new SkriptEvent(explosion, null, null, null);
        assertEquals(0.5F, blockYield.getSingle(explosionEvent));
        blockYield.change(explosionEvent, new Object[]{0.25F}, ChangeMode.ADD);
        assertEquals(0.75F, explosion.yield());

        ParserInstance.get().setCurrentEvent("respawn", resolveHandle("PlayerRespawn"));
        ExprRespawnLocation respawnLocation = new ExprRespawnLocation();
        assertTrue(respawnLocation.init(new Expression[0], 0, Kleenean.FALSE, parseResult("respawn location")));
        FabricLocation spawn = new FabricLocation(null, new Vec3(1, 2, 3));
        SkriptEvent respawnEvent = new SkriptEvent(newHandle("PlayerRespawn", spawn, true, false, "bed"), null, null, null);
        assertEquals(spawn, respawnLocation.getSingle(respawnEvent));
        respawnLocation.change(respawnEvent, new Object[]{new FabricLocation(null, new Vec3(4, 5, 6))}, ChangeMode.SET);
        assertEquals(4.0, ((FabricLocation) invoke(respawnEvent.handle(), "respawnLocation")).position().x);

        ExprRespawnReason respawnReason = new ExprRespawnReason();
        assertTrue(respawnReason.init(new Expression[0], 0, Kleenean.FALSE, parseResult("respawn reason")));
        assertEquals("bed", respawnReason.getSingle(respawnEvent));

        ParserInstance.get().setCurrentEvent("hanging break", resolveHandle("HangingBreak"));
        ExprHanging hanging = parseExpression("hanging remover", ExprHanging.class);
        Entity remover = allocateEntity(ItemEntity.class, EntityType.ITEM);
        SkriptEvent hangingEvent = new SkriptEvent(newHandle("HangingBreak", allocateEntity(ItemEntity.class, EntityType.ITEM), remover), null, null, null);
        assertEquals(remover, hanging.getSingle(hangingEvent));
    }

    @Test
    void fireworkAndSpawnExpressionsProduceExpectedValues() throws Exception {
        ExprFireworkEffect fireworkEffect = parseExpression(
                "flickering lane-m4-firework-type colored lane-m4-color",
                ExprFireworkEffect.class
        );
        FabricFireworkEffect built = fireworkEffect.getSingle(SkriptEvent.EMPTY);
        assertNotNull(built);
        assertEquals(FireworkExplosion.Shape.BURST, built.shape());
        assertTrue(built.flicker());

        setStatic("ch.njol.skript.effects.EffFireworkLaunch", "lastSpawned", allocateEntity(FireworkRocketEntity.class, EntityType.FIREWORK_ROCKET));
        ExprLastSpawnedEntity lastSpawned = parseExpression("last launched firework", ExprLastSpawnedEntity.class);
        assertInstanceOf(FireworkRocketEntity.class, lastSpawned.getSingle(SkriptEvent.EMPTY));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(Projectile.class, "projectile");
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(FabricItemType.class, "itemtype");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo((Class) Holder.class, "attributetype");
        registerClassInfo(FireworkExplosion.Shape.class, "fireworktype");
        registerClassInfo(Color.class, "color");
        registerClassInfo(GameProfile.class, "offlineplayer");
        registerClassInfo(FabricLocation.class, "location");
        Skript.registerExpression(TestProjectileExpression.class, Projectile.class, "lane-m4-projectile");
        Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-m4-block");
        Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-m4-entity");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-m4-livingentity");
        Skript.registerExpression(TestAttributeExpression.class, Holder.class, "lane-m4-attribute");
        Skript.registerExpression(TestFireworkTypeExpression.class, FireworkExplosion.Shape.class, "lane-m4-firework-type");
        Skript.registerExpression(TestColorExpression.class, Color.class, "lane-m4-color");
        Skript.registerExpression(TestOfflinePlayerExpression.class, GameProfile.class, "lane-m4-offlineplayer");
        new ExprArrowKnockbackStrength();
        new ExprArrowPierceLevel();
        new ExprBarterDrops();
        new ExprBeaconEffects();
        new ExprBeaconRange();
        new ExprBeaconTier();
        new ExprClicked();
        new ExprDrops();
        new ExprDropsOfBlock();
        new ExprEntityAttribute();
        new ExprExplosionBlockYield();
        new ExprExplosionYield();
        new ExprExplosiveYield();
        new ExprFertilizedBlocks();
        new ExprFireworkEffect();
        new ExprHanging();
        new ExprLastDeathLocation();
        new ExprLastSpawnedEntity();
        new ExprRespawnLocation();
        new ExprRespawnReason();
        syntaxRegistered = true;
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static Expression<?> parseExpression(String input) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{Object.class});
        assertNotNull(parsed, input);
        return parsed;
    }

    private static <T extends Expression<?>> T parseExpression(String input, Class<T> expected) {
        Expression<?> parsed = parseExpression(input);
        assertInstanceOf(expected, parsed);
        return expected.cast(parsed);
    }

    private static Expression<?> parseExpressionInEvent(String input, Class<?> eventClass) {
        ParserInstance.get().setCurrentEvent("m4", eventClass);
        return parseExpression(input);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static Class<?> resolveHandle(String simpleName) {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$" + simpleName);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Object newHandle(String simpleName, Object... args) throws Exception {
        Class<?> type = resolveHandle(simpleName);
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() != args.length) {
                continue;
            }
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        }
        throw new NoSuchMethodException(simpleName);
    }

    private static Object invoke(Object target, String method) throws Exception {
        var resolved = target.getClass().getDeclaredMethod(method);
        resolved.setAccessible(true);
        return resolved.invoke(target);
    }

    private static void setStatic(String className, String fieldName, Object value) throws Exception {
        Class<?> type = Class.forName(className);
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> T allocateEntity(Class<T> type, EntityType<?> entityType) throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);
        T entity = (T) unsafe.allocateInstance(type);
        for (Field candidate : net.minecraft.world.entity.Entity.class.getDeclaredFields()) {
            if (candidate.getType() == EntityType.class) {
                candidate.setAccessible(true);
                candidate.set(entity, entityType);
                break;
            }
        }
        return entity;
    }

    public static final class TestProjectileExpression extends SimpleExpression<Projectile> {
        @Override
        protected Projectile @Nullable [] get(SkriptEvent event) {
            return new Projectile[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Projectile> getReturnType() {
            return Projectile.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }
    }

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return new FabricBlock[]{new FabricBlock(null, BlockPos.ZERO)};
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
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }
    }

    public static class TestEntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return new Entity[0];
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
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
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
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }
    }

    public static final class TestAttributeExpression extends SimpleExpression<Holder> {
        @Override
        protected Holder @Nullable [] get(SkriptEvent event) {
            return new Holder[]{Attributes.MAX_HEALTH};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Holder> getReturnType() {
            return Holder.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }
    }

    public static final class TestFireworkTypeExpression extends SimpleExpression<FireworkExplosion.Shape> {
        @Override
        protected FireworkExplosion.Shape @Nullable [] get(SkriptEvent event) {
            return new FireworkExplosion.Shape[]{FireworkExplosion.Shape.BURST};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FireworkExplosion.Shape> getReturnType() {
            return FireworkExplosion.Shape.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }
    }

    public static final class TestColorExpression extends SimpleExpression<Color> {
        @Override
        protected Color @Nullable [] get(SkriptEvent event) {
            return new Color[]{new ColorRGB(255, 0, 0)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Color> getReturnType() {
            return Color.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }
    }

    public static final class TestOfflinePlayerExpression extends SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return new GameProfile[]{new GameProfile(java.util.UUID.randomUUID(), "LaneM4")};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends GameProfile> getReturnType() {
            return GameProfile.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            return true;
        }
    }
}
