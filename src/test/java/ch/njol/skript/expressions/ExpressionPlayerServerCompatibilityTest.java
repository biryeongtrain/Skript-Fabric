package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import sun.misc.Unsafe;

final class ExpressionPlayerServerCompatibilityTest {

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

    @Test
    void playerServerExpressionsParseWithRegisteredSources() {
        assertInstanceOf(ExprAllCommands.class, parseExpression("all commands", String.class));
        assertInstanceOf(ExprAllCommands.class, parseExpression("all script commands", String.class));
        assertInstanceOf(ExprClientViewDistance.class, parseExpression("client view distance of lane-e-m3-player", Long.class));
        assertInstanceOf(ExprIP.class, parseExpression("ip of lane-e-m3-player", String.class));
        assertInstanceOf(ExprLanguage.class, parseExpression("current language of lane-e-m3-player", String.class));
        assertInstanceOf(ExprLastLoginTime.class, parseExpression("last login of lane-e-m3-offlineplayer", ch.njol.skript.util.Date.class));
        assertInstanceOf(ExprLastLoginTime.class, parseExpression("first login of lane-e-m3-offlineplayer", ch.njol.skript.util.Date.class));
        assertInstanceOf(ExprMaxPlayers.class, parseExpression("max players count", Integer.class));
        assertInstanceOf(ExprPing.class, parseExpression("ping of lane-e-m3-player", Long.class));
        assertInstanceOf(ExprPlayerProtocolVersion.class, parseExpression("protocol version of lane-e-m3-player", Integer.class));
        assertInstanceOf(ExprProtocolVersion.class, parseExpression("protocol version", Long.class));
    }

    @Test
    void resourcePackResponseExpressionParsesAndReadsCompatHandle() throws Exception {
        ParserInstance.get().setCurrentEvent("resource pack response", ch.njol.skript.events.FabricEventCompatHandles.ResourcePackResponse.class);

        assertInstanceOf(ExprLastResourcePackResponse.class, parseExpression("last resource pack response of lane-e-m3-player", String.class));

        ServerPlayer player = allocate(ServerPlayer.class);
        ExprLastResourcePackResponse response = new ExprLastResourcePackResponse();
        assertTrue(response.init(
                new Expression[]{new TestPlayerExpression()},
                0,
                ch.njol.util.Kleenean.FALSE,
                parseResult("last resource pack response of lane-e-m3-player")
        ));
        assertEquals(
                "accepted",
                response.getSingle(new SkriptEvent(
                        new ch.njol.skript.events.FabricEventCompatHandles.ResourcePackResponse("accepted"),
                        null,
                        null,
                        player
                ))
        );
    }

    @Test
    void commandExpressionsParseAndReadCompatHandle() throws Exception {
        Class<?> commandClass = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        ParserInstance.get().setCurrentEvent("command", commandClass);

        assertInstanceOf(ExprCommand.class, parseExpression("full command", String.class));
        assertInstanceOf(ExprCommand.class, parseExpression("command", String.class));
        assertInstanceOf(ExprCommandSender.class, parseExpression("command sender", ServerPlayer.class));

        Object handle = commandHandle("/say hello world");
        ExprCommand full = new ExprCommand();
        assertTrue(full.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("full command")));
        assertEquals("say hello world", full.getSingle(new SkriptEvent(handle, null, null, null)));

        ExprCommand label = new ExprCommand();
        assertTrue(label.init(new Expression[0], 1, ch.njol.util.Kleenean.FALSE, parseResult("command")));
        assertEquals("say", label.getSingle(new SkriptEvent(handle, null, null, null)));

        ServerPlayer player = allocate(ServerPlayer.class);
        ExprCommandSender sender = new ExprCommandSender();
        assertTrue(sender.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("command sender")));
        assertSame(player, sender.getSingle(new SkriptEvent(handle, null, null, player)));
    }

    @Test
    void lastLoginHelperReadsPlayerDataTimestamps() throws Exception {
        Path playerData = Files.createTempDirectory("lane-e-m3-playerdata");
        UUID uuid = UUID.randomUUID();
        Path file = playerData.resolve(uuid + ".dat");
        Files.writeString(file, "lane-e");
        GameProfile profile = new GameProfile(uuid, "lane-e-m3");

        FileTime first = ExprLastLoginTime.resolveLoginTime(playerData, profile, true);
        FileTime expectedLast = FileTime.fromMillis(System.currentTimeMillis() + 5_000L);
        Files.setLastModifiedTime(file, expectedLast);
        FileTime last = ExprLastLoginTime.resolveLoginTime(playerData, profile, false);

        assertNotNull(first);
        assertNotNull(last);
        assertTrue(first.toMillis() <= last.toMillis());
        assertEquals(expectedLast.toMillis(), last.toMillis());
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(GameProfile.class, "offlineplayer");
        registerClassInfo(ch.njol.skript.util.Date.class, "date");
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-e-m3-player");
        Skript.registerExpression(TestOfflinePlayerExpression.class, GameProfile.class, "lane-e-m3-offlineplayer");
        new ExprAllCommands();
        new ExprClientViewDistance();
        new ExprCommand();
        new ExprCommandSender();
        new ExprIP();
        new ExprLanguage();
        new ExprLastLoginTime();
        new ExprLastResourcePackResponse();
        new ExprMaxPlayers();
        new ExprPing();
        new ExprPlayerProtocolVersion();
        new ExprProtocolVersion();
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

    private static Object commandHandle(String command) throws Exception {
        Class<?> type = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        Constructor<?> constructor = type.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(command);
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws ReflectiveOperationException {
        return (T) unsafe().allocateInstance(type);
    }

    private static Unsafe unsafe() throws ReflectiveOperationException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {
        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return event.player() == null ? new ServerPlayer[0] : new ServerPlayer[]{event.player()};
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
            return "lane-e-m3-player";
        }
    }

    public static final class TestOfflinePlayerExpression extends SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return new GameProfile[0];
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
            return "lane-e-m3-offlineplayer";
        }
    }
}
