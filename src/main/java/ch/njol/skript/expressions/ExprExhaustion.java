package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import kim.biryeong.skriptFabric.mixin.FoodDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Exhaustion")
@Description("The exhaustion of a player. This is mainly used to determine the rate of hunger depletion.")
@Example("set exhaustion of all players to 1")
@Since("2.2-dev35")
public class ExprExhaustion extends SimplePropertyExpression<ServerPlayer, Number> {

    static {
        register(ExprExhaustion.class, Number.class, "exhaustion", "players");
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "exhaustion";
    }

    @Override
    @Nullable
    public Number convert(ServerPlayer player) {
        return ((FoodDataAccessor) (Object) player.getFoodData()).skript$getExhaustionLevel();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        float exhaustion = delta == null || delta.length == 0 ? 0 : ((Number) delta[0]).floatValue();
        for (ServerPlayer player : getExpr().getArray(event)) {
            FoodDataAccessor accessor = (FoodDataAccessor) (Object) player.getFoodData();
            float current = accessor.skript$getExhaustionLevel();
            switch (mode) {
                case ADD -> accessor.skript$setExhaustionLevel(current + exhaustion);
                case REMOVE -> accessor.skript$setExhaustionLevel(current - exhaustion);
                case SET -> accessor.skript$setExhaustionLevel(exhaustion);
                case DELETE, RESET -> accessor.skript$setExhaustionLevel(0);
            }
        }
    }
}
