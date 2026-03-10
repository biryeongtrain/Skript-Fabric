package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDifficulty extends SimplePropertyExpression<ServerLevel, Difficulty> {

    static {
        register(ExprDifficulty.class, Difficulty.class, "difficult(y|ies)", "worlds");
    }

    @Override
    public @Nullable Difficulty convert(ServerLevel world) {
        return world.getDifficulty();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Difficulty.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof Difficulty difficulty)) {
            return;
        }
        for (ServerLevel world : getExpr().getArray(event)) {
            world.getServer().setDifficulty(difficulty, true);
        }
    }

    @Override
    protected String getPropertyName() {
        return "difficulty";
    }

    @Override
    public Class<Difficulty> getReturnType() {
        return Difficulty.class;
    }
}
