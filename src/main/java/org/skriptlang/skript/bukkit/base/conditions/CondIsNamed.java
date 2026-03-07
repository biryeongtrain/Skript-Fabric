package org.skriptlang.skript.bukkit.base.conditions;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsNamed extends Condition {

    private enum Kind {
        ENTITY,
        ITEMSTACK
    }

    private Expression<?> target;
    private Kind kind;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }

        target = expressions[0];
        kind = switch (matchedPattern / 2) {
            case 0 -> Kind.ENTITY;
            case 1 -> Kind.ITEMSTACK;
            default -> null;
        };
        if (kind == null) {
            return false;
        }

        if (!accepts(kind, target)) {
            return false;
        }

        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        Object value = target.getSingle(event);
        if (value == null) {
            return false;
        }

        boolean named = switch (kind) {
            case ENTITY -> ((Entity) value).getCustomName() != null;
            case ITEMSTACK -> ((ItemStack) value).getCustomName() != null;
        };
        return isNegated() ? !named : named;
    }

    private boolean accepts(Kind kind, Expression<?> expression) {
        return switch (kind) {
            case ENTITY -> expression.canReturn(Entity.class);
            case ITEMSTACK -> expression.canReturn(ItemStack.class);
        };
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return target + (isNegated() ? " is not named" : " is named");
    }
}
