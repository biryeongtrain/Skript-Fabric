package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class FileSystemSkriptScriptServiceTest {

    private static final List<String> MARKERS = new ArrayList<>();

    @TempDir
    Path tempDir;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        Skript.registerEffect(RecordScriptMarkerEffect.class, "record script marker %string%");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        MARKERS.clear();
    }

    @Test
    void loadsAllEnabledScriptsRecursivelyUnderConfigSkript() throws IOException {
        Path root = tempDir.resolve("config").resolve("skript");
        writeScript(root.resolve("admin.sk"), "admin");
        writeScript(root.resolve("quests").resolve("daily.sk"), "daily");
        writeScript(root.resolve("quests").resolve("-disabled.sk"), "disabled");

        FileSystemSkriptScriptService service = new FileSystemSkriptScriptService(root, SkriptRuntime.instance());
        SkriptScriptOperationResult result = service.loadAll();

        assertEquals(List.of("admin", "quests/daily"), result.scripts());
        assertEquals(List.of("admin", "daily"), MARKERS);
        assertEquals(List.of("admin", "quests/daily"), service.listLoadedScripts());
        assertTrue(Files.exists(root.resolve("quests").resolve("-disabled.sk")));
    }

    @Test
    void discoversScriptsAndSuggestionsAcrossFolders() throws IOException {
        Path root = tempDir.resolve("config").resolve("skript");
        writeScript(root.resolve("admin.sk"), "admin");
        writeScript(root.resolve("quests").resolve("daily.sk"), "daily");
        writeScript(root.resolve("quests").resolve("-disabled.sk"), "disabled");

        FileSystemSkriptScriptService service = new FileSystemSkriptScriptService(root, SkriptRuntime.instance());

        assertIterableEquals(
                List.of("admin.sk", "quests/-disabled.sk", "quests/daily.sk"),
                service.discoverScripts()
        );
        assertTrue(service.suggestedTargets().contains("quests"));
        assertTrue(service.suggestedTargets().contains("quests/disabled"));
    }

    @Test
    void enableAndDisableTargetRenamesFilesAndUpdatesRuntime() throws IOException {
        Path root = tempDir.resolve("config").resolve("skript");
        writeScript(root.resolve("-archived.sk"), "archived");

        FileSystemSkriptScriptService service = new FileSystemSkriptScriptService(root, SkriptRuntime.instance());

        SkriptScriptOperationResult enabled = service.enableTarget("archived");
        assertEquals(List.of("archived"), enabled.scripts());
        assertTrue(Files.exists(root.resolve("archived.sk")));
        assertFalse(Files.exists(root.resolve("-archived.sk")));
        assertEquals(List.of("archived"), service.listLoadedScripts());

        SkriptScriptOperationResult disabled = service.disableTarget("archived");
        assertEquals(List.of("archived"), disabled.scripts());
        assertTrue(Files.exists(root.resolve("-archived.sk")));
        assertFalse(Files.exists(root.resolve("archived.sk")));
        assertEquals(List.of(), service.listLoadedScripts());
    }

    @Test
    void reloadTargetReloadsOnlyMatchingSubtree() throws IOException {
        Path root = tempDir.resolve("config").resolve("skript");
        Path admin = root.resolve("admin.sk");
        Path daily = root.resolve("quests").resolve("daily.sk");
        writeScript(admin, "admin-v1");
        writeScript(daily, "daily-v1");

        FileSystemSkriptScriptService service = new FileSystemSkriptScriptService(root, SkriptRuntime.instance());
        service.loadAll();
        MARKERS.clear();

        writeScript(daily, "daily-v2");
        SkriptScriptOperationResult result = service.reloadTarget("quests");

        assertEquals(List.of("quests/daily"), result.scripts());
        assertEquals(List.of("daily-v2"), MARKERS);
        assertEquals(List.of("admin", "quests/daily"), service.listLoadedScripts());
    }

    @Test
    void unloadAllClearsLoadedScripts() throws IOException {
        Path root = tempDir.resolve("config").resolve("skript");
        writeScript(root.resolve("admin.sk"), "admin");
        writeScript(root.resolve("quests").resolve("daily.sk"), "daily");

        FileSystemSkriptScriptService service = new FileSystemSkriptScriptService(root, SkriptRuntime.instance());
        service.loadAll();
        MARKERS.clear();

        SkriptScriptOperationResult result = service.unloadAll();

        assertEquals(2, result.affectedFiles());
        assertEquals(List.of("admin", "quests/daily"), result.scripts());
        assertEquals(List.of(), service.listLoadedScripts());
    }

    private static void writeScript(Path path, String marker) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(
                path,
                """
                on load:
                    record script marker "%s"
                """.formatted(marker)
        );
    }

    public static final class RecordScriptMarkerEffect extends Effect {

        private Expression<String> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            value = (Expression<String>) expressions[0];
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            @Nullable String marker = value.getSingle(event);
            if (marker != null) {
                MARKERS.add(marker);
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record script marker " + value.toString(event, debug);
        }
    }
}
