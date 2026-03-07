package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricBreedingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprBreedingFamily extends SimpleExpression<LivingEntity> {

    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricBreedingEventHandle.class)) {
            Skript.error("The 'breeding family' expression can only be used in a breeding event.");
            return false;
        }
        pattern = matchedPattern;
        return true;
    }

    @Override
    protected LivingEntity @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricBreedingEventHandle handle)) {
            return new LivingEntity[0];
        }
        LivingEntity value = switch (pattern) {
            case 0 -> handle.mother();
            case 1 -> handle.father();
            case 2 -> handle.offspring();
            default -> handle.breeder();
        };
        return value == null ? new LivingEntity[0] : new LivingEntity[]{value};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends LivingEntity> getReturnType() {
        return LivingEntity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (pattern) {
            case 0 -> "breeding mother";
            case 1 -> "breeding father";
            case 2 -> "bred offspring";
            default -> "breeder";
        };
    }
}
