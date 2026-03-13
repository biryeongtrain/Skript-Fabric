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
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Me")
@Description("The current player in command event contexts.")
@Example("send \"hello\" to me")
@Since("2.1.1, Fabric")
public final class ExprMe extends SimpleExpression<ServerPlayer> {

    private static final Class<?> COMMAND_EVENT_CLASS = commandEventClass();

    static {
        Skript.registerExpression(ExprMe.class, ServerPlayer.class, "me", "my[self]");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return getParser().isCurrentEvent(COMMAND_EVENT_CLASS);
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
        return "me";
    }

    private static Class<?> commandEventClass() {
        try {
            return Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
