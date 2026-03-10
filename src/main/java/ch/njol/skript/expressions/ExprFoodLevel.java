package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * @author Peter Guttiger
 */
@Name("Food Level")
@Description("The food level of a player from 0 to 10. Has several aliases: food/hunger level/meter/bar. ")
@Example("set the player's food level to 10")
@Since("1.0")
public class ExprFoodLevel extends PropertyExpression<ServerPlayer, Number> {

    static {
        register(ExprFoodLevel.class, Number.class, "(food|hunger)[[ ](level|met(er|re)|bar)]", "players");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        setExpr((Expression<ServerPlayer>) vars[0]);
        return true;
    }

    @Override
    protected Number[] get(SkriptEvent event, ServerPlayer[] source) {
        return get(source, player -> 0.5F * player.getFoodData().getFoodLevel());
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the food level of " + getExpr().toString(event, debug);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Number.class};
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int change = delta == null ? 0 : Math.round(((Number) delta[0]).floatValue() * 2);
        for (ServerPlayer player : getExpr().getArray(event)) {
            int food = player.getFoodData().getFoodLevel();
            switch (mode) {
                case SET, DELETE -> food = Math.max(0, Math.min(change, 20));
                case ADD -> food = Math.max(0, Math.min(food + change, 20));
                case REMOVE -> food = Math.max(0, Math.min(food - change, 20));
                case RESET -> food = 20;
            }
            player.getFoodData().setFoodLevel(food);
        }
    }
}
