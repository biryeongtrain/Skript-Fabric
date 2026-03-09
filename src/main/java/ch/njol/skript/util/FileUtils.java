package ch.njol.skript.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import org.skriptlang.skript.lang.converter.Converter;

public abstract class FileUtils {

    private static final SimpleDateFormat BACKUP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private FileUtils() {
    }

    public static String getBackupSuffix() {
        synchronized (BACKUP_FORMAT) {
            return BACKUP_FORMAT.format(System.currentTimeMillis());
        }
    }

    public static void backupPurge(File varFile, int toKeep) throws IOException {
        if (toKeep < 0) {
            throw new IllegalArgumentException("Called with invalid input, 'toKeep' can not be less than 0");
        }
        File backupDir = new File(varFile.getParentFile(), "backups" + File.separator);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            throw new IOException("Backup directory not found");
        }
        File[] listedFiles = backupDir.listFiles();
        ArrayList<File> files = listedFiles == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(listedFiles));
        if (files.size() <= toKeep) {
            return;
        }
        if (toKeep > 0) {
            files.sort(Comparator.comparingLong(File::lastModified));
        }
        int numberToRemove = files.size() - toKeep;
        for (int i = 0; i < numberToRemove; i++) {
            files.get(i).delete();
        }
    }

    public static File backup(File file) throws IOException {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        String extension = dot == -1 ? null : name.substring(dot + 1);
        if (dot != -1) {
            name = name.substring(0, dot);
        }
        File backupFolder = new File(file.getParentFile(), "backups" + File.separator);
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            throw new IOException("Cannot create backups folder");
        }
        File backup = new File(backupFolder, name + "_" + getBackupSuffix() + (extension == null ? "" : "." + extension));
        if (backup.exists()) {
            throw new IOException("Backup file " + backup.getName() + " does already exist");
        }
        copy(file, backup);
        return backup;
    }

    public static File move(File from, File to, boolean replace) throws IOException {
        if (!replace && to.exists()) {
            throw new IOException("Can't rename " + from.getName() + " to " + to.getName()
                    + ": The target file already exists");
        }
        if (replace) {
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } else {
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
        }
        return to;
    }

    public static void copy(File from, File to) throws IOException {
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }

    public static Collection<File> renameAll(File directory, Converter<String, String> renamer) throws IOException {
        Collection<File> changed = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return changed;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                changed.addAll(renameAll(file, renamer));
                continue;
            }
            String name = file.getName();
            String newName = renamer.convert(name);
            if (newName == null) {
                continue;
            }
            File newFile = new File(file.getParent(), newName);
            move(file, newFile, false);
            changed.add(newFile);
        }
        return changed;
    }

    public static void save(InputStream in, File file) throws IOException {
        file.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        }
    }
}
