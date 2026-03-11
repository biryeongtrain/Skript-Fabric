package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionLaneBCompatibilityTest {

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

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void laneBServerStateExpressionsParseWithRegisteredSources() {
        assertInstanceOf(ExprMOTD.class, parseExpression("motd", String.class));
        assertInstanceOf(ExprOnlinePlayersCount.class, parseExpression("online player count", Long.class));
        assertInstanceOf(ExprOps.class, parseExpression("all operators", GameProfile.class));
        assertInstanceOf(ExprOps.class, parseExpression("all non-operators", GameProfile.class));
        assertInstanceOf(ExprVersion.class, parseExpression("craftbukkit version", String.class));
        assertInstanceOf(ExprVersion.class, parseExpression("minecraft version", String.class));
        assertInstanceOf(ExprVersion.class, parseExpression("skript version", String.class));
        assertInstanceOf(ExprVersion.class, parseExpression("fabric version", String.class));
        assertInstanceOf(ExprViewDistance.class, parseExpression("view distance of lane-b-unit-player", Integer.class));
        assertInstanceOf(ExprWhitelist.class, parseExpression("whitelist", GameProfile.class));
    }

    @Test
    void unsupportedShownServerListRoutesFailFast() {
        ExprMOTD motd = new ExprMOTD();
        ParseResult shownMotd = parseResult("shown motd");
        shownMotd.mark = 2;
        assertFalse(motd.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, shownMotd));

        ExprOnlinePlayersCount playersCount = new ExprOnlinePlayersCount();
        ParseResult shownCount = parseResult("shown online player count");
        shownCount.mark = 2;
        assertFalse(playersCount.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, shownCount));
    }

    @Test
    void versionExpressionReturnsFabricMinecraftAndSkriptVersions() {
        ExprVersion fabric = new ExprVersion();
        assertTrue(fabric.init(new Expression[0], 1, ch.njol.util.Kleenean.FALSE, parseResult("fabric version")));
        assertEquals(
                FabricLoader.getInstance().getModContainer("fabricloader")
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown"),
                fabric.getSingle(SkriptEvent.EMPTY)
        );

        ExprVersion minecraft = new ExprVersion();
        ParseResult minecraftResult = parseResult("minecraft version");
        minecraftResult.mark = 1;
        assertTrue(minecraft.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, minecraftResult));
        assertEquals(Skript.getMinecraftVersion().toString(), minecraft.getSingle(SkriptEvent.EMPTY));

        ExprVersion skript = new ExprVersion();
        ParseResult skriptResult = parseResult("skript version");
        skriptResult.mark = 2;
        assertTrue(skript.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, skriptResult));
        assertEquals(
                FabricLoader.getInstance()
                        .getModContainer("skript-fabric-port")
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown"),
                skript.getSingle(SkriptEvent.EMPTY)
        );
    }

    @Test
    void changeCapableLaneBExpressionsAdvertiseExpectedModes() {
        ExprOps ops = new ExprOps();
        assertNotNull(ops.acceptChange(ChangeMode.ADD));
        assertNotNull(ops.acceptChange(ChangeMode.SET));
        assertNotNull(ops.acceptChange(ChangeMode.REMOVE));

        ExprWhitelist whitelist = new ExprWhitelist();
        assertNotNull(whitelist.acceptChange(ChangeMode.ADD));
        assertNotNull(whitelist.acceptChange(ChangeMode.SET));
        assertNotNull(whitelist.acceptChange(ChangeMode.DELETE));

        ExprViewDistance viewDistance = new ExprViewDistance();
        assertNotNull(viewDistance.acceptChange(ChangeMode.SET));
        assertNotNull(viewDistance.acceptChange(ChangeMode.ADD));
        assertNotNull(viewDistance.acceptChange(ChangeMode.RESET));
    }

    @Test
    void whitelistRuntimeHelperExtractsProfilesFromWhitelistEntries() throws Exception {
        UserWhiteList whitelist = new UserWhiteList(tempDir.resolve("whitelist.json").toFile());
        GameProfile expected = new GameProfile(
                UUID.nameUUIDFromBytes("OfflinePlayer:LaneBListed".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                "LaneBListed"
        );
        whitelist.add(new UserWhiteListEntry(expected));

        Method helper = ExpressionRuntimeSupport.class.getDeclaredMethod("configEntriesAsProfiles", Object.class);
        helper.setAccessible(true);
        GameProfile[] profiles = (GameProfile[]) helper.invoke(null, whitelist);

        assertEquals(1, profiles.length);
        assertEquals(expected.getId(), profiles[0].getId());
        assertEquals(expected.getName(), profiles[0].getName());
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(GameProfile.class, "offlineplayer");
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-b-unit-player");
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

    private static ParseResult parseResult(String expr) {
        ParseResult result = new ParseResult();
        result.expr = expr;
        return result;
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
            return "lane-b-unit-player";
        }
    }
}
