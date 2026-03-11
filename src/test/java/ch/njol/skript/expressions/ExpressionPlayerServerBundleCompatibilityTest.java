package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionPlayerServerBundleCompatibilityTest {

    private static boolean syntaxRegistered;
    private static List<SyntaxInfo<?>> originalExpressions = List.of();

    @BeforeAll
    static void bootstrapSyntax() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        originalExpressions = new ArrayList<>();
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            originalExpressions.add(info);
        }
        ensureSyntax();
    }

    @AfterAll
    static void restoreSyntax() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
        for (SyntaxInfo<?> info : originalExpressions) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EXPRESSION, info);
        }
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    private static void ensureSyntax() {
        if (!syntaxRegistered) {
            registerClassInfo(ServerPlayer.class, "player");
            registerClassInfo(MinecraftServer.class, "server");
            registerClassInfo(GameProfile.class, "offlineplayer");
            registerClassInfo(ch.njol.skript.util.Date.class, "date");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-m3-test-player");
            Skript.registerExpression(TestOfflinePlayerExpression.class, GameProfile.class, "lane-m3-test-offlineplayer");
            syntaxRegistered = true;
        }
        new ExprAllCommands();
        new ExprCommand();
        new ExprCommandSender();
        new ExprClientViewDistance();
        new ExprIP();
        new ExprLanguage();
        new ExprLastLoginTime();
        new ExprLastResourcePackResponse();
        new ExprMaxPlayers();
        new ExprPing();
        new ExprPlayerProtocolVersion();
        new ExprProtocolVersion();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void playerAndServerExpressionsParseAgainstFabricTypes() {
        assertInstanceOf(ExprAllCommands.class, parseExpression("all commands", String.class));
        assertInstanceOf(ExprAllCommands.class, parseExpression("all script commands", String.class));
        assertInstanceOf(ExprClientViewDistance.class, parseExpression("client view distance of lane-m3-test-player", Long.class));
        assertInstanceOf(ExprIP.class, parseExpression("ip address of lane-m3-test-player", String.class));
        assertInstanceOf(ExprLanguage.class, parseExpression("language of lane-m3-test-player", String.class));
        assertInstanceOf(ExprLastLoginTime.class, parseExpression("last login of lane-m3-test-offlineplayer", ch.njol.skript.util.Date.class));
        assertInstanceOf(ExprLastLoginTime.class, parseExpression("first login of lane-m3-test-offlineplayer", ch.njol.skript.util.Date.class));
        assertInstanceOf(ExprPing.class, parseExpression("ping of lane-m3-test-player", Long.class));
        assertInstanceOf(ExprPlayerProtocolVersion.class, parseExpression("protocol version of lane-m3-test-player", Integer.class));
        assertInstanceOf(ExprMaxPlayers.class, parseExpression("max players count", Integer.class));
        assertInstanceOf(ExprProtocolVersion.class, parseExpression("protocol version", Long.class));
    }

    @Test
    void commandExpressionUsesFabricCommandHandle() {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            Class<?> commandEventClass = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
            parser.setCurrentEvent("command", commandEventClass);

            Expression<?> full = parseExpression("full command", String.class);
            assertInstanceOf(ExprCommand.class, full);
            assertEquals("say hello there", full.getSingle(new SkriptEvent(newCommandHandle("/say hello there"), null, null, null)));

            Expression<?> label = parseExpression("command", String.class);
            assertInstanceOf(ExprCommand.class, label);
            assertEquals("say", label.getSingle(new SkriptEvent(newCommandHandle("/say hello there"), null, null, null)));

            assertInstanceOf(ExprCommandSender.class, parseExpression("command sender", ServerPlayer.class));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    @Test
    void resourcePackResponseExpressionParsesInCompatEvent() {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("resource pack response", ch.njol.skript.events.FabricEventCompatHandles.ResourcePackResponse.class);
            assertInstanceOf(
                    ExprLastResourcePackResponse.class,
                    parseExpression("last resource pack response of lane-m3-test-player", String.class)
            );
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private void restoreEventContext(ParserInstance parser, @Nullable String previousEventName, Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }
    }

    private Object newCommandHandle(String command) throws ReflectiveOperationException {
        Class<?> type = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        Constructor<?> constructor = type.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(command);
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
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-m3-test-player";
        }
    }

    public static final class TestOfflinePlayerExpression extends SimpleExpression<GameProfile> {

        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return new GameProfile[]{new GameProfile(UUID.randomUUID(), "LaneM3")};
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
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-m3-test-offlineplayer";
        }
    }
}
