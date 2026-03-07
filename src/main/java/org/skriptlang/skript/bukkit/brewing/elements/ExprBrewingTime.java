package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.PrivateBlockEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprBrewingTime extends SimpleExpression<Timespan> {

    private Expression<FabricBlock> blocks;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(FabricBlock.class)) {
            return false;
        }
        blocks = (Expression<FabricBlock>) expressions[0];
        return true;
    }

    @Override
    protected Timespan @Nullable [] get(SkriptEvent event) {
        List<Timespan> values = new ArrayList<>();
        for (FabricBlock block : blocks.getAll(event)) {
            if (block.level().getBlockEntity(block.position()) instanceof BrewingStandBlockEntity brewingStand) {
                values.add(new Timespan(TimePeriod.TICK, PrivateBlockEntityAccess.brewingTime(brewingStand)));
            }
        }
        return values.toArray(Timespan[]::new);
    }

    @Override
    public boolean isSingle() {
        return blocks.isSingle();
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Timespan.class, Number.class, String.class};
            case RESET, DELETE -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        long rawTicks = resolveTicks(delta);
        for (FabricBlock block : blocks.getAll(event)) {
            if (!(block.level().getBlockEntity(block.position()) instanceof BrewingStandBlockEntity brewingStand)) {
                continue;
            }
            int current = PrivateBlockEntityAccess.brewingTime(brewingStand);
            int next = switch (mode) {
                case SET -> clampTicks(rawTicks);
                case ADD -> clampTicks((long) current + rawTicks);
                case REMOVE -> clampTicks((long) current - rawTicks);
                case RESET, DELETE -> 0;
                default -> current;
            };
            if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE
                    || mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
                PrivateBlockEntityAccess.setBrewingTime(brewingStand, next);
                brewingStand.setChanged();
            }
        }
    }

    private long resolveTicks(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return 0L;
        }
        Object value = delta[0];
        if (value instanceof Timespan timespan) {
            return timespan.getAs(TimePeriod.TICK);
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string) {
            Timespan parsed = Classes.parse(string, Timespan.class, ParseContext.DEFAULT);
            if (parsed != null) {
                return parsed.getAs(TimePeriod.TICK);
            }
        }
        return 0L;
    }

    private int clampTicks(long value) {
        if (value <= 0L) {
            return 0;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "brewing time of " + blocks.toString(event, debug);
    }
}
