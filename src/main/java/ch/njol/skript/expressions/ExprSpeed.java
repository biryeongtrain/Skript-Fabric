package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Speed")
@Description({
        "A player's walking or flying speed. Both can be changed, but values must be between -1 and 1 (excessive values will be changed to -1 or 1 respectively). Negative values reverse directions.",
        "Please note that changing a player's speed will change their FOV just like potions do."
})
@Example("set the player's walk speed to 1")
@Example("increase the argument's fly speed by 0.1")
@Since("unknown (before 2.1)")
public class ExprSpeed extends SimplePropertyExpression<ServerPlayer, Number> {

    static {
        register(ExprSpeed.class, Number.class, "(0¦walk[ing]|1¦fl(y[ing]|ight))[( |-)]speed", "players");
    }

    private boolean walk;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        super.init(exprs, matchedPattern, isDelayed, parseResult);
        walk = parseResult.mark == 0;
        return true;
    }

    @Override
    public Number convert(ServerPlayer player) {
        return walk ? player.getAbilities().getWalkingSpeed() : player.getAbilities().getFlyingSpeed();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) throws UnsupportedOperationException {
        float input = delta == null ? 0 : ((Number) delta[0]).floatValue();
        for (ServerPlayer player : getExpr().getArray(event)) {
            float oldSpeed = walk ? player.getAbilities().getWalkingSpeed() : player.getAbilities().getFlyingSpeed();
            float newSpeed = switch (mode) {
                case SET -> input;
                case ADD -> oldSpeed + input;
                case REMOVE -> oldSpeed - input;
                default -> walk ? 0.1F : 0.05F;
            };
            float clamped = Math.clamp(newSpeed, -1.0F, 1.0F);
            if (walk) {
                player.getAbilities().setWalkingSpeed(clamped);
            } else {
                player.getAbilities().setFlyingSpeed(clamped);
            }
            player.onUpdateAbilities();
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return walk ? "walk speed" : "fly speed";
    }
}
