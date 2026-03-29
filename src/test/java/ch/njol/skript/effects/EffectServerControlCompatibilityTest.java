package ch.njol.skript.effects;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import java.lang.reflect.Field;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class EffectServerControlCompatibilityTest {

    @BeforeAll
    static void bootstrapSyntax() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        try {
            Class.forName(EffEnforceWhitelist.class.getName());
            Class.forName(EffRespawn.class.getName());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
        if (Classes.getExactClassInfo(ServerPlayer.class) == null) {
            Classes.registerClassInfo(new ClassInfo<>(ServerPlayer.class, "player"));
        }
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
    }

    @Test
    void enforceWhitelistEffectParsesMode() throws Exception {
        EffEnforceWhitelist effect = parseEffect("unenforce the whitelist", EffEnforceWhitelist.class);

        assertEquals("unenforce the whitelist", effect.toString(null, false));
        assertEquals(false, readBoolean(effect, "enforce"));
    }

    @Test
    void respawnEffectParsesPlayerExpression() throws Exception {
        EffRespawn effect = parseEffect("force lane-f-test-player to respawn", EffRespawn.class);

        assertEquals("lane-f-test-player", expression(effect, "players").toString(null, false));
        assertEquals("force lane-f-test-player to respawn", effect.toString(null, false));
    }

    @Test
    void respawnEffectRejectsRespawnEventContext() {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("respawn", FabricEffectEventHandles.PlayerRespawn.class);
            assertNull(Effect.parse("force lane-f-test-player to respawn", "failed", new SectionNode("force lane-f-test-player to respawn"), null));
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }

    private <T> T parseEffect(String input, Class<T> type) {
        Statement parsed = Statement.parse(input, "failed");
        assertNotNull(parsed);
        assertInstanceOf(type, parsed);
        return type.cast(parsed);
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(owner);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {

        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return new ServerPlayer[0];
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-player";
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, ch.njol.skript.lang.SkriptParser.ParseResult parseResult) {
            return true;
        }
    }
}
