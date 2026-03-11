package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprClientViewDistance;
import ch.njol.skript.expressions.ExprIP;
import ch.njol.skript.expressions.ExprLanguage;
import ch.njol.skript.expressions.ExprMaxPlayers;
import ch.njol.skript.expressions.ExprMOTD;
import ch.njol.skript.expressions.ExprMods;
import ch.njol.skript.expressions.ExprOnlinePlayersCount;
import ch.njol.skript.expressions.ExprOps;
import ch.njol.skript.expressions.ExprPing;
import ch.njol.skript.expressions.ExprPlayerProtocolVersion;
import ch.njol.skript.expressions.ExprProtocolVersion;
import ch.njol.skript.expressions.ExprVersion;
import ch.njol.skript.expressions.ExprViewDistance;
import ch.njol.skript.expressions.ExprWhitelist;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import com.mojang.authlib.GameProfile;
import ch.njol.skript.registrations.Classes;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
final class ExpressionLaneBBindingTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void laneBServerExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprClientViewDistance.class, parseExpression("client view distance of lane-b-player", Long.class));
        assertInstanceOf(ExprIP.class, parseExpression("ip address of lane-b-player", String.class));
        assertInstanceOf(ExprLanguage.class, parseExpression("current language of lane-b-player", String.class));
        assertInstanceOf(ExprMaxPlayers.class, parseExpression("max players count", Integer.class));
        assertInstanceOf(ExprMOTD.class, parseExpression("motd", String.class));
        assertInstanceOf(ExprMods.class, parseExpression("loaded mods", String.class));
        assertInstanceOf(ExprOnlinePlayersCount.class, parseExpression("online player count", Long.class));
        assertInstanceOf(ExprOps.class, parseExpression("all operators", GameProfile.class));
        assertInstanceOf(ExprOps.class, parseExpression("all non-operators", GameProfile.class));
        assertInstanceOf(ExprPing.class, parseExpression("ping of lane-b-player", Long.class));
        assertInstanceOf(ExprPlayerProtocolVersion.class, parseExpression("protocol version of lane-b-player", Integer.class));
        assertInstanceOf(ExprProtocolVersion.class, parseExpression("protocol version", Long.class));
        assertInstanceOf(ExprVersion.class, parseExpression("craftbukkit version", String.class));
        assertInstanceOf(ExprVersion.class, parseExpression("fabric version", String.class));
        assertInstanceOf(ExprViewDistance.class, parseExpression("view distance of lane-b-player", Integer.class));
        assertInstanceOf(ExprWhitelist.class, parseExpression("whitelist", GameProfile.class));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        if (Classes.getExactClassInfo(ServerPlayer.class) == null) {
            Classes.registerClassInfo(new ClassInfo<>(ServerPlayer.class, "player"));
        }
        if (Classes.getExactClassInfo(GameProfile.class) == null) {
            Classes.registerClassInfo(new ClassInfo<>(GameProfile.class, "offlineplayer"));
        }
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-b-player");
        syntaxRegistered = true;
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
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
    }
}
