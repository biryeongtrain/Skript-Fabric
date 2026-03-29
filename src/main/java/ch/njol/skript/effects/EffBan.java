package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanListEntry;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Ban")
@Description({"Bans or unbans a player or an IP address.",
        "If a reason is given, it will be shown to the player when they try to join the server while banned.",
        "A length of ban may also be given to apply a temporary ban. If it is absent for any reason, a permanent ban will be used instead.",
        "We recommend that you test your scripts so that no accidental permanent bans are applied.",
        "",
        "Note that banning people does not kick them from the server.",
        "You can optionally use 'and kick' or consider using the <a href='#EffKick'>kick effect</a> after applying a ban."})
@Example("unban player")
@Example("ban \"127.0.0.1\"")
@Example("IP-ban the player because \"he is an idiot\"")
@Example("ban player due to \"inappropriate language\" for 2 days")
@Example("ban and kick player due to \"inappropriate language\" for 2 days")
@Since("1.4, 2.1.1 (ban reason), 2.5 (timespan), 2.9.0 (kick)")
public final class EffBan extends Effect {

    private static boolean registered;

    private Expression<?> players;
    private @Nullable Expression<String> reason;
    private @Nullable Expression<Timespan> expires;
    private boolean ban;
    private boolean ipBan;
    private boolean kick;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffBan.class,
                "ban [kick:and kick] %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
                "unban %strings/offlineplayers%",
                "ban [kick:and kick] %players% by IP [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
                "unban %players% by IP",
                "IP(-| )ban [kick:and kick] %players% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
                "(IP(-| )unban|un[-]IP[-]ban) %players%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = exprs[0];
        reason = exprs.length > 1 ? (Expression<String>) exprs[1] : null;
        expires = exprs.length > 2 ? (Expression<Timespan>) exprs[2] : null;
        ban = matchedPattern % 2 == 0;
        ipBan = matchedPattern >= 2;
        kick = parseResult.hasTag("kick");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.server() == null) {
            return;
        }
        PlayerList playerList = event.server().getPlayerList();
        String reason = this.reason == null ? null : this.reason.getSingle(event);
        Date expiry = null;
        if (expires != null) {
            Timespan timespan = expires.getSingle(event);
            if (timespan != null) {
                expiry = new Date(System.currentTimeMillis() + timespan.getAs(Timespan.TimePeriod.MILLISECOND));
            }
        }
        for (Object value : players.getArray(event)) {
            if (ipBan) {
                String ip = ipOf(value);
                if (ip == null || ip.isBlank()) {
                    continue;
                }
                if (ban) {
                    playerList.getIpBans().add(new IpBanListEntry(ip, null, "Skript ban effect", expiry, reason));
                } else {
                    playerList.getIpBans().remove(ip);
                }
                if (kick && value instanceof ServerPlayer player) {
                    player.connection.disconnect(EffectRuntimeSupport.componentOf(reason, event));
                }
                continue;
            }

            if (value instanceof ServerPlayer player) {
                if (ban) {
                    playerList.getBans().add(new UserBanListEntry(new net.minecraft.server.players.NameAndId(player.getGameProfile()), null, "Skript ban effect", expiry, reason));
                } else {
                    playerList.getBans().remove(new net.minecraft.server.players.NameAndId(player.getGameProfile()));
                }
                if (kick) {
                    player.connection.disconnect(EffectRuntimeSupport.componentOf(reason, event));
                }
                continue;
            }

            if (value instanceof GameProfile profile) {
                if (ban) {
                    playerList.getBans().add(new UserBanListEntry(new net.minecraft.server.players.NameAndId(profile), null, "Skript ban effect", expiry, reason));
                } else {
                    playerList.getBans().remove(new net.minecraft.server.players.NameAndId(profile));
                }
                continue;
            }

            if (value instanceof String string) {
                if (ban) {
                    playerList.getIpBans().add(new IpBanListEntry(string, null, "Skript ban effect", expiry, reason));
                    playerList.getBans().add(new UserBanListEntry(net.minecraft.server.players.NameAndId.createOffline(string), null, "Skript ban effect", expiry, reason));
                } else {
                    playerList.getIpBans().remove(string);
                    playerList.getBans().remove(net.minecraft.server.players.NameAndId.createOffline(string));
                }
            }
        }
    }

    private @Nullable String ipOf(Object value) {
        if (value instanceof ServerPlayer player) {
            return player.getIpAddress();
        }
        return value instanceof String string ? string : null;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        if (ipBan) {
            builder.append("IP");
        }
        builder.append(ban ? "ban" : "unban");
        if (kick) {
            builder.append("and kick");
        }
        builder.append(players);
        if (reason != null) {
            builder.append("on account of", reason);
        }
        if (expires != null) {
            builder.append("for", expires);
        }
        return builder.toString();
    }
}
