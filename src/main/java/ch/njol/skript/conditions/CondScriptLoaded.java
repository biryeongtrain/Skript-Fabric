package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

@Name("Is Script Loaded")
@Description("Check if the current script, or another script, is currently loaded.")
@Example("script is loaded")
@Example("script \"example.sk\" is loaded")
@Since("2.2-dev31")
public class CondScriptLoaded extends Condition {

    static {
        Skript.registerCondition(CondScriptLoaded.class,
                "script[s] [%-strings%] (is|are) loaded",
                "script[s] [%-strings%] (isn't|is not|aren't|are not) loaded"
        );
    }

    private @Nullable Expression<String> scripts;
    private @Nullable Script currentScript;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        scripts = exprs.length == 0 ? null : (Expression<String>) exprs[0];
        ParserInstance parser = getParser();
        if (scripts == null) {
            if (parser.isActive()) {
                currentScript = parser.getCurrentScript();
            } else {
                Skript.error("The condition 'script loaded' requires a script name argument when used outside of script files");
                return false;
            }
        }
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (scripts == null) {
            return ConditionRuntimeSupport.isLoadedScript(currentScript) ^ isNegated();
        }
        return scripts.check(event, ConditionRuntimeSupport::isLoadedScriptNamed, isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String scriptName = scripts == null
                ? "script"
                : (scripts.isSingle() ? "script " : "scripts ") + scripts.toString(event, debug);
        if (scripts == null || scripts.isSingle()) {
            return scriptName + (isNegated() ? " isn't" : " is") + " loaded";
        }
        return scriptName + (isNegated() ? " aren't" : " are") + " loaded";
    }
}
