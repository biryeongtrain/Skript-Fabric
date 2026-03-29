package ch.njol.skript.effects;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
final class EffectPresentationCompatibilityTest {

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

    private static void ensureSyntax() {
        if (!expressionsRegistered) {
            Skript.registerExpression(TestStringExpression.class, String.class, "lane-f-test-string");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-f-test-world");
            Skript.registerExpression(TestOfflinePlayerExpression.class, GameProfile.class, "lane-f-test-offlineplayer");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-f-test-location");
            Skript.registerExpression(TestTimespanExpression.class, Timespan.class, "lane-f-test-timespan");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        EffActionBar.register();
        EffBroadcast.register();
        EffKick.register();
        EffMessage.register();
        EffOp.register();
        EffPlaySound.register();
        EffResetTitle.register();
        EffSendResourcePack.register();
        EffSendTitle.register();
        EffStopSound.register();
    }

    @Test
    void actionBarEffectBindsMessageAndRecipients() throws Exception {
        EffActionBar effect = parseEffect("send action bar lane-f-test-string to lane-f-test-player", EffActionBar.class);
        assertEquals("lane-f-test-string", expression(effect, "message").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "recipients").toString(null, false));
    }

    @Test
    void broadcastEffectBindsMessageAndWorlds() throws Exception {
        EffBroadcast effect = parseEffect("broadcast lane-f-test-string to lane-f-test-world", EffBroadcast.class);
        assertEquals("lane-f-test-string", expression(effect, "messageExpr").toString(null, false));
        assertEquals("lane-f-test-world", expression(effect, "worlds").toString(null, false));
    }

    @Test
    void kickEffectBindsPlayersAndReason() throws Exception {
        EffKick effect = parseEffect("kick lane-f-test-player due to lane-f-test-string", EffKick.class);
        assertEquals("lane-f-test-player", expression(effect, "players").toString(null, false));
        assertEquals("lane-f-test-string", expression(effect, "reason").toString(null, false));
    }

    @Test
    void messageEffectBindsRecipientsAndSender() throws Exception {
        EffMessage effect = parseEffect("send lane-f-test-string to lane-f-test-player from lane-f-test-player", EffMessage.class);
        assertEquals("lane-f-test-string", expression(effect, "messages").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "recipients").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "sender").toString(null, false));
    }

    @Test
    void opEffectTracksDeopPrefix() throws Exception {
        EffOp effect = parseEffect("deop lane-f-test-offlineplayer", EffOp.class);
        assertEquals("lane-f-test-offlineplayer", expression(effect, "players").toString(null, false));
        assertFalse(readBoolean(effect, "op"));
    }

    @Test
    void playSoundEffectBindsSeedCategoryTargetsAndLocations() throws Exception {
        EffPlaySound effect = parseEffect(
                "play sound lane-f-test-string with seed lane-f-test-number in lane-f-test-string with volume lane-f-test-number with pitch lane-f-test-number to lane-f-test-player at lane-f-test-location",
                EffPlaySound.class
        );
        assertEquals("lane-f-test-string", expression(effect, "sounds").toString(null, false));
        assertEquals("lane-f-test-number", expression(effect, "seed").toString(null, false));
        assertEquals("lane-f-test-string", expression(effect, "category").toString(null, false));
        assertEquals("lane-f-test-number", expression(effect, "volume").toString(null, false));
        assertEquals("lane-f-test-number", expression(effect, "pitch").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "players").toString(null, false));
        assertEquals("lane-f-test-location", expression(effect, "locations").toString(null, false));
    }

    @Test
    void resetTitleEffectBindsRecipients() throws Exception {
        EffResetTitle effect = parseEffect("reset the title of lane-f-test-player", EffResetTitle.class);
        assertEquals("lane-f-test-player", expression(effect, "recipients").toString(null, false));
    }

    @Test
    void sendResourcePackEffectBindsUrlHashAndRecipients() throws Exception {
        EffSendResourcePack effect = parseEffect(
                "send the resource pack from lane-f-test-string with hash lane-f-test-string to lane-f-test-player",
                EffSendResourcePack.class
        );
        assertEquals("lane-f-test-string", expression(effect, "url").toString(null, false));
        assertEquals("lane-f-test-string", expression(effect, "hash").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "recipients").toString(null, false));
    }

    @Test
    void sendTitleEffectBindsTextsRecipientsAndTimes() throws Exception {
        EffSendTitle effect = parseEffect(
                "send title lane-f-test-string with subtitle lane-f-test-string to lane-f-test-player for lane-f-test-timespan with fadein lane-f-test-timespan and with fadeout lane-f-test-timespan",
                EffSendTitle.class
        );
        assertEquals("lane-f-test-string", expression(effect, "title").toString(null, false));
        assertEquals("lane-f-test-string", expression(effect, "subtitle").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "recipients").toString(null, false));
        assertEquals("lane-f-test-timespan", expression(effect, "stay").toString(null, false));
        assertEquals("lane-f-test-timespan", expression(effect, "fadeIn").toString(null, false));
        assertEquals("lane-f-test-timespan", expression(effect, "fadeOut").toString(null, false));
    }

    @Test
    void stopSoundEffectTracksSoundMode() throws Exception {
        EffStopSound effect = parseEffect("stop sound lane-f-test-string in lane-f-test-string for lane-f-test-player", EffStopSound.class);
        assertFalse(readBoolean(effect, "allSounds"));
        assertEquals("lane-f-test-string", expression(effect, "sounds").toString(null, false));
        assertEquals("lane-f-test-string", expression(effect, "category").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "players").toString(null, false));
    }

    private <T extends Effect> T parseEffect(String input, Class<T> effectClass) {
        Effect effect = Effect.parse(input, null);
        assertNotNull(effect);
        assertInstanceOf(effectClass, effect);
        return effectClass.cast(effect);
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readObject(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Object readObject(Object owner, String fieldName) throws Exception {
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

    public static final class TestStringExpression extends SimpleExpression<String> {

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
    }

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {

        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return new ServerPlayer[0];
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
    }

    public static final class TestWorldExpression extends SimpleExpression<ServerLevel> {

        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) {
            return new ServerLevel[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerLevel> getReturnType() {
            return ServerLevel.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }
    }

    public static final class TestOfflinePlayerExpression extends SimpleExpression<GameProfile> {

        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return new GameProfile[]{new GameProfile(java.util.UUID.randomUUID(), "LaneF")};
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
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }
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
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {

        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[0];
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
    }

    public static final class TestTimespanExpression extends SimpleExpression<Timespan> {

        @Override
        protected Timespan @Nullable [] get(SkriptEvent event) {
            return new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, 20)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Timespan> getReturnType() {
            return Timespan.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }
    }
}
