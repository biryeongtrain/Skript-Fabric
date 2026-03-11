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

@Name("MOTD")
@Description("The default server MOTD on the current Fabric compatibility surface.")
@Example("send motd")
@Since("2.3, Fabric")
public class ExprMOTD extends SimpleExpression<String> {

    private boolean defaultMotd;

    static {
        Skript.registerExpression(
                ExprMOTD.class,
                String.class,
                "[the] [(1¦default)|(2¦shown|displayed)] (MOTD|message of [the] day)"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (parseResult.mark == 2) {
            Skript.error("The shown MOTD expression requires a server list ping compatibility event, which is not wired yet.");
            return false;
        }
        defaultMotd = true;
        return true;
    }

    @Override
    protected @Nullable String[] get(SkriptEvent event) {
        var server = ExpressionRuntimeSupport.resolveServer(event);
        String motd = server == null ? null : ExpressionRuntimeSupport.motd(server);
        return motd == null ? new String[0] : new String[]{motd};
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
        return defaultMotd ? "the default MOTD" : "the MOTD";
    }
}
