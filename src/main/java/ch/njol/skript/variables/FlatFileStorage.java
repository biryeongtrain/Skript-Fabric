package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class FlatFileStorage extends VariablesStorage {

    public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

    private static final long SAVE_TASK_DELAY = 5 * 60 * 20;
    private static final long SAVE_TASK_PERIOD = 5 * 60 * 20;
    private static int REQUIRED_CHANGES_FOR_RESAVE = 1000;

    private final Object changesWriterMonitor = new Object();
    @Nullable
    private PrintWriter changesWriter;
    private volatile boolean loaded = false;
    private final AtomicInteger changes = new AtomicInteger(0);
    @Nullable
    private Task saveTask;
    private boolean loadError = false;

    FlatFileStorage(String type) {
        super(type);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean load_i(SectionNode sectionNode) {
        SkriptLogger.setNode(null);

        if (file == null) {
            return false;
        }

        IOException ioException = null;
        int unsuccessfulVariableCount = 0;
        StringBuilder invalid = new StringBuilder();

        Version csvSkriptVersion = new Version(2, 1);
        Version v2_0_beta3 = new Version(2, 0, "beta 3");
        boolean update2_0_beta3 = false;
        Version v2_1 = new Version(2, 1);
        boolean update2_1 = false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), FILE_CHARSET))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    if (line.startsWith("# version:")) {
                        try {
                            csvSkriptVersion = new Version(line.substring("# version:".length()).trim());
                            update2_0_beta3 = csvSkriptVersion.isSmallerThan(v2_0_beta3);
                            update2_1 = csvSkriptVersion.isSmallerThan(v2_1);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    continue;
                }

                String[] split = splitCSV(line);
                if (split == null || split.length != 3) {
                    Skript.error("invalid amount of commas in line " + lineNum + " ('" + line + "')");
                    if (invalid.length() != 0) {
                        invalid.append(", ");
                    }
                    invalid.append(split == null ? "<unknown>" : split[0]);
                    unsuccessfulVariableCount++;
                    continue;
                }

                if (split[1].equals("null")) {
                    Variables.variableLoaded(split[0], null, this);
                    continue;
                }

                Object deserializedValue;
                if (update2_1) {
                    deserializedValue = SerializedVariable.deserialize(split[1], split[2]);
                } else {
                    deserializedValue = SerializedVariable.deserialize(split[1], decode(split[2]));
                }

                if (deserializedValue == null) {
                    if (invalid.length() != 0) {
                        invalid.append(", ");
                    }
                    invalid.append(split[0]);
                    unsuccessfulVariableCount++;
                    continue;
                }

                if (deserializedValue instanceof String && update2_0_beta3) {
                    deserializedValue = Utils.replaceChatStyles((String) deserializedValue);
                }

                Variables.variableLoaded(split[0], deserializedValue, this);
            }
        } catch (IOException e) {
            loadError = true;
            ioException = e;
        }

        if (ioException != null || unsuccessfulVariableCount > 0 || update2_1) {
            if (unsuccessfulVariableCount > 0) {
                Skript.error(unsuccessfulVariableCount + " variable" + (unsuccessfulVariableCount == 1 ? "" : "s")
                        + " could not be loaded!");
                Skript.error("Affected variables: " + invalid);
            }

            if (ioException != null) {
                Skript.error("An I/O error occurred while loading the variables: " + ExceptionUtils.toString(ioException));
                Skript.error("This means that some to all variables could not be loaded!");
            }

            try {
                if (update2_1) {
                    Skript.info("[2.1] updating " + file.getName() + " to the new format...");
                }

                File backupFile = FileUtils.backup(file);
                Skript.info("Created a backup of " + file.getName() + " as " + backupFile.getName());
                loadError = false;
            } catch (IOException ex) {
                Skript.error("Could not backup " + file.getName() + ": " + ex.getMessage());
            }
        }

        if (update2_1) {
            saveVariables(false);
            Skript.info(file.getName() + " successfully updated.");
        }

        connect();

        saveTask = new Task(Skript.getInstance(), SAVE_TASK_DELAY, SAVE_TASK_PERIOD, true) {
            @Override
            public void run() {
                if (changes.get() >= REQUIRED_CHANGES_FOR_RESAVE) {
                    saveVariables(false);
                    changes.set(0);
                }
            }
        };

        return ioException == null;
    }

    @Override
    protected void allLoaded() {
    }

    @Override
    protected boolean requiresFile() {
        return true;
    }

    @Override
    protected File getFile(String fileName) {
        return new File(fileName);
    }

    @Override
    protected final void disconnect() {
        synchronized (connectionLock) {
            clearChangesQueue();
            synchronized (changesWriterMonitor) {
                if (changesWriter != null) {
                    changesWriter.close();
                    changesWriter = null;
                }
            }
        }
    }

    @Override
    protected final boolean connect() {
        synchronized (connectionLock) {
            synchronized (changesWriterMonitor) {
                assert file != null;

                if (changesWriter != null) {
                    return true;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(file, true);
                    changesWriter = new PrintWriter(new OutputStreamWriter(fos, FILE_CHARSET));
                    loaded = true;
                    changesWriterMonitor.notifyAll();
                    return true;
                } catch (IOException e) {
                    Skript.exception(e);
                    return false;
                }
            }
        }
    }

    @Override
    public void close() {
        clearChangesQueue();
        super.close();
        saveVariables(true);
    }

    @Override
    protected boolean save(String name, @Nullable String type, @Nullable byte[] value) {
        synchronized (connectionLock) {
            synchronized (changesWriterMonitor) {
                if (!loaded && type == null) {
                    return true;
                }

                while (changesWriter == null) {
                    try {
                        changesWriterMonitor.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                writeCSV(changesWriter, name, type, value == null ? "" : encode(value));
                changesWriter.flush();
                changes.incrementAndGet();
            }
        }
        return true;
    }

    public final void saveVariables(boolean finalSave) {
        if (finalSave) {
            if (saveTask != null) {
                saveTask.cancel();
            }
            if (backupTask != null) {
                backupTask.cancel();
            }
        }

        try {
            Variables.getReadLock().lock();

            synchronized (connectionLock) {
                try {
                    if (file == null) {
                        return;
                    }

                    disconnect();

                    if (loadError) {
                        try {
                            File backup = FileUtils.backup(file);
                            Skript.info("Created a backup of the old " + file.getName() + " as " + backup.getName());
                            loadError = false;
                        } catch (IOException e) {
                            Skript.error("Could not backup the old " + file.getName() + ": " + ExceptionUtils.toString(e));
                            Skript.error("No variables are saved!");
                            return;
                        }
                    }

                    File tempFile = new File(file.getParentFile(), file.getName() + ".temp");

                    try (PrintWriter pw = new PrintWriter(tempFile, StandardCharsets.UTF_8)) {
                        pw.println("# === Skript's variable storage ===");
                        pw.println("# Please do not modify this file manually!");
                        pw.println("#");
                        pw.println("# version: " + Skript.getVersion());
                        pw.println();
                        save(pw, "", Variables.getVariables());
                        pw.println();
                        pw.flush();
                        FileUtils.move(tempFile, file, true);
                    } catch (IOException e) {
                        Skript.error("Unable to make a final save of the database '" + getUserConfigurationName()
                                + "' (no variables are lost): " + ExceptionUtils.toString(e));
                    }
                } finally {
                    if (!finalSave) {
                        connect();
                    }
                }
            }
        } finally {
            Variables.getReadLock().unlock();
            boolean gotWriteLock = Variables.variablesLock.writeLock().tryLock();
            if (gotWriteLock) {
                try {
                    Variables.processChangeQueue();
                } finally {
                    Variables.variablesLock.writeLock().unlock();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void save(PrintWriter pw, String parent, TreeMap<String, Object> map) {
        if (parent.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN)) {
            return;
        }

        for (Entry<String, Object> childEntry : map.entrySet()) {
            Object childNode = childEntry.getValue();
            String childKey = childEntry.getKey();

            if (childNode == null) {
                continue;
            }

            if (childNode instanceof TreeMap) {
                save(pw, parent + childKey + Variable.SEPARATOR, (TreeMap<String, Object>) childNode);
                continue;
            }

            String name = childKey == null ? parent.substring(0, parent.length() - Variable.SEPARATOR.length()) : parent + childKey;
            if (name.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN)) {
                continue;
            }

            try {
                if (Variables.STORAGES.isEmpty()) {
                    SerializedVariable.Value serializedValue = SerializedVariable.serialize(childNode);
                    if (serializedValue != null) {
                        writeCSV(pw, name, serializedValue.type, encode(serializedValue.data));
                    }
                    continue;
                }

                for (VariablesStorage storage : Variables.STORAGES) {
                    if (!storage.accept(name)) {
                        continue;
                    }
                    if (storage == this) {
                        SerializedVariable.Value serializedValue = SerializedVariable.serialize(childNode);
                        if (serializedValue != null) {
                            writeCSV(pw, name, serializedValue.type, encode(serializedValue.data));
                        }
                    }
                    break;
                }
            } catch (Exception ex) {
                Skript.exception(ex, "Error saving variable named " + name);
            }
        }
    }

    static String encode(byte[] data) {
        char[] encoded = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            encoded[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 0xF0) >>> 4, 16));
            encoded[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 0xF, 16));
        }
        return new String(encoded);
    }

    static byte[] decode(String hex) {
        byte[] decoded = new byte[hex.length() / 2];
        for (int i = 0; i < decoded.length; i++) {
            decoded[i] = (byte) ((Character.digit(hex.charAt(2 * i), 16) << 4)
                    + Character.digit(hex.charAt(2 * i + 1), 16));
        }
        return decoded;
    }

    private static final Pattern CSV_LINE_PATTERN =
            Pattern.compile("(?<=^|,)\\s*(?:([^\",]*)|\"((?:[^\"]+|\"\")*)\")\\s*(?:,|$)");
    private static final Pattern CONTAINS_WHITESPACE = Pattern.compile("\\s");

    @Nullable
    static String[] splitCSV(String line) {
        Matcher matcher = CSV_LINE_PATTERN.matcher(line);

        int lastEnd = 0;
        ArrayList<String> result = new ArrayList<>();

        while (matcher.find()) {
            if (lastEnd != matcher.start()) {
                return null;
            }

            if (matcher.group(1) != null) {
                result.add(matcher.group(1).trim());
            } else {
                result.add(matcher.group(2).replace("\"\"", "\""));
            }

            lastEnd = matcher.end();
        }

        if (lastEnd != line.length()) {
            return null;
        }

        return result.toArray(new String[0]);
    }

    private static void writeCSV(PrintWriter printWriter, String... values) {
        assert values.length == 3;

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                printWriter.print(", ");
            }

            String value = values[i];
            boolean escapingNeeded = value != null
                    && (value.contains(",")
                    || value.contains("\"")
                    || value.contains("#")
                    || CONTAINS_WHITESPACE.matcher(value).find());
            if (escapingNeeded) {
                value = '"' + value.replace("\"", "\"\"") + '"';
            }

            printWriter.print(value);
        }

        printWriter.println();
    }

    public static void setRequiredChangesForResave(int value) {
        if (value <= 0) {
            Skript.warning("Variable changes until save cannot be zero or less. Using default of 1000.");
            value = 1000;
        }
        REQUIRED_CHANGES_FOR_RESAVE = value;
    }
}
