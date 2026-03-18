package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricServerListPingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("MOTD")
@Description("The server MOTD. Can be the default or the shown MOTD in a server list ping event.")
@Example("send motd")
@Example("""
        on server list ping:
            set the shown motd to "Welcome!"
        """)
@Since("2.3, Fabric")
public class ExprMOTD extends SimpleExpression<String> {

    private boolean defaultMotd;
    private boolean isServerPingEvent;

    static {
        Skript.registerExpression(
                ExprMOTD.class,
                String.class,
                "[the] [(1¦default)|(2¦shown|displayed)] (MOTD|message of [the] day)"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        isServerPingEvent = getParser().isCurrentEvent(FabricServerListPingEventHandle.class);
        if (parseResult.mark == 2) {
            if (!isServerPingEvent) {
                Skript.error("The shown MOTD expression can only be used in a server list ping event");
                return false;
            }
            defaultMotd = false;
        } else {
            defaultMotd = parseResult.mark == 1 || !isServerPingEvent;
        }
        return true;
    }

    @Override
    protected @Nullable String[] get(SkriptEvent event) {
        if (!defaultMotd && event.handle() instanceof FabricServerListPingEventHandle handle) {
            String motd = handle.motd();
            if (motd != null) {
                return new String[]{motd};
            }
        }
        var server = ExpressionRuntimeSupport.resolveServer(event);
        String motd = server == null ? null : ExpressionRuntimeSupport.motd(server);
        return motd == null ? new String[0] : new String[]{motd};
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        if (defaultMotd) {
            return null;
        }
        if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            return new Class[]{String.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricServerListPingEventHandle handle)) {
            return;
        }
        if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            handle.setMotd(null);
            return;
        }
        if (delta != null && delta.length > 0) {
            handle.setMotd((String) delta[0]);
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return defaultMotd ? "the default MOTD" : "the shown MOTD";
    }
}
