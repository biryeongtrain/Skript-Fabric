package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Game Mode")
@Description("The game mode of a player.")
@Example("player's game mode is survival")
@Example("set the player's game mode to creative")
@Since("1.0, 2.13.2 (fabric players)")
public class ExprGameMode extends SimplePropertyExpression<ServerPlayer, GameType> {

    static {
        register(ExprGameMode.class, GameType.class, "game[ ]mode", "players");
    }

    @Override
    public @Nullable GameType convert(ServerPlayer player) {
        return player.gameMode();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET || mode == ChangeMode.RESET
                ? new Class[]{GameType.class}
                : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        GameType gameMode = mode == ChangeMode.RESET || delta == null ? GameType.SURVIVAL : (GameType) delta[0];
        for (ServerPlayer player : getExpr().getArray(event)) {
            player.setGameMode(gameMode);
        }
    }

    @Override
    public Class<? extends GameType> getReturnType() {
        return GameType.class;
    }

    @Override
    protected String getPropertyName() {
        return "game mode";
    }
}
