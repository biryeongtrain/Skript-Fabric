package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260312GBindingCompatibilityTest {

    private static boolean syntaxRegistered;
    private static final Config TEST_CONFIG = new Config("main", "main.sk", null);

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        ExprConfig.setMainConfig(TEST_CONFIG);
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void parserBindsCycle20260312gRecoveredSyntax() {
        assertExpressionRegistered(ExprQuitReason.class);
        assertExpressionRegistered(ExprSourceBlock.class);
        assertExpressionRegistered(ExprTamer.class);
        assertExpressionRegistered(ExprHostname.class);
        assertExpressionRegistered(ExprTPS.class);
        assertExpressionRegistered(ExprPermissions.class);
        assertExpressionRegistered(ExprConfig.class);
        assertExpressionRegistered(ExprNode.class);
        assertExpressionRegistered(ExprScripts.class);

        ParserInstance parser = ParserInstance.get();

        parser.setCurrentEvent("quit", TestQuitHandle.class);
        assertInstanceOf(ExprQuitReason.class, parseExpression("quit reason", String.class));

        parser.setCurrentEvent("spread", TestSourceBlockHandle.class);
        assertInstanceOf(ExprSourceBlock.class, parseExpression("source block", FabricBlock.class));

        parser.setCurrentEvent("tame", TestTameHandle.class);
        assertInstanceOf(ExprTamer.class, parseExpression("tamer", ServerPlayer.class));

        parser.setCurrentEvent("connect", ClientIntentionPacket.class);
        assertInstanceOf(ExprHostname.class, parseExpression("hostname", String.class));
        parser.deleteCurrentEvent();

        assertInstanceOf(ExprTPS.class, parseExpression("tps", Number.class));
        assertInstanceOf(ExprPermissions.class, parseExpression("permissions of lane-g-player", String.class));
        assertInstanceOf(ExprConfig.class, parseExpression("the skript config", Config.class));
        assertInstanceOf(ExprNode.class, parseExpression("the node \"language\" in lane-g-config", Node.class));
        assertInstanceOf(ExprScripts.class, parseExpression("all scripts", Script.class));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(Config.class, "config");
        registerClassInfo(Node.class, "node");
        registerClassInfo(Script.class, "script");

        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-g-player");
        Skript.registerExpression(TestConfigExpression.class, Config.class, "lane-g-config");
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

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
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
    }

    public static final class TestConfigExpression extends SimpleExpression<Config> {
        @Override
        protected Config @Nullable [] get(SkriptEvent event) {
            return new Config[]{TEST_CONFIG};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Config> getReturnType() {
            return Config.class;
        }
    }

    private record TestQuitHandle(String reason) {
    }

    private record TestSourceBlockHandle(FabricBlock source) {
    }

    private record TestTameHandle(ServerPlayer owner) {
    }
}
