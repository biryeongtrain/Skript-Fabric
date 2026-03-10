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
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Has Played Before")
@Description("Checks whether a player has played on this server before. You can also use " +
        "<a href='#first_join'>on first join</a> if you want to make triggers for new players.")
@Example("player has played on this server before")
@Example("player hasn't played before")
@Since("1.4, 2.7 (multiple players)")
public class CondPlayedBefore extends Condition {

    static {
        Skript.registerCondition(
                CondPlayedBefore.class,
                "%offlineplayers% [(has|have|did)] [already] play[ed] [on (this|the) server] (before|already)",
                "%offlineplayers% (has not|hasn't|have not|haven't|did not|didn't) [(already|yet)] play[ed] [on (this|the) server] (before|already|yet)"
        );
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
        Path playerData = event.server().getWorldPath(LevelResource.PLAYER_DATA_DIR);
        return players.check(event, profile -> hasPlayedBefore(playerData, profile), isNegated());
    }

    private static boolean hasPlayedBefore(Path playerData, GameProfile profile) {
        return profile.getId() != null && Files.exists(playerData.resolve(profile.getId() + ".dat"));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return players.toString(event, debug)
                + (isNegated() ? (players.isSingle() ? " hasn't" : " haven't") : (players.isSingle() ? " has" : " have"))
                + " played on this server before";
    }
}
