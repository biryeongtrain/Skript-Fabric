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
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
public final class EffectMixedImportCompatibilityTest {

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
            registerClassInfo(Number.class, "number");
            registerClassInfo(String.class, "string");
            registerClassInfo(Color.class, "color");
            registerClassInfo(FabricItemType.class, "itemtype");
            registerClassInfo(FabricLocation.class, "location");
            registerClassInfo(Object.class, "enchantmenttype");
            registerClassInfo(Object.class, "fireworkeffect");
            registerClassInfo(Entity.class, "entity");
            registerClassInfo(LivingEntity.class, "livingentity");
            registerClassInfo(ServerPlayer.class, "player");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestStringExpression.class, String.class, "lane-f-test-string");
            Skript.registerExpression(TestColorExpression.class, Color.class, "lane-f-test-color");
            Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-f-test-itemtype");
            Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-f-test-location");
            Skript.registerExpression(TestEnchantmentExpression.class, Object.class, "lane-f-test-enchantment");
            Skript.registerExpression(TestFireworkEffectExpression.class, Object.class, "lane-f-test-firework-effect");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        EffColorItems.register();
        EffEnchant.register();
        EffEquip.register();
        EffDrop.register();
        EffHealth.register();
        EffTeleport.register();
        EffWakeupSleep.register();
        EffFireworkLaunch.register();
        EffElytraBoostConsume.register();
        EffExplosion.register();
        EffTree.register();
        EffEntityVisibility.register();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void colorItemsParsesLiteralAndRgbVariants() throws Exception {
        EffColorItems named = parseEffect("dye lane-f-test-itemtype lane-f-test-color", EffColorItems.class);
        EffColorItems rgb = parseEffect("paint lane-f-test-itemtype (lane-f-test-number, lane-f-test-number, lane-f-test-number)", EffColorItems.class);

        assertEquals("lane-f-test-itemtype", expression(named, "items").toString(null, false));
        assertEquals("lane-f-test-color", expression(named, "color").toString(null, false));
        assertTrue(expression(rgb, "color").toString(null, false).contains("lane-f-test-number"));
    }

    @Test
    void enchantEquipHealthAndFireworkEffectsBindExpectedFields() throws Exception {
        EffEnchant enchant = parseEffect("naturally enchant lane-f-test-itemtype at level lane-f-test-number allowing treasure enchantments", EffEnchant.class);
        EffEquip equip = parseEffect("unequip lane-f-test-itemtype from lane-f-test-livingentity", EffEquip.class);
        EffHealth repair = parseEffect("repair lane-f-test-itemtype by lane-f-test-number", EffHealth.class);
        EffFireworkLaunch firework = parseEffect(
                "launch a firework with effect lane-f-test-firework-effect at lane-f-test-location with duration lane-f-test-number",
                EffFireworkLaunch.class
        );

        assertEquals("lane-f-test-itemtype", expression(enchant, "items").toString(null, false));
        assertTrue(readBoolean(enchant, "treasure"));
        assertFalse(readBoolean(equip, "equip"));
        assertEquals("lane-f-test-number", expression(repair, "amount").toString(null, false));
        assertEquals("lane-f-test-location", expression(firework, "locations").toString(null, false));
    }

    @Test
    void explosionEffectParsesNormalAndVisualVariants() throws Exception {
        EffExplosion normal = parseEffect("create explosion of force lane-f-test-number at lane-f-test-location", EffExplosion.class);
        EffExplosion visual = parseEffect("create explosion effect at lane-f-test-location", EffExplosion.class);

        assertEquals("lane-f-test-number", expression(normal, "force").toString(null, false));
        assertEquals("lane-f-test-location", expression(normal, "locations").toString(null, false));
        assertEquals("lane-f-test-location", expression(visual, "locations").toString(null, false));
        assertFalse(readBoolean(visual, "blockDamage"));
    }

    @Test
    void blockedEffectsFailInitUntilCompatSurfaceExists() {
        assertFalse(new EffDrop().init(new Expression[]{new TestItemTypeExpression(), new TestStringExpression(), new TestLocationExpression()}, 0, Kleenean.FALSE, new ParseResult()));
        assertFalse(new EffTeleport().init(new Expression[]{new TestEntityExpression(), new TestStringExpression(), new TestLocationExpression(), new TestStringExpression()}, 0, Kleenean.FALSE, new ParseResult()));
        assertFalse(new EffWakeupSleep().init(new Expression[]{new TestLivingEntityExpression(), new TestStringExpression(), new TestLocationExpression()}, 0, Kleenean.FALSE, new ParseResult()));
        assertFalse(new EffTree().init(new Expression[]{new TestStringExpression(), new TestStringExpression(), new TestLocationExpression()}, 0, Kleenean.FALSE, new ParseResult()));
        assertFalse(new EffElytraBoostConsume().init(new Expression<?>[0], 0, Kleenean.FALSE, new ParseResult()));
        assertTrue(new EffEntityVisibility().init(new Expression[]{new TestEntityExpression(), new TestPlayerExpression()}, 0, Kleenean.FALSE, new ParseResult()));
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

    public static final class TestColorExpression extends SimpleExpression<Color> {
        @Override
        protected Color @Nullable [] get(SkriptEvent event) {
            return new Color[]{new Color() {
                @Override
                public int red() {
                    return 1;
                }

                @Override
                public int green() {
                    return 2;
                }

                @Override
                public int blue() {
                    return 3;
                }

                @Override
                public String toString() {
                    return "lane-f-test-color";
                }
            }};
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
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-color";
        }
    }

    public static final class TestItemTypeExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-itemtype";
        }
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{new FabricLocation((ServerLevel) null, Vec3.ZERO)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricLocation> getReturnType() {
            return FabricLocation.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-location";
        }
    }

    public static final class TestEnchantmentExpression extends SimpleExpression<Object> {
        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return new Object[]{"enchantment"};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-enchantment";
        }
    }

    public static final class TestFireworkEffectExpression extends SimpleExpression<Object> {
        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return new Object[]{"firework-effect"};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-firework-effect";
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

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
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
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
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
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-player";
        }
    }
}
