package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.equipment.Equippable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondEquipCompInteract extends Condition {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return values.check(event, value -> {
            Equippable equippable = EquippableSupport.getEquippable(value);
            return equippable != null && equippable.equipOnInteract();
        }, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return values.toString(event, debug) + (isNegated() ? " cannot" : " can") + " be equipped onto entities";
    }
}
