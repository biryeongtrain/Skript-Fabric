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
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Banned")
@Description("Checks whether a player or IP is banned.")
@Example("player is banned")
@Example("victim is not IP-banned")
@Example("\"127.0.0.1\" is banned")
@Since("1.4")
public class CondIsBanned extends Condition {

    static {
        Skript.registerCondition(CondIsBanned.class,
                "%offlineplayers/strings% (is|are) banned",
                "%players/strings% (is|are) IP(-| |)banned",
                "%offlineplayers/strings% (isn't|is not|aren't|are not) banned",
                "%players/strings% (isn't|is not|aren't|are not) IP(-| |)banned");
    }

    private Expression<?> values;
    private boolean ipBanned;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        values = exprs[0];
        setNegated(matchedPattern >= 2);
        ipBanned = (matchedPattern & 1) == 1;
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (event.server() == null) {
            return isNegated();
        }
        return values.check(event, value -> isBanned(event, value), isNegated());
    }

    private boolean isBanned(SkriptEvent event, Object value) {
        if (ipBanned) {
            if (value instanceof ServerPlayer player) {
                return event.server().getPlayerList().getIpBans().isBanned(player.getIpAddress());
            }
            if (value instanceof String ip) {
                return event.server().getPlayerList().getIpBans().isBanned(ip);
            }
            return false;
        }
        if (value instanceof GameProfile profile) {
            return event.server().getPlayerList().getBans().isBanned(profile);
        }
        if (value instanceof String name) {
            for (String bannedName : event.server().getPlayerList().getBans().getUserList()) {
                if (name.equalsIgnoreCase(bannedName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String property = ipBanned ? "IP-banned" : "banned";
        return values.toString(event, debug)
                + (values.isSingle() ? " is " : " are ")
                + (isNegated() ? "not " : "")
                + property;
    }
}
