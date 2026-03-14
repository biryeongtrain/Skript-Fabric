package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLastResourcePackResponse extends SimpleExpression<String> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(
                ExprLastResourcePackResponse.class,
                String.class,
                "[last] resource pack response[s] of %players%",
                "%players%'[s] [last] resource pack response[s]"
        );
    }

    private Expression<ServerPlayer> players;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.ResourcePackResponse.class};
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.ResourcePackResponse handle)) {
            return new String[0];
        }
        ServerPlayer eventPlayer = event.player();
        FabricEventCompatHandles.ResourcePackState status = handle.status();
        if (eventPlayer == null || status == null) {
            return new String[0];
        }
        String statusName = status.name().toLowerCase().replace('_', ' ');
        return players.stream(event)
                .filter(player -> player == eventPlayer)
                .map(player -> statusName)
                .toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return players.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "last resource pack response of " + players.toString(event, debug);
    }
}
