package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;

final class ExpressionCycle20260312ESyntax3CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void parserBindsOfflinePlayers() {
        assertInstanceOf(ExprOfflinePlayers.class, parseExpression("offline players", GameProfile.class));
    }

    @Test
    void offlinePlayersResolveProfilesFromPlayerData(@TempDir Path tempDir) throws Exception {
        UUID onlineUuid = UUID.randomUUID();
        UUID offlineUuid = UUID.randomUUID();
        Files.createFile(tempDir.resolve(offlineUuid + ".dat"));
        Files.createFile(tempDir.resolve("not-a-uuid.dat"));

        GameProfile online = new GameProfile(onlineUuid, "OnlinePlayer");
        GameProfile offline = new GameProfile(offlineUuid, "OfflinePlayer");

        GameProfile[] resolved = ExprOfflinePlayers.resolveOfflinePlayers(
                tempDir,
                List.of(online),
                uuid -> uuid.equals(offlineUuid) ? offline : null
        );

        assertEquals(2, resolved.length);
        assertEquals(onlineUuid, resolved[0].getId());
        assertEquals(offlineUuid, resolved[1].getId());
        assertEquals("OfflinePlayer", resolved[1].getName());
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(GameProfile.class, "offlineplayer");
        new ExprOfflinePlayers();
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
}
