package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntimeServices;
import org.skriptlang.skript.fabric.runtime.SkriptScriptService;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

import java.io.IOException;

@Name("Enable/Disable/Unload/Reload Script")
@Description("""
        Enables, disables, unloads, or reloads a script.

        Disabling a script unloads it and prepends - to its name so it will not be loaded the next time the server restarts.
        If the script reflection experiment is enabled: unloading a script terminates it and removes it from memory, but does not alter the file.""")
@Example("reload script \"test\"")
@Example("enable script file \"testing\"")
@Example("unload script file \"script.sk\"")
@Example("""
        set {_script} to the script "MyScript.sk"
        reload {_script}
        """)
@Since("2.4, 2.10 (unloading)")
public class EffScriptFile extends Effect {

    private static boolean registered;

    private int mark;
    private @Nullable Expression<String> scriptNameExpression;
    private @Nullable Expression<Script> scriptExpression;
    private boolean scripts;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffScriptFile.class,
                "(1:(enable|load)|2:reload|3:disable|4:unload) script [file|named] %string% [print:with errors]",
                "(1:(enable|load)|2:reload|3:disable|4:unload) skript file %string% [print:with errors]",
                "(1:(enable|load)|2:reload|3:disable|4:unload) %scripts% [print:with errors]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        mark = parseResult.mark;
        switch (matchedPattern) {
            case 0, 1 -> scriptNameExpression = (Expression<String>) exprs[0];
            case 2 -> {
                scriptExpression = (Expression<Script>) exprs[0];
                scripts = true;
            }
            default -> {
            }
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        SkriptScriptService service = SkriptRuntimeServices.scriptService();
        try {
            if (scripts && scriptExpression != null) {
                for (Script script : scriptExpression.getArray(event)) {
                    String name = script.nameAndPath();
                    if (name == null) continue;
                    executeForTarget(service, name);
                }
            } else if (scriptNameExpression != null) {
                String name = scriptNameExpression.getSingle(event);
                if (name == null) return;
                executeForTarget(service, name);
            }
        } catch (IOException exception) {
            Skript.error("Script file operation failed: " + exception.getMessage());
        }
    }

    private void executeForTarget(SkriptScriptService service, String target) throws IOException {
        switch (mark) {
            case 1 -> service.enableTarget(target);
            case 2 -> service.reloadTarget(target);
            case 3 -> service.disableTarget(target);
            case 4 -> {
                var runtime = org.skriptlang.skript.fabric.runtime.SkriptRuntime.instance();
                for (Script script : runtime.loadedScripts()) {
                    String scriptName = script.nameAndPath();
                    if (scriptName != null && scriptName.equalsIgnoreCase(target)) {
                        runtime.unloadScripts(java.util.List.of(script));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String start = switch (mark) {
            case 1 -> "enable";
            case 2 -> "reload";
            case 3 -> "disable";
            default -> "unload";
        } + " ";
        if (scripts && scriptExpression != null) {
            return start + scriptExpression.toString(event, debug);
        }
        return start + "script file " + (scriptNameExpression == null ? "" : scriptNameExpression.toString(event, debug));
    }
}
