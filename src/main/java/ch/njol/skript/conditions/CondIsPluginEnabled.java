package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsPluginEnabled extends Condition {

    static {
        Skript.registerCondition(
                CondIsPluginEnabled.class,
                "plugin[s] %strings% (is|are) enabled",
                "plugin[s] %strings% (is|are)(n't| not) enabled",
                "plugin[s] %strings% (is|are) disabled"
        );
    }

    private Expression<String> plugins;
    private int pattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        plugins = (Expression<String>) exprs[0];
        pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return plugins.check(event, plugin -> {
            boolean enabled = ConditionRuntimeSupport.hasEnabledMod(plugin);
            return switch (pattern) {
                case 1, 2 -> !enabled;
                default -> enabled;
            };
        });
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String plugin = plugins.isSingle() ? "plugin " : "plugins ";
        String plural = plugins.isSingle() ? " is" : " are";
        String suffix = pattern == 0 ? " enabled" : pattern == 1 ? " not enabled" : " disabled";
        return plugin + plugins.toString(event, debug) + plural + suffix;
    }
}
