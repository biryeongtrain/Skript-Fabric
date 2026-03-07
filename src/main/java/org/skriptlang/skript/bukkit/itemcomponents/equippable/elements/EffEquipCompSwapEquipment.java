package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffEquipCompSwapEquipment extends AbstractEquippableEffect {

    private boolean swappable;

    @Override
    protected boolean initPattern(int matchedPattern) {
        swappable = matchedPattern < 2;
        return true;
    }

    @Override
    protected void apply(EquippableWrapper wrapper) {
        wrapper.swappable(swappable);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (swappable ? "allow " : "prevent ") + values.toString(event, debug) + " equipment swapping";
    }
}
