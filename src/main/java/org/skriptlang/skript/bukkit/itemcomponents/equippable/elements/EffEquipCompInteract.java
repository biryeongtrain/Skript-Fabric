package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffEquipCompInteract extends AbstractEquippableEffect {

    private boolean equip;

    @Override
    protected boolean initPattern(int matchedPattern) {
        equip = matchedPattern < 3;
        return true;
    }

    @Override
    protected void apply(EquippableWrapper wrapper) {
        wrapper.equipOnInteract(equip);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (equip ? "allow " : "prevent ") + values.toString(event, debug) + " entity equipping";
    }
}
