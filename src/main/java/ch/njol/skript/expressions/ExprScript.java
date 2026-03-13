package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

public class ExprScript extends SimpleExpression<Script> implements ReflectionExperimentSyntax {

    static {
        Skript.registerExpression(
                ExprScript.class,
                Script.class,
                "[the] [current] script",
                "[the] script[s] [named] %strings%",
                "[the] scripts in [directory|folder] %string%"
        );
    }

    private @Nullable Script script;
    private @Nullable Expression<String> name;
    private boolean directory;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        directory = matchedPattern == 2;
        if (matchedPattern == 0) {
            ParserInstance parser = getParser();
            if (!parser.isActive()) {
                Skript.error("'the current script' can only be used in a script.");
                return false;
            }
            script = parser.getCurrentScript();
            return script != null;
        }
        name = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected Script[] get(SkriptEvent event) {
        if (script != null) {
            return new Script[]{script};
        }
        if (name == null) {
            return new Script[0];
        }
        if (directory) {
            String folder = name.getSingle(event);
            if (folder == null) {
                return new Script[0];
            }
            String normalizedFolder = normalizeDirectory(folder);
            if (normalizedFolder.isEmpty()) {
                return loadedScripts().toArray(Script[]::new);
            }
            return loadedScripts().stream()
                    .filter(candidate -> isInDirectory(candidate, normalizedFolder))
                    .toArray(Script[]::new);
        }
        return name.stream(event)
                .map(ExprScript::findScript)
                .filter(Objects::nonNull)
                .distinct()
                .toArray(Script[]::new);
    }

    @Override
    public boolean isSingle() {
        return script != null || (name != null && name.isSingle() && !directory);
    }

    @Override
    public Class<? extends Script> getReturnType() {
        return Script.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (script != null) {
            return "the current script";
        }
        if (name == null) {
            return "the script";
        }
        if (directory) {
            return "the scripts in directory " + name.toString(event, debug);
        }
        return (name.isSingle() ? "the script named " : "the scripts named ") + name.toString(event, debug);
    }

    static List<Script> loadedScripts() {
        try {
            Field field = SkriptRuntime.class.getDeclaredField("scripts");
            field.setAccessible(true);
            Object value = field.get(SkriptRuntime.instance());
            if (value instanceof List<?> entries) {
                List<Script> scripts = new ArrayList<>(entries.size());
                for (Object entry : entries) {
                    if (entry instanceof Script script) {
                        scripts.add(script);
                    }
                }
                return scripts;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return List.of();
    }

    static @Nullable Script findScript(@Nullable String input) {
        String normalizedInput = normalizeName(input);
        if (normalizedInput.isEmpty()) {
            return null;
        }
        for (Script candidate : loadedScripts()) {
            if (matchesName(candidate, normalizedInput)) {
                return candidate;
            }
        }
        return null;
    }

    static boolean isInDirectory(Script script, String normalizedFolder) {
        String fileName = script.getConfig().getFileName();
        if (fileName == null) {
            return false;
        }
        String normalizedPath = normalizePath(fileName);
        return normalizedPath.startsWith(normalizedFolder + "/");
    }

    private static boolean matchesName(Script script, String normalizedInput) {
        String fileName = script.getConfig().getFileName();
        if (fileName != null && normalizeName(fileName).equals(normalizedInput)) {
            return true;
        }
        String nameAndPath = script.nameAndPath();
        if (nameAndPath != null && normalizeName(nameAndPath).equals(normalizedInput)) {
            return true;
        }
        return normalizeName(script.name()).equals(normalizedInput);
    }

    static String normalizeDirectory(String input) {
        String normalized = normalizePath(input);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    static String normalizeName(@Nullable String input) {
        String normalized = normalizePath(input);
        if (normalized.endsWith(".sk")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        return normalized;
    }

    private static String normalizePath(@Nullable String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
                .replace('\\', '/')
                .replaceAll("/+", "/")
                .replaceAll("^/", "")
                .toLowerCase(Locale.ENGLISH);
    }
}
