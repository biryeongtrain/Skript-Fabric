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

@Name("Tamer")
@Description("The player that tamed an entity in an entity tame style event handle.")
@Example("""
    on tame:
        send "%tamer%" to console
    """)
@Since("2.2-dev25")
public final class ExprTamer extends SimpleExpression<ServerPlayer> {

    static {
        Skript.registerExpression(ExprTamer.class, ServerPlayer.class, "[the] tamer");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!ReflectiveHandleAccess.currentEventSupports("owner", "getOwner", "tamer", "getTamer")) {
            Skript.error("The expression 'tamer' may only be used in the entity tame event.");
            return false;
        }
        return expressions.length == 0;
    }

    @Override
    protected ServerPlayer @Nullable [] get(SkriptEvent event) {
        Object value = ReflectiveHandleAccess.invokeNoArg(event.handle(), "owner", "getOwner", "tamer", "getTamer");
        return value instanceof ServerPlayer player ? new ServerPlayer[]{player} : null;
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
        return "tamer";
    }
}
