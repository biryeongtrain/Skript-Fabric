package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

@Name("Log")
@Description({
        "Writes text into a .log file. Relative file paths are written under the local `logs` folder.",
        "Using 'server.log' as the log file will write to the default server log. Omitting the log file altogether will log the message as '[<script>.sk] <message>' in the server log."
})
@Example("""
        on join:
            log "%player% has just joined the server!"
        """)
@Example("""
        on world change:
            log "Someone just went to %event-world%!" to file "worldlog/worlds.log"
        """)
@Example("""
        on command:
            log "%player% just executed %full command%!" to file "server/commands.log" with a severity of warning
        """)
@Since("2.0, 2.9.0 (severities)")
public class EffLog extends Effect {

    private static final File LOGS_FOLDER = new File("logs");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    static final Map<String, PrintWriter> writers = new HashMap<>();
    private static boolean registered;

    private Expression<String> messages;
    private @Nullable Expression<String> files;
    private Level logLevel = Level.INFO;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffLog.class, "log %strings% [(to|in) [file[s]] %-strings%] [with [the|a] severity [of] (1:warning|2:severe)]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        messages = (Expression<String>) exprs[0];
        files = (Expression<String>) exprs[1];
        if (parser.mark == 1) {
            logLevel = Level.WARNING;
        } else if (parser.mark == 2) {
            logLevel = Level.SEVERE;
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (String message : messages.getArray(event)) {
            if (files != null) {
                for (String logFile : files.getArray(event)) {
                    writeFile(logFile, message);
                }
            } else {
                SkriptLogger.log(logLevel, "[" + scriptName() + "] " + message);
            }
        }
    }

    private void writeFile(String rawLogFile, String message) {
        String logFile = rawLogFile.toLowerCase(Locale.ENGLISH);
        if (!logFile.endsWith(".log")) {
            logFile += ".log";
        }
        if (logFile.equals("server.log")) {
            SkriptLogger.log(logLevel, message);
            return;
        }
        PrintWriter logWriter = writers.get(logFile);
        if (logWriter == null) {
            File target = new File(LOGS_FOLDER, logFile);
            try {
                File parent = target.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                logWriter = new PrintWriter(new BufferedWriter(new FileWriter(target, true)));
                writers.put(logFile, logWriter);
            } catch (IOException ex) {
                Skript.error("Cannot write to log file '" + logFile + "' (" + target.getPath() + "): " + ex.getMessage());
                return;
            }
        }
        logWriter.println(prefix(logLevel) + " " + message);
        logWriter.flush();
    }

    private String scriptName() {
        Trigger trigger = getTrigger();
        if (trigger == null) {
            return "---";
        }
        Script script = trigger.getScript();
        if (script == null || script.getConfig() == null) {
            return "---";
        }
        return script.getConfig().getFileName();
    }

    private static String prefix(Level level) {
        String timestamp = TIMESTAMP_FORMAT.format(Instant.now());
        if (level == Level.INFO) {
            return "[" + timestamp + "]";
        }
        return "[" + timestamp + " " + level + "]";
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "log " + messages.toString(event, debug)
                + (files != null ? " to " + files.toString(event, debug) : "")
                + (logLevel != Level.INFO ? " with severity " + logLevel.getName().toLowerCase(Locale.ENGLISH) : "");
    }
}
