package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.GameEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprGameEffectWithData extends SimpleExpression<GameEffect> {

    private @Nullable Expression<?> first;
    private boolean particles;
    private boolean potionBreak;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        first = expressions.length > 0 ? expressions[0] : null;
        potionBreak = matchedPattern == 0;
        particles = matchedPattern == 1;
        return true;
    }

    @Override
    protected GameEffect @Nullable [] get(SkriptEvent event) {
        GameEffect effect = new GameEffect(Identifier.withDefaultNamespace(potionBreak ? "potion_break" : "bone_meal_use"));
        if (first != null) {
            Object value = first.getSingle(event);
            if (potionBreak) {
                effect.data(ch.njol.skript.util.ColorRGB.parse(value));
            } else if (particles && value instanceof Number number) {
                effect.data(number.intValue());
            }
        }
        return new GameEffect[]{effect};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends GameEffect> getReturnType() {
        return GameEffect.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return potionBreak ? "potion break effect" : "bone meal effect";
    }
}
