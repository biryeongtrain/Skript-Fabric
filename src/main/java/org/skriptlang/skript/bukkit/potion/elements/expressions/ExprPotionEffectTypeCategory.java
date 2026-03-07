package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprPotionEffectTypeCategory extends SimpleExpression<MobEffectCategory> {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        return true;
    }

    @Override
    protected MobEffectCategory @Nullable [] get(SkriptEvent event) {
        List<MobEffectCategory> categories = new ArrayList<>();
        for (Object value : values.getAll(event)) {
            Holder<MobEffect> effectType = PotionEffectSupport.parsePotionType(value);
            if (effectType != null) {
                categories.add(effectType.value().getCategory());
            }
        }
        return categories.toArray(MobEffectCategory[]::new);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    public Class<? extends MobEffectCategory> getReturnType() {
        return MobEffectCategory.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "potion effect type category of " + values.toString(event, debug);
    }
}
