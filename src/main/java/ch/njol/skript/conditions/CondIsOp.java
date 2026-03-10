package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Operator")
@Description("Checks whether a player is a server operator.")
@Example("player is an operator")
@Since("2.7")
public class CondIsOp extends Condition {

    static {
        Skript.registerCondition(CondIsOp.class,
                "%offlineplayers% (is|are) [[a] server|an] op[erator][s]",
                "%offlineplayers% (isn't|is not|aren't|are not) [[a] server|an] op[erator][s]");
    }

    private Expression<GameProfile> players;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<GameProfile>) exprs[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (event.server() == null) {
            return isNegated();
        }
        return players.check(event, profile -> event.server().getPlayerList().isOp(profile), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return players.toString(event, debug)
                + (players.isSingle() ? " is " : " are ")
                + (isNegated() ? "not " : "")
                + "op";
    }
}
