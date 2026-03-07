package org.skriptlang.skript.bukkit.damagesource.elements;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDirectEntity extends AbstractDamageSourceExpression<Entity> {

    @Override
    protected @Nullable Entity convert(SkriptEvent event, DamageSource damageSource) {
        return damageSource.getDirectEntity();
    }

    @Override
    protected Entity[] createArray(int length) {
        return new Entity[length];
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    protected String propertyName() {
        return "direct entity";
    }
}
