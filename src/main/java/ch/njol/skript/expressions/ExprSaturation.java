package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Saturation")
@Description("The saturation of a player.")
@Example("set saturation of player to 20")
@Since("2.2-Fixes-v10, 2.2-dev35 (fully modifiable), 2.13.2 (fabric players)")
public class ExprSaturation extends SimplePropertyExpression<ServerPlayer, Number> {

    static {
        register(ExprSaturation.class, Number.class, "saturation", "players");
    }

    @Override
    public @Nullable Number convert(ServerPlayer player) {
        return player.getFoodData().getSaturationLevel();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.DELETE || mode == ChangeMode.RESET || mode == ChangeMode.SET
                || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE
                ? new Class[]{Number.class}
                : null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        float value = delta == null ? 0.0F : ((Number) delta[0]).floatValue();
        for (ServerPlayer player : getExpr().getArray(event)) {
            float current = player.getFoodData().getSaturationLevel();
            float next = switch (mode) {
                case ADD -> current + value;
                case REMOVE -> current - value;
                case SET -> value;
                case DELETE, RESET -> 0.0F;
                default -> current;
            };
            player.getFoodData().setSaturation(Math.max(0.0F, next));
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "saturation";
    }
}
