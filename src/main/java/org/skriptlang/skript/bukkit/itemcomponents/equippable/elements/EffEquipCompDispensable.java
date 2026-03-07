package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffEquipCompDispensable extends AbstractEquippableEffect {

    private boolean dispensable;

    @Override
    protected boolean initPattern(int matchedPattern) {
        dispensable = matchedPattern < 3;
        return true;
    }

    @Override
    protected void apply(EquippableWrapper wrapper) {
        wrapper.dispensable(dispensable);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (dispensable ? "allow " : "prevent ") + values.toString(event, debug) + " from being dispensed";
    }
}
