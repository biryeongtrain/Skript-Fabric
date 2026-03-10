package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBeaconEffects extends PropertyExpression<FabricBlock, Holder<MobEffect>> {

    static {
        registerDefault(ExprBeaconEffects.class, (Class) Holder.class, "(:primary|secondary) [beacon] effect", "blocks");
    }

    private boolean primary;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends FabricBlock>) expressions[0]);
        primary = parseResult.hasTag("primary");
        return true;
    }

    @Override
    protected Holder<MobEffect>[] get(SkriptEvent event, FabricBlock[] source) {
        List<Holder<MobEffect>> values = new ArrayList<>();
        for (FabricBlock block : source) {
            Holder<MobEffect> effect = effect(block);
            if (effect != null) {
                values.add(effect);
            }
        }
        return values.toArray(Holder[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{Object.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Object raw = delta == null ? null : delta[0];
        Holder<MobEffect> resolved = mode == ChangeMode.DELETE || mode == ChangeMode.RESET
                ? null
                : PotionEffectSupport.parsePotionType(raw);
        for (FabricBlock block : getExpr().getArray(event)) {
            BeaconBlockEntity beacon = beacon(block);
            if (beacon == null) {
                continue;
            }
            try {
                Field field = BeaconBlockEntity.class.getDeclaredField(primary ? "primaryPower" : "secondaryPower");
                field.setAccessible(true);
                field.set(beacon, resolved);
                beacon.setChanged();
            } catch (ReflectiveOperationException ignored) {
                return;
            }
        }
    }

    @Override
    public Class<? extends Holder<MobEffect>> getReturnType() {
        return (Class) Holder.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (primary ? "primary" : "secondary") + " beacon effect of " + getExpr().toString(event, debug);
    }

    private @Nullable Holder<MobEffect> effect(FabricBlock block) {
        BeaconBlockEntity beacon = beacon(block);
        if (beacon == null) {
            return null;
        }
        try {
            Field field = BeaconBlockEntity.class.getDeclaredField(primary ? "primaryPower" : "secondaryPower");
            field.setAccessible(true);
            return (Holder<MobEffect>) field.get(beacon);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private @Nullable BeaconBlockEntity beacon(FabricBlock block) {
        if (block.level() == null) {
            return null;
        }
        return block.level().getBlockEntity(block.position()) instanceof BeaconBlockEntity beacon ? beacon : null;
    }
}
