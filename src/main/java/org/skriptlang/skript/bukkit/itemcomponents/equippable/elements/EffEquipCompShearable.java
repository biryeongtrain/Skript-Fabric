package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffEquipCompShearable extends AbstractEquippableEffect {

    private boolean shearable;

    @Override
    protected boolean initPattern(int matchedPattern) {
        shearable = matchedPattern == 0;
        return true;
    }

    @Override
    protected void apply(EquippableWrapper wrapper) {
        wrapper.canBeSheared(shearable);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (shearable ? "allow " : "prevent ") + values.toString(event, debug) + " shearing";
    }
}
