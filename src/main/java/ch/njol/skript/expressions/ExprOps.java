package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("All Operators")
@Description("The list of operators, or online non-operators, on the current Fabric compatibility surface.")
@Example("set {_ops::*} to all operators")
@Since("2.7, Fabric")
public class ExprOps extends SimpleExpression<GameProfile> {

    private boolean nonOps;

    static {
        ch.njol.skript.Skript.registerExpression(
                ExprOps.class,
                GameProfile.class,
                "[all [[of] the]|the] [server] [:non(-| )]op[erator]s"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        nonOps = parseResult.hasTag("non");
        return true;
    }

    @Override
    protected GameProfile[] get(SkriptEvent event) {
        var server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null) {
            return new GameProfile[0];
        }
        return nonOps ? ExpressionRuntimeSupport.nonOperators(server) : ExpressionRuntimeSupport.operators(server);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (nonOps) {
            return null;
        }
        return switch (mode) {
            case ADD, SET, REMOVE, RESET, DELETE -> new Class[]{GameProfile.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        var server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null) {
            return;
        }
        switch (mode) {
            case SET -> {
                clearOperators(server);
                applyOperators(server, delta, true);
            }
            case ADD -> applyOperators(server, delta, true);
            case REMOVE -> applyOperators(server, delta, false);
            case DELETE, RESET -> clearOperators(server);
            default -> {
            }
        }
    }

    private static void clearOperators(net.minecraft.server.MinecraftServer server) {
        for (GameProfile profile : ExpressionRuntimeSupport.operators(server)) {
            ExpressionRuntimeSupport.deop(server, profile);
        }
    }

    private static void applyOperators(net.minecraft.server.MinecraftServer server, @Nullable Object[] delta, boolean operator) {
        if (delta == null) {
            return;
        }
        for (Object value : delta) {
            if (!(value instanceof GameProfile profile)) {
                continue;
            }
            if (operator) {
                ExpressionRuntimeSupport.op(server, profile);
            } else {
                ExpressionRuntimeSupport.deop(server, profile);
            }
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends GameProfile> getReturnType() {
        return GameProfile.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return nonOps ? "all non-operators" : "all operators";
    }
}
