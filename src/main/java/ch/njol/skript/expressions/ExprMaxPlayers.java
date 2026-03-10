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

@Name("Max Players")
@Description("The server's real max player count on the current compatibility surface.")
@Example("send max players count")
@Since("2.3, Fabric")
public class ExprMaxPlayers extends SimpleExpression<Integer> {

    static {
        Skript.registerExpression(
                ExprMaxPlayers.class,
                Integer.class,
                "[the] [1:(real|default)|2:(fake|shown|displayed)] max[imum] player[s] [count|amount|number|size]",
                "[the] [1:(real|default)|2:(fake|shown|displayed)] max[imum] (count|amount|number|size) of players"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (parseResult.mark == 2) {
            Skript.error("The shown max players count is not currently exposed on this compatibility surface.");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable Integer[] get(SkriptEvent event) {
        var server = ExpressionRuntimeSupport.resolveServer(event);
        return server == null ? new Integer[0] : new Integer[]{server.getMaxPlayers()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the max players count";
    }
}
