package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class FirstJoinRuntimeUnitTest {

    @Test
    void isFirstJoinChecksPlayerdataPresence() throws Exception {
        Path playerData = Files.createTempDirectory("first-join-playerdata");
        GameProfile profile = new GameProfile(UUID.randomUUID(), "first-join");

        assertTrue(SkriptFabricEventBridge.isFirstJoin(playerData, profile));

        Files.createFile(playerData.resolve(profile.id() + ".dat"));
        assertFalse(SkriptFabricEventBridge.isFirstJoin(playerData, profile));
    }
}
