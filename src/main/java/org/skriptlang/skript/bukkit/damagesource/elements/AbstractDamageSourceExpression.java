package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

abstract class AbstractDamageSourceExpression<T> extends SimpleExpression<T> {

    protected Expression<DamageSource> damageSources;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(DamageSource.class)) {
            return false;
        }
        damageSources = (Expression<DamageSource>) expressions[0];
        return true;
    }

    @Override
    protected T @Nullable [] get(SkriptEvent event) {
        List<T> values = new ArrayList<>();
        for (DamageSource damageSource : damageSources.getAll(event)) {
            T converted = convert(event, damageSource);
            if (converted != null) {
                values.add(converted);
            }
        }
        return values.toArray(createArray(values.size()));
    }

    protected @Nullable ServerLevel resolveLevel(SkriptEvent event, DamageSource damageSource) {
        Entity causingEntity = damageSource.getEntity();
        if (causingEntity != null && causingEntity.level() instanceof ServerLevel causingLevel) {
            return causingLevel;
        }

        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity != null && directEntity.level() instanceof ServerLevel directLevel) {
            return directLevel;
        }

        return event.level();
    }

    protected abstract @Nullable T convert(SkriptEvent event, DamageSource damageSource);

    protected abstract T[] createArray(int length);

    protected abstract String propertyName();

    @Override
    public boolean isSingle() {
        return damageSources.isSingle();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return propertyName() + " of " + damageSources.toString(event, debug);
    }
}
