package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprConfig extends SimpleExpression<Config> implements ReflectionExperimentSyntax {

    private static volatile @Nullable Config mainConfig;

    static {
        Skript.registerExpression(ExprConfig.class, Config.class, "[the] [skript] config");
    }

    private @Nullable Config config;

    static void setMainConfig(@Nullable Config config) {
        mainConfig = config;
    }

    static @Nullable Config getMainConfig() {
        return mainConfig;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        config = mainConfig;
        if (config == null) {
            Skript.warning("The main config is unavailable here!");
            return false;
        }
        return true;
    }

    @Override
    protected Config[] get(SkriptEvent event) {
        if (config == null || !config.valid()) {
            config = mainConfig;
        }
        if (config != null && config.valid()) {
            return new Config[]{config};
        }
        return new Config[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Config> getReturnType() {
        return Config.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the skript config";
    }
}
