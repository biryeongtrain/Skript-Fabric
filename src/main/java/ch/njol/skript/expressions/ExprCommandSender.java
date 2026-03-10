package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprCommandSender extends SimpleExpression<ServerPlayer> {

    private static final Class<?> COMMAND_EVENT_CLASS = commandEventClass();

    static {
        Skript.registerExpression(ExprCommandSender.class, ServerPlayer.class,
                "[command['s]] (sender|executor)");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(COMMAND_EVENT_CLASS)) {
            Skript.error("The command sender expression can only be used in command events");
            return false;
        }
        return true;
    }

    @Override
    protected ServerPlayer @Nullable [] get(SkriptEvent event) {
        return event.player() == null ? new ServerPlayer[0] : new ServerPlayer[]{event.player()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ServerPlayer> getReturnType() {
        return ServerPlayer.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "command sender";
    }

    private static Class<?> commandEventClass() {
        try {
            return Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
