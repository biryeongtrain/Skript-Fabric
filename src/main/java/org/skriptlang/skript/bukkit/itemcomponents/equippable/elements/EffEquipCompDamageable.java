package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffEquipCompDamageable extends AbstractEquippableEffect {

    private boolean loseDurability;

    @Override
    protected boolean initPattern(int matchedPattern) {
        loseDurability = matchedPattern <= 1;
        return true;
    }

    @Override
    protected void apply(EquippableWrapper wrapper) {
        wrapper.damageOnHurt(loseDurability);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (loseDurability ? "allow " : "prevent ") + values.toString(event, debug) + " durability loss on injury";
    }
}
