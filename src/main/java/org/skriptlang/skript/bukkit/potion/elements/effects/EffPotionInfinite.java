package org.skriptlang.skript.bukkit.potion.elements.effects;

import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

public final class EffPotionInfinite extends PotionPropertyEffect {

    @Override
    public void modify(SkriptPotionEffect effect, boolean isNegated) {
        effect.infinite(!isNegated);
    }

    @Override
    public Type getPropertyType() {
        return Type.MAKE;
    }

    @Override
    public String getPropertyName() {
        return "infinite";
    }
}
