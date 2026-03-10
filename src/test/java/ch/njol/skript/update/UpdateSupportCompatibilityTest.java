package ch.njol.skript.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UpdateSupportCompatibilityTest {

    @Test
    void releaseManifestLoadsAndCreatesChecker() throws Exception {
        String json = """
                {
                  "id": "2.0",
                  "date": "2026-03-10",
                  "flavor": "github",
                  "updateCheckerType": "ch.njol.skript.update.NoUpdateChecker",
                  "updateSource": "https://example.com/releases.json",
                  "downloadSource": null
                }
                """;

        ReleaseManifest manifest = ReleaseManifest.load(json);
        assertEquals("2.0", manifest.id);
        assertEquals("2026-03-10", manifest.date);
        assertEquals("github", manifest.flavor);
        assertEquals("https://example.com/releases.json", manifest.updateSource);
        assertNull(manifest.downloadSource);
        assertInstanceOf(NoUpdateChecker.class, manifest.createUpdateChecker());
    }

    @Test
    void githubCheckerReadsLatestReleaseFromFileUrl() throws Exception {
        Path releases = Files.createTempFile("skript-release", ".json");
        Files.writeString(releases, """
                [
                  {
                    "id": 21,
                    "tag_name": "2.1",
                    "created_at": "2026-03-11",
                    "body": "latest notes",
                    "assets": [
                      {
                        "browser_download_url": "https://example.com/skript-2.1.jar"
                      }
                    ]
                  },
                  {
                    "id": 20,
                    "tag_name": "2.0",
                    "created_at": "2026-03-10",
                    "body": "old notes",
                    "assets": [
                      {
                        "browser_download_url": "https://example.com/skript-2.0.jar"
                      }
                    ]
                  }
                ]
                """);

        ReleaseManifest manifest = new ReleaseManifest(
                "2.0",
                "2026-03-10",
                "github",
                GithubChecker.class,
                releases.toUri().toURL().toString(),
                null
        );
        ReleaseChannel channel = new ReleaseChannel(name -> true, "all");

        UpdateManifest update = new GithubChecker().check(manifest, channel).join();
        assertEquals("2.1", update.id);
        assertEquals("2026-03-11", update.date);
        assertEquals("latest notes", update.patchNotes);
        assertEquals(new URL("https://example.com/skript-2.1.jar"), update.downloadUrl);
    }

    @Test
    void noUpdateCheckerAndEnumsStayStable() {
        ReleaseChannel channel = new ReleaseChannel(name -> name.startsWith("stable"), "stable");
        assertEquals("stable", channel.getName());
        assertSame(Boolean.TRUE, channel.check("stable-1.0"));

        UpdateManifest updateManifest = new UpdateManifest("2.0", "2026-03-10", "notes", url("https://example.com/a.jar"));
        assertEquals("2.0", updateManifest.id);
        assertEquals("outdated", ReleaseStatus.OUTDATED.toString());
        assertSame(UpdaterState.CHECKING, UpdaterState.valueOf("CHECKING"));

        NoUpdateChecker checker = new NoUpdateChecker();
        ReleaseManifest manifest = new ReleaseManifest("2.0", "2026-03-10", "none", NoUpdateChecker.class, "https://example.com", null);
        assertNull(checker.check(manifest, channel).join());
    }

    private static URL url(String value) {
        try {
            return new URL(value);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
