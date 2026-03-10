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
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Beehive Honey Level")
@Description({
        "The current or max honey level of a beehive.",
        "The max level is 5, which cannot be changed."
})
@Example("set the honey level of {_beehive} to the max honey level of {_beehive}")
@Since("2.11")
public class ExprBeehiveHoneyLevel extends SimplePropertyExpression<FabricBlock, Integer> {

    static {
        registerDefault(ExprBeehiveHoneyLevel.class, Integer.class, "[max:max[imum]] honey level", "blocks");
    }

    private boolean isMax;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        isMax = parseResult.hasTag("max");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Integer convert(FabricBlock block) {
        return honeyLevel(block.state(), isMax);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (isMax) {
            return null;
        }
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Integer.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int value = delta == null ? 0 : ((Number) delta[0]).intValue();
        for (FabricBlock block : getExpr().getArray(event)) {
            BlockState state = block.state();
            if (!state.hasProperty(BeehiveBlock.HONEY_LEVEL)) {
                continue;
            }
            int current = state.getValue(BeehiveBlock.HONEY_LEVEL);
            int next = switch (mode) {
                case SET -> value;
                case ADD -> current + value;
                case REMOVE -> current - value;
                default -> current;
            };
            block.level().setBlock(
                    block.position(),
                    state.setValue(BeehiveBlock.HONEY_LEVEL, Math.max(0, Math.min(next, BeehiveBlock.MAX_HONEY_LEVELS))),
                    3
            );
        }
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return (isMax ? "maximum " : "") + "honey level";
    }

    static @Nullable Integer honeyLevel(BlockState state, boolean max) {
        if (!state.hasProperty(BeehiveBlock.HONEY_LEVEL)) {
            return null;
        }
        return max ? BeehiveBlock.MAX_HONEY_LEVELS : state.getValue(BeehiveBlock.HONEY_LEVEL);
    }
}
