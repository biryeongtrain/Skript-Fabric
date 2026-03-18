package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricServerListPingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Player Info Visibility")
@Description({"Sets whether all player related information is hidden in the server list.",
        "The Vanilla Minecraft client will display ??? (dark gray) instead of player counts and will not show the",
        "<a href='#ExprHoverList'>hover hist</a> when hiding player info.",
        "<a href='#ExprVersionString'>The version string</a> can override the ???.",
        "Also the <a href='#ExprOnlinePlayersCount'>Online Players Count</a> and",
        "<a href='#ExprMaxPlayers'>Max Players</a> expressions will return -1 when hiding player info."})
@Example("hide player info")
@Example("hide player related information in the server list")
@Example("reveal all player related info")
@Since("2.3")
@Events("server list ping")
public final class EffPlayerInfoVisibility extends Effect {

    private static boolean registered;
    private boolean shouldHide;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffPlayerInfoVisibility.class,
                "hide [all] player [related] info[rmation] [(in|on|from) [the] server list]",
                "(show|reveal) [all] player [related] info[rmation] [(in|to|on|from) [the] server list]"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricServerListPingEventHandle.class)) {
            Skript.error("The player info visibility effect can only be used in a server list ping event");
            return false;
        }
        shouldHide = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (!(event.handle() instanceof FabricServerListPingEventHandle handle)) {
            return;
        }
        handle.setHidePlayerInfo(shouldHide);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (shouldHide ? "hide" : "show") + " player info in the server list";
    }
}
