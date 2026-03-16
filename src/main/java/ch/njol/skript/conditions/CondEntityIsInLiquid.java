package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.Method;

import kim.biryeong.skriptFabric.mixin.EntityAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;

@Name("Entity is in Liquid")
@Description("Checks whether an entity is in rain, lava, water or a bubble column.")
@Example("if player is in rain:")
@Example("if player is in water:")
@Example("player is in lava:")
@Example("player is in bubble column")
@Since("2.6.1")
public class CondEntityIsInLiquid extends PropertyCondition<Entity> {

    private static final int IN_WATER = 1;
    private static final int IN_LAVA = 2;
    private static final int IN_BUBBLE_COLUMN = 3;
    private static final int IN_RAIN = 4;

    static {
        register(CondEntityIsInLiquid.class, "in (1¦water|2¦lava|3¦[a] bubble[ ]column|4¦rain)", "entities");
    }

    private int mark;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends Entity>) exprs[0]);
        setNegated(matchedPattern == 1);
        mark = parseResult.mark;
        return true;
    }

    @Override
    public boolean check(Entity entity) {
        return switch (mark) {
            case IN_WATER -> entity.isInWater();
            case IN_LAVA -> entity.isInLava();
            case IN_BUBBLE_COLUMN -> isInBubbleColumn(entity);
            case IN_RAIN -> isInRain(entity);
            default -> throw new IllegalStateException();
        };
    }

    @Override
    protected String getPropertyName() {
        return switch (mark) {
            case IN_WATER -> "in water";
            case IN_LAVA -> "in lava";
            case IN_BUBBLE_COLUMN -> "in bubble column";
            case IN_RAIN -> "in rain";
            default -> throw new IllegalStateException();
        };
    }

    private static boolean isInBubbleColumn(Entity entity) {
        return entity.level().getBlockState(entity.blockPosition()).is(Blocks.BUBBLE_COLUMN)
                || entity.level().getBlockState(entity.blockPosition().above()).is(Blocks.BUBBLE_COLUMN);
    }

    private static boolean isInRain(Entity entity) {
        return ((EntityAccessor) entity).callIsInRain();
    }
}
