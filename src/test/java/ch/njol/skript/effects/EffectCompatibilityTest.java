package ch.njol.skript.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser.ParseResult;
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
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
final class EffectCompatibilityTest {

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
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        Skript.registerEffect(EffFeed.class, "feed [the] %players% [by %-number% [beef[s]]]");
        Skript.registerEffect(EffKill.class, "kill %entities%");
        Skript.registerEffect(
                EffSilence.class,
                "silence %entities%",
                "unsilence %entities%",
                "make %entities% silent",
                "make %entities% not silent"
        );
        Skript.registerEffect(
                EffInvisible.class,
                "make %livingentities% not visible",
                "make %livingentities% not invisible",
                "make %livingentities% invisible",
                "make %livingentities% visible"
        );
        Skript.registerEffect(
                EffInvulnerability.class,
                "make %entities% (invulnerable|invincible)",
                "make %entities% (not (invulnerable|invincible)|vulnerable|vincible)"
        );
        Skript.registerEffect(
                EffSprinting.class,
                "make %players% (start sprinting|sprint)",
                "force %players% to (start sprinting|sprint)",
                "make %players% (stop sprinting|not sprint)",
                "force %players% to (stop sprinting|not sprint)"
        );
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void feedEffectParsesOptionalBeefAmount() throws Exception {
        EffFeed effect = parseEffect("feed lane-f-test-player by lane-f-test-number", EffFeed.class);

        assertEquals("lane-f-test-player", expression(effect, "players").toString(null, false));
        assertEquals("lane-f-test-number", expression(effect, "beefs").toString(null, false));
    }

    @Test
    void killEffectParsesEntityExpression() throws Exception {
        EffKill effect = parseEffect("kill lane-f-test-entity", EffKill.class);

        assertEquals("lane-f-test-entity", expression(effect, "entities").toString(null, false));
        assertEquals("kill lane-f-test-entity", effect.toString(null, false));
    }

    @Test
    void silenceEffectTracksMatchedPatternMode() throws Exception {
        EffSilence silence = parseEffect("silence lane-f-test-entity", EffSilence.class);
        EffSilence unsilence = parseEffect("make lane-f-test-entity not silent", EffSilence.class);

        assertTrue(readBoolean(silence, "silence"));
        assertFalse(readBoolean(unsilence, "silence"));
    }

    @Test
    void invisibilityEffectTracksVisibleAndInvisibleModes() throws Exception {
        EffInvisible invisible = parseEffect("make lane-f-test-livingentity invisible", EffInvisible.class);
        EffInvisible visible = parseEffect("make lane-f-test-livingentity visible", EffInvisible.class);

        assertTrue(readBoolean(invisible, "invisible"));
        assertFalse(readBoolean(visible, "invisible"));
    }

    @Test
    void invulnerabilityEffectTracksNegatedPattern() throws Exception {
        EffInvulnerability yes = parseEffect("make lane-f-test-entity invulnerable", EffInvulnerability.class);
        EffInvulnerability no = parseEffect("make lane-f-test-entity vulnerable", EffInvulnerability.class);

        assertTrue(readBoolean(yes, "invulnerable"));
        assertFalse(readBoolean(no, "invulnerable"));
    }

    @Test
    void sprintingEffectTracksStartAndStopPatterns() throws Exception {
        EffSprinting start = parseEffect("make lane-f-test-player sprint", EffSprinting.class);
        EffSprinting stop = parseEffect("force lane-f-test-player to stop sprinting", EffSprinting.class);

        assertTrue(readBoolean(start, "sprint"));
        assertFalse(readBoolean(stop, "sprint"));
    }

    private <T> T parseEffect(String input, Class<T> type) {
        Statement parsed = Statement.parse(input, "failed");
        assertNotNull(parsed);
        assertInstanceOf(type, parsed);
        return type.cast(parsed);
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Object readField(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(owner);
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
}
