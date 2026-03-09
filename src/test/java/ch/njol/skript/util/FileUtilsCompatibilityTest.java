package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsCompatibilityTest {

    @TempDir
    File tempDir;

    @Test
    void saveAndBackupPreserveFileContents() throws Exception {
        File original = new File(tempDir, "vars.csv");
        FileUtils.save(new ByteArrayInputStream("alpha".getBytes(StandardCharsets.UTF_8)), original);

        File backup = FileUtils.backup(original);

        assertEquals("alpha", Files.readString(original.toPath()));
        assertEquals("alpha", Files.readString(backup.toPath()));
        assertTrue(backup.getParentFile().getName().startsWith("backups"));
    }

    @Test
    void renameAllRenamesLeafFilesRecursively() throws Exception {
        File nested = new File(tempDir, "nested");
        assertTrue(nested.mkdirs());
        File original = new File(nested, "old.txt");
        Files.writeString(original.toPath(), "x");

        Collection<File> changed = FileUtils.renameAll(tempDir, name -> name.startsWith("old") ? "new.txt" : null);

        assertEquals(1, changed.size());
        assertFalse(original.exists());
        assertTrue(new File(nested, "new.txt").exists());
    }
}
