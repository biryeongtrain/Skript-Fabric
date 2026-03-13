package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

public class ExprScriptsOld extends SimpleExpression<String> implements SimpleExperimentalSyntax {

    private static final ExperimentData EXPERIMENT_DATA =
            ExperimentData.builder().disallowed(Feature.SCRIPT_REFLECTION).build();

    static {
        Skript.registerExpression(
                ExprScriptsOld.class,
                String.class,
                "[all [of the]|the] scripts [1:without ([subdirectory] paths|parents)]",
                "[all [of the]|the] (enabled|loaded) scripts [1:without ([subdirectory] paths|parents)]",
                "[all [of the]|the] (disabled|unloaded) scripts [1:without ([subdirectory] paths|parents)]"
        );
    }

    private boolean includeEnabled;
    private boolean includeDisabled;
    private boolean noPaths;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        includeEnabled = matchedPattern <= 1;
        includeDisabled = matchedPattern != 1;
        noPaths = parseResult.mark == 1;
        return true;
    }

    @Override
    public ExperimentData getExperimentData() {
        return EXPERIMENT_DATA;
    }

    @Override
    protected String[] get(SkriptEvent event) {
        if (!includeEnabled && includeDisabled) {
            return new String[0];
        }
        List<Script> scripts = ExprScript.loadedScripts();
        return scripts.stream()
                .map(script -> noPaths ? formatLeafName(script) : formatPath(script))
                .filter(value -> value != null && !value.isBlank())
                .toArray(String[]::new);
    }

    private static @Nullable String formatLeafName(Script script) {
        String fileName = script.getConfig().getFileName();
        if (fileName == null) {
            return null;
        }
        int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        return slash >= 0 ? fileName.substring(slash + 1) : fileName;
    }

    private static @Nullable String formatPath(Script script) {
        return script.getConfig().getFileName();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String text;
        if (!includeEnabled) {
            text = "all disabled scripts";
        } else if (!includeDisabled) {
            text = "all enabled scripts";
        } else {
            text = "all scripts";
        }
        if (noPaths) {
            text += " without paths";
        }
        return text;
    }
}
