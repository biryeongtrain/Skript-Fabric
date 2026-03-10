package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.FabricPlayerClientState;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;
import sun.misc.Unsafe;

class ConditionM1CompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        EntityData.register();
    }

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void newConditionClassesInstantiate() {
        assertDoesNotThrow(CondChatColors::new);
        assertDoesNotThrow(CondChatFiltering::new);
        assertDoesNotThrow(CondChatVisibility::new);
        assertDoesNotThrow(CondElytraBoostConsume::new);
        assertDoesNotThrow(CondFromMobSpawner::new);
        assertDoesNotThrow(CondHasClientWeather::new);
        assertDoesNotThrow(CondHasMetadata::new);
        assertDoesNotThrow(CondHasResourcePack::new);
        assertDoesNotThrow(CondIsPluginEnabled::new);
        assertDoesNotThrow(CondIsSpawnable::new);
        assertDoesNotThrow(CondLeashed::new);
    }

    @Test
    void chatAndResourcePackConditionsReadPlayerState() throws Exception {
        ServerPlayer fullColors = allocate(ServerPlayer.class);
        setField(fullColors, "chatVisibility", ChatVisiblity.FULL);
        setField(fullColors, "canChatColor", true);

        ServerPlayer filtered = allocate(ServerPlayer.class);
        setField(filtered, "chatVisibility", ChatVisiblity.SYSTEM);
        setField(filtered, "textFilteringEnabled", true);

        CondChatColors colors = new CondChatColors();
        colors.init(new Expression[]{new SimpleLiteral<>(fullColors, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(colors.check(SkriptEvent.EMPTY));

        CondChatFiltering filtering = new CondChatFiltering();
        filtering.init(new Expression[]{new SimpleLiteral<>(filtered, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(filtering.check(SkriptEvent.EMPTY));

        CondChatVisibility visibility = new CondChatVisibility();
        visibility.init(new Expression[]{new SimpleLiteral<>(filtered, false)}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(visibility.check(SkriptEvent.EMPTY));

        FabricPlayerClientState.setResourcePackStatus(fullColors, TestPackStatus.SUCCESSFULLY_LOADED);
        CondHasResourcePack resourcePack = new CondHasResourcePack();
        resourcePack.init(new Expression[]{new SimpleLiteral<>(fullColors, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertDoesNotThrow(() -> resourcePack.check(SkriptEvent.EMPTY));
    }

    @Test
    void importedConditionsKeepExpectedStringsAndFallbackChecks() throws Exception {
        CondFromMobSpawner spawner = new CondFromMobSpawner();
        spawner.init(new Expression[]{new TestExpression<>("zombies", Entity.class)}, 2, Kleenean.FALSE, parseResult(""));
        assertTrue(spawner.toString(SkriptEvent.EMPTY, false).contains("mob spawner"));

        CondHasMetadata metadata = new CondHasMetadata();
        metadata.init(
                new Expression[]{
                        new SimpleLiteral<>(Map.of("healer", true), false),
                        new SimpleLiteral<>("healer", false)
                },
                0,
                Kleenean.FALSE,
                parseResult("")
        );
        assertTrue(metadata.check(SkriptEvent.EMPTY));

        CondHasClientWeather weather = new CondHasClientWeather();
        weather.init(new Expression[]{new SimpleLiteral<>(allocate(ServerPlayer.class), false)}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(weather.check(SkriptEvent.EMPTY));

        CondIsPluginEnabled enabled = new CondIsPluginEnabled();
        enabled.init(new Expression[]{new SimpleLiteral<>("fabricloader", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertDoesNotThrow(() -> enabled.check(SkriptEvent.EMPTY));

        CondIsPluginEnabled pluginText = new CondIsPluginEnabled();
        pluginText.init(new Expression[]{new SimpleLiteral<>("fabricloader", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("plugin [fabricloader] is enabled", pluginText.toString(SkriptEvent.EMPTY, false));

        CondIsSpawnable spawnable = new CondIsSpawnable();
        spawnable.init(
                new Expression[]{
                        new SimpleLiteral<>(EntityData.parse("pig"), false),
                        new SimpleLiteral<>(allocate(ServerLevel.class), false)
                },
                0,
                Kleenean.FALSE,
                parseResult("")
        );
        assertTrue(spawnable.check(SkriptEvent.EMPTY));

        CondLeashed leashed = new CondLeashed();
        leashed.init(new Expression[]{new TestExpression<>("entity", net.minecraft.world.entity.LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("entity is leashed", leashed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void elytraBoostConditionRemainsEventBound() {
        ParserInstance.get().setCurrentEvent("elytra boost", eventClass("PlayerElytraBoost"));
        CondElytraBoostConsume consume = new CondElytraBoostConsume();
        assertTrue(consume.init(new Expression[0], 0, Kleenean.FALSE, parseResult("")));
        assertTrue(consume.check(new SkriptEvent(new ElytraBoostHandle(true), null, null, null)));

        ParserInstance.get().deleteCurrentEvent();
        assertFalse(new CondElytraBoostConsume().init(new Expression[0], 0, Kleenean.FALSE, parseResult("")));
    }

    private static Class<?> eventClass(String simpleName) {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$" + simpleName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        return (T) unsafe().allocateInstance(type);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private enum TestPackStatus {
        SUCCESSFULLY_LOADED
    }

    private record ElytraBoostHandle(boolean shouldConsume) {
    }

    private static final class TestExpression<T> extends SimpleExpression<T> {

        private final String text;
        private final Class<? extends T> returnType;

        private TestExpression(String text, Class<? extends T> returnType) {
            this.text = text;
            this.returnType = returnType;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            @SuppressWarnings("unchecked")
            T[] empty = (T[]) java.lang.reflect.Array.newInstance(returnType, 0);
            return empty;
        }

        @Override
        public boolean isSingle() {
            return !text.endsWith("s");
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return text;
        }
    }
}
