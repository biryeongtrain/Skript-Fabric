package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;

@Name("Difficulty")
@Description("The difficulty of a world.")
@Example("difficulty of player's world")
@Since("1.0")
public class ExprDifficulty extends SimplePropertyExpression<ServerLevel, Difficulty> {

    static {
        register(ExprDifficulty.class, Difficulty.class, "difficult(y|ies)", "worlds");
    }

    @Override
    public Difficulty convert(ServerLevel world) {
        return world.getDifficulty();
    }

    @Override
    public Class<? extends Difficulty> getReturnType() {
        return Difficulty.class;
    }

    @Override
    protected String getPropertyName() {
        return "difficulty";
    }
}
