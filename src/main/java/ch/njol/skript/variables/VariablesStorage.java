package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"SuspiciousIndentAfterControlStatement", "removal"})
public abstract class VariablesStorage implements AutoCloseable {

    private static final int QUEUE_SIZE = 1000;
    private static final int FIRST_WARNING = 300;

    final LinkedBlockingQueue<SerializedVariable> changesQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    protected volatile boolean closed = false;
    private String databaseName;
    private final String databaseType;
    @Nullable
    protected File file;
    @Nullable
    private Pattern variableNamePattern;
    private final Thread writeThread;
    protected final Object connectionLock = new Object();
    @Nullable
    protected Task backupTask = null;

    private static final Set<File> registeredFiles = new HashSet<>();
    private static final int WARNING_INTERVAL = 10;
    private static final int ERROR_INTERVAL = 10;

    private long lastWarning = Long.MIN_VALUE;
    private long lastError = Long.MIN_VALUE;

    protected VariablesStorage(String type) {
        databaseType = type;
        writeThread = Skript.newThread(() -> {
            while (!closed) {
                try {
                    SerializedVariable variable = changesQueue.take();
                    SerializedVariable.Value value = variable.value;
                    if (value != null) {
                        save(variable.name, value.type, value.data);
                    } else {
                        save(variable.name, null, null);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }, "Skript variable save thread for database '" + type + "'");
    }

    protected final String getUserConfigurationName() {
        return databaseName;
    }

    protected final String getDatabaseType() {
        return databaseType;
    }

    @Nullable
    protected String getValue(SectionNode sectionNode, String key) {
        return getValue(sectionNode, key, String.class);
    }

    @Nullable
    protected <T> T getValue(SectionNode sectionNode, String key, Class<T> type) {
        String rawValue = sectionNode.getValue(key);
        if (rawValue == null) {
            Skript.error("The config is missing the entry for '" + key + "' in the database '" + databaseName + "'");
            return null;
        }

        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            T parsedValue = Classes.parse(rawValue, type, ParseContext.CONFIG);

            if (parsedValue == null) {
                log.printError("The entry for '" + key + "' in the database '" + databaseName + "' must be "
                        + Classes.getSuperClassInfo(type).getName().withIndefiniteArticle());
            } else {
                log.printLog();
            }

            return parsedValue;
        }
    }

    public final boolean load(SectionNode sectionNode) {
        databaseName = sectionNode.getKey();

        String pattern = getValue(sectionNode, "pattern");
        if (pattern == null) {
            return false;
        }

        try {
            variableNamePattern = pattern.equals(".*") || pattern.equals(".+") ? null : Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            Skript.error("Invalid pattern '" + pattern + "': " + e.getLocalizedMessage());
            return false;
        }

        if (requiresFile()) {
            String fileName = getValue(sectionNode, "file");
            if (fileName == null) {
                return false;
            }

            file = getFile(fileName).getAbsoluteFile();

            if (file.exists() && !file.isFile()) {
                Skript.error("The database file '" + file.getName() + "' must be an actual file, not a directory.");
                return false;
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                Skript.error("Cannot create the database file '" + file.getName() + "': " + e.getLocalizedMessage());
                return false;
            }

            if (!file.canWrite()) {
                Skript.error("Cannot write to the database file '" + file.getName() + "'!");
                return false;
            }
            if (!file.canRead()) {
                Skript.error("Cannot read from the database file '" + file.getName() + "'!");
                return false;
            }

            if (registeredFiles.contains(file)) {
                Skript.error("Database `" + databaseName + "` failed to load. The file `" + fileName
                        + "` is already registered to another database.");
                return false;
            }
            registeredFiles.add(file);

            if (!"0".equals(getValue(sectionNode, "backup interval"))) {
                Timespan backupInterval = getValue(sectionNode, "backup interval", Timespan.class);
                Integer toKeep = getValue(sectionNode, "backups to keep", Integer.class);
                boolean removeBackups = false;
                boolean startBackup = true;
                if (backupInterval != null) {
                    if (toKeep != null && toKeep == 0) {
                        startBackup = false;
                    } else if (toKeep != null && toKeep >= 1) {
                        removeBackups = true;
                    }
                    if (startBackup) {
                        startBackupTask(backupInterval, removeBackups, toKeep == null ? 0 : toKeep);
                    } else {
                        try {
                            FileUtils.backupPurge(file, toKeep == null ? 0 : toKeep);
                        } catch (IOException e) {
                            Skript.error("Variables backup wipe failed: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        }

        if (!load_i(sectionNode)) {
            return false;
        }

        Variables.registerLoadedStorage(this);
        writeThread.start();
        Skript.closeOnDisable(this);
        return true;
    }

    protected abstract boolean load_i(SectionNode node);

    protected abstract void allLoaded();

    protected abstract boolean requiresFile();

    protected abstract File getFile(String fileName);

    protected abstract boolean connect();

    protected abstract void disconnect();

    public void startBackupTask(Timespan backupInterval, boolean removeBackups, int toKeep) {
        if (file == null || backupInterval.getAs(Timespan.TimePeriod.TICK) == 0) {
            return;
        }
        backupTask = new Task(Skript.getInstance(),
                backupInterval.getAs(Timespan.TimePeriod.TICK),
                backupInterval.getAs(Timespan.TimePeriod.TICK),
                true) {
            @Override
            public void run() {
                synchronized (connectionLock) {
                    disconnect();
                    try {
                        FileUtils.backup(file);
                        if (removeBackups) {
                            try {
                                FileUtils.backupPurge(file, toKeep);
                            } catch (IOException | IllegalArgumentException e) {
                                Skript.error("Automatic variables backup purge failed: " + e.getLocalizedMessage());
                            }
                        }
                    } catch (IOException e) {
                        Skript.error("Automatic variables backup failed: " + e.getLocalizedMessage());
                    } finally {
                        connect();
                    }
                }
            }
        };
    }

    boolean accept(@Nullable String var) {
        if (var == null) {
            return false;
        }
        return variableNamePattern == null || variableNamePattern.matcher(var).matches();
    }

    public @Nullable Pattern getNamePattern() {
        return variableNamePattern;
    }

    final void save(SerializedVariable var) {
        if (changesQueue.size() > FIRST_WARNING && lastWarning < System.currentTimeMillis() - WARNING_INTERVAL * 1000L) {
            Skript.warning("Cannot write variables to the database '" + databaseName + "' at sufficient speed; "
                    + "server performance may suffer and many variables will be lost if the server crashes. "
                    + "(this warning will be repeated at most once every " + WARNING_INTERVAL + " seconds)");
            lastWarning = System.currentTimeMillis();
        }

        if (changesQueue.offer(var)) {
            return;
        }

        if (lastError < System.currentTimeMillis() - ERROR_INTERVAL * 1000L) {
            Skript.error("Skript cannot save any variables to the database '" + databaseName + "'. "
                    + "The server will hang and may crash if no more variables can be saved.");
            lastError = System.currentTimeMillis();
        }

        while (true) {
            try {
                changesQueue.put(var);
                return;
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void close() {
        while (changesQueue.size() > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        closed = true;
        writeThread.interrupt();
        Variables.unregisterLoadedStorage(this);
    }

    protected void clearChangesQueue() {
        changesQueue.clear();
    }

    protected abstract boolean save(String name, @Nullable String type, @Nullable byte[] value);
}
