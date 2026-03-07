package org.skriptlang.skript.bukkit.damagesource.elements;

import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFoodExhaustion extends AbstractDamageSourceExpression<Float> {

    @Override
    protected @Nullable Float convert(SkriptEvent event, DamageSource damageSource) {
        return damageSource.getFoodExhaustion();
    }

    @Override
    protected Float[] createArray(int length) {
        return new Float[length];
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    protected String propertyName() {
        return "food exhaustion";
    }
}
