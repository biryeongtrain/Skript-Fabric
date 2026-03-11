package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Online Player Count")
@Description("The real amount of online players on the current Fabric compatibility surface.")
@Example("send online player count")
@Since("2.3, Fabric")
public class ExprOnlinePlayersCount extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(
                ExprOnlinePlayersCount.class,
                Long.class,
                "[the] [(1:(real|default)|2:(fake|shown|displayed))] [online] player (count|amount|number)",
                "[the] [(1:(real|default)|2:(fake|shown|displayed))] (count|amount|number|size) of online players"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (parseResult.mark == 2) {
            Skript.error("The shown online players count is not currently exposed on this compatibility surface.");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable Long[] get(SkriptEvent event) {
        var server = ExpressionRuntimeSupport.resolveServer(event);
        return server == null ? new Long[0] : new Long[]{(long) ExpressionRuntimeSupport.onlinePlayerCount(server)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the count of online players";
    }
}
