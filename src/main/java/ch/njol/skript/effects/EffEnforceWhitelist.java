package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Enforce Whitelist")
@Description({
        "Enforces or un-enforce a server's whitelist.",
        "All non-whitelisted players will be kicked upon enforcing the whitelist."
})
@Example("enforce the whitelist")
@Example("unenforce the whitelist")
@Since("2.9.0")
@RequiredPlugins("MC 1.17+")
public class EffEnforceWhitelist extends Effect {

    private boolean enforce;

    static {
        Skript.registerEffect(EffEnforceWhitelist.class, "[:un]enforce [the] [server] white[ ]list");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        enforce = !parseResult.hasTag("un");
        return exprs.length == 0;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.server() == null) {
            return;
        }
        event.server().setEnforceWhitelist(enforce);
        invokeWhitelistToggle(event.server(), enforce);
        invokeKickUnlistedPlayers(event.server());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (!enforce ? "un" : "") + "enforce the whitelist";
    }

    private void invokeWhitelistToggle(net.minecraft.server.MinecraftServer server, boolean enabled) {
        try {
            server.getClass().getMethod("setUsingWhitelist", boolean.class).invoke(server, enabled);
            return;
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            server.getClass().getMethod("setUsingWhiteList", boolean.class).invoke(server, enabled);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private void invokeKickUnlistedPlayers(net.minecraft.server.MinecraftServer server) {
        try {
            server.getClass().getMethod("kickUnlistedPlayers", net.minecraft.commands.CommandSourceStack.class)
                    .invoke(server, server.createCommandSourceStack());
            return;
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            server.getClass().getMethod("kickUnlistedPlayers").invoke(server);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
