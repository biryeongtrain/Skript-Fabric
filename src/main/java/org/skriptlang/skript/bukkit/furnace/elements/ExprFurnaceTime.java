package org.skriptlang.skript.bukkit.furnace.elements;

import ch.njol.skript.Skript;
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
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.PrivateFurnaceAccess;
import org.skriptlang.skript.fabric.runtime.FabricFurnaceEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFurnaceTime extends SimpleExpression<Timespan> {

    private enum Type {
        COOK,
        TOTAL_COOK,
        BURN
    }

    private Type type;
    private @Nullable Expression<FabricBlock> blocks;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        type = switch (matchedPattern) {
            case 0, 1, 2, 3 -> Type.COOK;
            case 4, 5, 6, 7 -> Type.TOTAL_COOK;
            case 8, 9 -> Type.BURN;
            default -> Type.COOK;
        };
        if (expressions.length == 0 || expressions[0] == null) {
            if (!getParser().isCurrentEvent(FabricFurnaceEventHandle.class)) {
                Skript.error("The event-only furnace time syntax can only be used in furnace events.");
                return false;
            }
            blocks = null;
            return true;
        }
        if (expressions.length != 1 || !expressions[0].canReturn(FabricBlock.class)) {
            return false;
        }
        blocks = (Expression<FabricBlock>) expressions[0];
        return true;
    }

    @Override
    protected Timespan @Nullable [] get(SkriptEvent event) {
        List<Timespan> results = new ArrayList<>();
        if (blocks == null) {
            if (event.handle() instanceof FabricFurnaceEventHandle handle) {
                results.add(new Timespan(TimePeriod.TICK, value(handle)));
            }
            return results.toArray(Timespan[]::new);
        }
        for (FabricBlock block : blocks.getAll(event)) {
            if (block.level().getBlockEntity(block.position()) instanceof AbstractFurnaceBlockEntity furnace) {
                results.add(new Timespan(TimePeriod.TICK, value(furnace)));
            }
        }
        return results.toArray(Timespan[]::new);
    }

    private int value(FabricFurnaceEventHandle handle) {
        return switch (type) {
            case COOK -> PrivateFurnaceAccess.cookingTimer(handle.furnace());
            case TOTAL_COOK -> handle.totalCookTime() > 0 ? handle.totalCookTime() : PrivateFurnaceAccess.cookingTotalTime(handle.furnace());
            case BURN -> handle.burnTime() > 0 ? handle.burnTime() : PrivateFurnaceAccess.litTimeRemaining(handle.furnace());
        };
    }

    private int value(AbstractFurnaceBlockEntity furnace) {
        return switch (type) {
            case COOK -> PrivateFurnaceAccess.cookingTimer(furnace);
            case TOTAL_COOK -> PrivateFurnaceAccess.cookingTotalTime(furnace);
            case BURN -> PrivateFurnaceAccess.litTimeRemaining(furnace);
        };
    }

    @Override
    public boolean isSingle() {
        return blocks == null || blocks.isSingle();
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
        int amount = resolveTicks(delta);
        if (blocks == null) {
            if (event.handle() instanceof FabricFurnaceEventHandle handle) {
                apply(handle.furnace(), amount, mode);
            }
            return;
        }
        for (FabricBlock block : blocks.getAll(event)) {
            if (block.level().getBlockEntity(block.position()) instanceof AbstractFurnaceBlockEntity furnace) {
                apply(furnace, amount, mode);
                furnace.setChanged();
            }
        }
    }

    private void apply(AbstractFurnaceBlockEntity furnace, int amount, ChangeMode mode) {
        int current = value(furnace);
        int next = switch (mode) {
            case SET -> amount;
            case ADD -> Math.max(0, current + amount);
            case REMOVE -> Math.max(0, current - amount);
            case RESET, DELETE -> 0;
            default -> current;
        };
        switch (type) {
            case COOK -> PrivateFurnaceAccess.setCookingTimer(furnace, next);
            case TOTAL_COOK -> PrivateFurnaceAccess.setCookingTotalTime(furnace, next);
            case BURN -> {
                PrivateFurnaceAccess.setLitTimeRemaining(furnace, next);
                if (mode == ChangeMode.SET) {
                    PrivateFurnaceAccess.setLitTotalTime(furnace, next);
                }
            }
        }
    }

    private int resolveTicks(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return 0;
        }
        Object value = delta[0];
        if (value instanceof Timespan timespan) {
            return (int) timespan.getAs(TimePeriod.TICK);
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            Timespan parsed = Classes.parse(string, Timespan.class, ParseContext.DEFAULT);
            if (parsed != null) {
                return (int) parsed.getAs(TimePeriod.TICK);
            }
        }
        return 0;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (type) {
            case COOK -> "cook time";
            case TOTAL_COOK -> "total cook time";
            case BURN -> "fuel burn time";
        };
    }
}
