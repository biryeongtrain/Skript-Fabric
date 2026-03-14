package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.LinkedHashSet;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprMods extends SimpleExpression<String> {

    static {
        Skript.registerExpression(
                ExprMods.class,
                String.class,
                "[(all [[of] the]|the)] [loaded] mod[s] [ids]",
                "[(all [[of] the]|the)] [loaded] plugins");
    }

    private boolean pluginsPattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pluginsPattern = matchedPattern == 1;
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        FabricLoader.getInstance().getAllMods().forEach(container -> ids.add(container.getMetadata().getId()));
        return ids.toArray(String[]::new);
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
        return pluginsPattern ? "loaded plugins" : "loaded mod ids";
    }
}