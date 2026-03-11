package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import kim.biryeong.skriptFabric.mixin.StoredUserEntryAccessor;
import net.minecraft.server.players.StoredUserList;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprAllBannedEntries extends SimpleExpression<Object> {

    static {
        register(GameProfile.class, "players:[all [[of] the]|the] banned players");
        register(String.class, "ips:[all [[of] the]|the] banned (ips|ip addresses)");
    }

    private boolean ip;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        ip = parseResult.hasTag("ips");
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        if (event.server() == null) {
            return ip ? new String[0] : new GameProfile[0];
        }
        if (ip) {
            return event.server().getPlayerList().getIpBans().getUserList();
        }

        Collection<?> entries = event.server().getPlayerList().getBans() instanceof StoredUserList<?, ?> list
                ? list.getEntries()
                : List.of();
        List<GameProfile> profiles = new ArrayList<>();
        for (Object entry : entries) {
            Object key = entry instanceof StoredUserEntryAccessor accessor
                    ? accessor.skript$getUser()
                    : ExpressionHandleSupport.invoke(entry, "getUser");
            if (!(key instanceof GameProfile)) {
                key = ExpressionHandleSupport.invoke(entry, "getKey");
            }
            if (key instanceof GameProfile profile) {
                profiles.add(profile);
            }
        }
        return profiles.toArray(GameProfile[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<?> getReturnType() {
        return ip ? String.class : GameProfile.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "all banned " + (ip ? "ip addresses" : "players");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void register(Class<?> returnType, String pattern) {
        Skript.registerExpression((Class) ExprAllBannedEntries.class, (Class) returnType, pattern);
    }
}
