package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.io.File;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricTaskScheduler;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public final class StructAutoReload extends Structure {

    public static final Priority PRIORITY = new Priority(10);
    private static final long CHECK_INTERVAL_TICKS = 40; // 2 seconds

    private @Nullable Script script;
    private @Nullable File file;
    private long lastModified;
    private int taskId = -1;

    public static void register() {
        Skript.registerStructure(
                StructAutoReload.class,
                SyntaxInfo.Structure.NodeType.BOTH,
                "auto[matically] reload [(this|the) script]"
        );
    }

    @Override
    public boolean init(
            Literal<?>[] args,
            int matchedPattern,
            ParseResult parseResult,
            @Nullable EntryContainer entryContainer
    ) {
        script = getParser().getCurrentScript();
        if (script == null || script.getConfig() == null) {
            return false;
        }
        file = script.getConfig().getFile();
        if (file == null || !file.exists()) {
            Skript.error("Cannot auto-reload: script file not found");
            return false;
        }
        lastModified = file.lastModified();
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean postLoad() {
        taskId = SkriptFabricTaskScheduler.scheduleRepeating(CHECK_INTERVAL_TICKS, this::checkAndReload);
        return true;
    }

    @Override
    public void unload() {
        if (taskId >= 0) {
            SkriptFabricTaskScheduler.cancelRepeating(taskId);
            taskId = -1;
        }
    }

    private void checkAndReload() {
        if (file == null || script == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        long currentModified = file.lastModified();
        if (currentModified <= lastModified) {
            return;
        }
        lastModified = currentModified;
        // Cancel our own repeating task before unloading (the new script will start its own)
        if (taskId >= 0) {
            SkriptFabricTaskScheduler.cancelRepeating(taskId);
            taskId = -1;
        }
        try {
            SkriptRuntime runtime = SkriptRuntime.instance();
            String displayPath = script.getConfig().getFileName();
            runtime.unloadScripts(List.of(script));
            runtime.loadFromPath(file.toPath(), displayPath != null ? displayPath : file.getName());
            Skript.warning("Auto-reloaded script: " + file.getName());
        } catch (Exception e) {
            Skript.error("Failed to auto-reload " + file.getName() + ": " + e.getMessage());
            // Restart checking so the user can fix and save again
            taskId = SkriptFabricTaskScheduler.scheduleRepeating(CHECK_INTERVAL_TICKS, this::checkAndReload);
        }
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "auto reload";
    }
}
