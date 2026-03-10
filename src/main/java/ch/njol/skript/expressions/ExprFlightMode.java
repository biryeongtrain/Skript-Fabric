package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Flight Mode")
@Description("Whether the player(s) are allowed to fly. Use <a href=#EffMakeFly>Make Fly</a> effect to force player(s) to fly.")
@Example("set flight mode of player to true")
@Example("send \"%flying state of all players%\"")
@Since("2.2-dev34")
public class ExprFlightMode extends SimplePropertyExpression<ServerPlayer, Boolean> {

    static {
        register(ExprFlightMode.class, Boolean.class, "fl(y[ing]|ight) (mode|state)", "players");
    }

    @Override
    public Boolean convert(ServerPlayer player) {
        return player.getAbilities().mayfly;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET) {
            return new Class[]{Boolean.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        boolean state = mode != Changer.ChangeMode.RESET && delta != null && delta.length > 0 && (boolean) delta[0];
        for (ServerPlayer player : getExpr().getArray(event)) {
            player.getAbilities().mayfly = state;
        }
    }

    @Override
    protected String getPropertyName() {
        return "flight mode";
    }

    @Override
    public Class<Boolean> getReturnType() {
        return Boolean.class;
    }
}
