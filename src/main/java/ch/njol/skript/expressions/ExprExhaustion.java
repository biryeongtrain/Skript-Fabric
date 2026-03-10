package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.lang.reflect.Field;

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
        return getExhaustion(player.getFoodData());
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        float exhaustion = delta == null || delta.length == 0 ? 0 : ((Number) delta[0]).floatValue();
        for (ServerPlayer player : getExpr().getArray(event)) {
            float current = getExhaustion(player.getFoodData());
            switch (mode) {
                case ADD -> setExhaustion(player.getFoodData(), current + exhaustion);
                case REMOVE -> setExhaustion(player.getFoodData(), current - exhaustion);
                case SET -> setExhaustion(player.getFoodData(), exhaustion);
                case DELETE, RESET -> setExhaustion(player.getFoodData(), 0);
            }
        }
    }

    private float getExhaustion(FoodData foodData) {
        try {
            Field field = FoodData.class.getDeclaredField("exhaustionLevel");
            field.setAccessible(true);
            return field.getFloat(foodData);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read exhaustion level", e);
        }
    }

    private void setExhaustion(FoodData foodData, float value) {
        try {
            Field field = FoodData.class.getDeclaredField("exhaustionLevel");
            field.setAccessible(true);
            field.setFloat(foodData, Math.max(0, value));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set exhaustion level", e);
        }
    }
}
