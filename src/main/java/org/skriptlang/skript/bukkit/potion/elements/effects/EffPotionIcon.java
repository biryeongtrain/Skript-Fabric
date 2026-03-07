package org.skriptlang.skript.bukkit.potion.elements.effects;

import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

public final class EffPotionIcon extends PotionPropertyEffect {

    @Override
    public void modify(SkriptPotionEffect effect, boolean isNegated) {
        effect.icon(!isNegated);
    }

    @Override
    public Type getPropertyType() {
        return Type.SHOW;
    }

    @Override
    public String getPropertyName() {
        return "icon";
    }
}
