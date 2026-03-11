package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.PrivateBeaconAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBeaconRange extends SimplePropertyExpression<FabricBlock, Double> {

    static {
        register(ExprBeaconRange.class, Double.class, "beacon [effect] range", "blocks");
    }

    @Override
    public @Nullable Double convert(FabricBlock block) {
        Integer levels = levels(block);
        if (levels == null) {
            return null;
        }
        return 10.0 + Math.max(0, levels) * 10.0;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        double change = delta == null ? 0.0 : ((Number) delta[0]).doubleValue();
        for (FabricBlock block : getExpr().getArray(event)) {
            BeaconBlockEntity beacon = beacon(block);
            Integer levels = levels(block);
            if (beacon == null || levels == null) {
                continue;
            }
            double current = 10.0 + Math.max(0, levels) * 10.0;
            double updated = switch (mode) {
                case SET -> change;
                case ADD -> current + change;
                case REMOVE -> current - change;
                case RESET -> 10.0 + Math.max(0, levels) * 10.0;
                default -> current;
            };
            setLevels(beacon, (int) Math2.fit(0, Math.round((Math.max(0.0, updated) - 10.0) / 10.0), 4));
        }
    }

    @Override
    public Class<? extends Double> getReturnType() {
        return Double.class;
    }

    @Override
    protected String getPropertyName() {
        return "beacon range";
    }

    private @Nullable BeaconBlockEntity beacon(FabricBlock block) {
        if (block.level() == null) {
            return null;
        }
        return block.level().getBlockEntity(block.position()) instanceof BeaconBlockEntity beacon ? beacon : null;
    }

    private @Nullable Integer levels(FabricBlock block) {
        BeaconBlockEntity beacon = beacon(block);
        return beacon == null ? null : PrivateBeaconAccess.levels(beacon);
    }

    private void setLevels(BeaconBlockEntity beacon, int levels) {
        PrivateBeaconAccess.setLevels(beacon, levels);
        beacon.setChanged();
    }
}
