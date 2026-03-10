package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDirection extends SimpleExpression<Direction> {

    static {
        Skript.registerExpression(ExprDirection.class, Direction.class,
                "[%-number% [(block|met(er|re))[s]] [to the]] (0¦north|1¦south|2¦east|3¦west|4¦above|4¦up|5¦down|5¦below)",
                "[%-number% [(block|met(er|re))[s]]] in [the] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) of %entity/block%",
                "[%-number% [(block|met(er|re))[s]]] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|[to the] (1¦right|-1¦left) [of])",
                "[%-number% [(block|met(er|re))[s]]] horizontal[ly] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|to the (1¦right|-1¦left) [of])");
    }

    @Nullable Expression<Number> amount;
    @Nullable private net.minecraft.core.Direction cardinal;
    @Nullable private Expression<?> relativeTo;
    private boolean horizontal;
    private boolean facing;
    private double yaw;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        amount = (Expression<Number>) exprs[0];
        switch (matchedPattern) {
            case 0 -> cardinal = switch (parseResult.mark) {
                case 0 -> net.minecraft.core.Direction.NORTH;
                case 1 -> net.minecraft.core.Direction.SOUTH;
                case 2 -> net.minecraft.core.Direction.EAST;
                case 3 -> net.minecraft.core.Direction.WEST;
                case 4 -> net.minecraft.core.Direction.UP;
                default -> net.minecraft.core.Direction.DOWN;
            };
            case 1 -> {
                relativeTo = exprs[1];
                horizontal = parseResult.mark % 2 != 0;
                facing = parseResult.mark >= 2;
            }
            case 2, 3 -> {
                yaw = Math.PI / 2 * parseResult.mark;
                horizontal = matchedPattern == 3;
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Direction @Nullable [] get(SkriptEvent event) {
        Number number = amount != null ? amount.getSingle(event) : 1;
        if (number == null) {
            return new Direction[0];
        }
        double length = number.doubleValue();
        if (cardinal != null) {
            return new Direction[]{new Direction(cardinal, length)};
        }
        if (relativeTo != null) {
            Object origin = relativeTo.getSingle(event);
            if (origin == null) {
                return new Direction[0];
            }
            if (origin instanceof FabricBlock block) {
                net.minecraft.core.Direction resolved = Direction.getFacing(block);
                if (resolved == null) {
                    return new Direction[]{Direction.ZERO};
                }
                if (horizontal && resolved.getAxis().isVertical()) {
                    return new Direction[]{Direction.ZERO};
                }
                return new Direction[]{new Direction(resolved, length)};
            }
            Entity entity = (Entity) origin;
            if (!horizontal && !facing) {
                return new Direction[]{new Direction(entity.getLookAngle().normalize().scale(length))};
            }
            net.minecraft.core.Direction resolved = Direction.getFacing(entity, horizontal || facing);
            if (!horizontal && facing && resolved.getAxis().isVertical()) {
                return new Direction[]{new Direction(resolved, length)};
            }
            if (resolved.getAxis().isVertical()) {
                return new Direction[]{Direction.ZERO};
            }
            return new Direction[]{new Direction(resolved, length)};
        }
        return new Direction[]{new Direction(horizontal ? Direction.IGNORE_PITCH : 0.0, yaw, length)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Direction> getReturnType() {
        return Direction.class;
    }
}
