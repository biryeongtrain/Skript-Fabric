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

@Name("Whitelist")
@Description("The server whitelist entries on the current Fabric compatibility surface.")
@Example("add player to whitelist")
@Example("set whitelist to false")
@Since("2.5.2, Fabric")
public class ExprWhitelist extends SimpleExpression<GameProfile> {

    static {
        ch.njol.skript.Skript.registerExpression(ExprWhitelist.class, GameProfile.class, "[the] white[ ]list");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected GameProfile[] get(SkriptEvent event) {
        var server = ExpressionRuntimeSupport.resolveServer(event);
        return server == null ? new GameProfile[0] : ExpressionRuntimeSupport.whitelist(server);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE -> new Class[]{GameProfile.class};
            case DELETE, RESET, SET -> new Class[]{Boolean.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        var server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null) {
            return;
        }
        switch (mode) {
            case SET -> {
                if (delta == null || delta.length == 0 || !(delta[0] instanceof Boolean enabled)) {
                    return;
                }
                ExpressionRuntimeSupport.setWhitelistEnabled(server, enabled);
            }
            case ADD -> {
                if (delta == null) {
                    return;
                }
                for (Object value : delta) {
                    if (value instanceof GameProfile profile) {
                        ExpressionRuntimeSupport.addWhitelist(server, profile);
                    }
                }
            }
            case REMOVE -> {
                if (delta == null) {
                    return;
                }
                for (Object value : delta) {
                    if (value instanceof GameProfile profile) {
                        ExpressionRuntimeSupport.removeWhitelist(server, profile);
                    }
                }
            }
            case DELETE, RESET -> ExpressionRuntimeSupport.clearWhitelist(server);
            default -> {
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
        return "whitelist";
    }
}
