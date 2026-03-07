package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprSecPotionEffect extends SectionExpression<SkriptPotionEffect> {

    private @Nullable Expression<?> type;
    private @Nullable Expression<Number> amplifier;
    private @Nullable Expression<Timespan> duration;
    private boolean infinite;
    private boolean ambient;
    private @Nullable Expression<?> source;
    private @Nullable Trigger trigger;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
        type = expressions.length > 0 ? expressions[0] : null;
        if (matchedPattern == 3) {
            source = expressions.length > 1 ? expressions[1] : null;
        } else {
            amplifier = expressions.length > 1 ? (Expression<Number>) expressions[1] : null;
            duration = expressions.length > 2 ? (Expression<Timespan>) expressions[2] : null;
            infinite = matchedPattern == 1 || matchedPattern == 2;
            ambient = matchedPattern == 0 && result.expr != null && result.expr.toLowerCase(java.util.Locale.ENGLISH).contains("ambient");
        }
        if (node != null) {
            trigger = SectionUtils.loadLinkedCode(
                    "create potion effect",
                    (beforeLoading, afterLoading) -> loadCode(node, "create potion effect", beforeLoading, afterLoading, SkriptPotionEffect.class)
            );
            return trigger != null;
        }
        return true;
    }

    @Override
    protected SkriptPotionEffect @Nullable [] get(SkriptEvent event) {
        SkriptPotionEffect effect = source != null ? PotionEffectSupport.parsePotionEffect(source.getSingle(event)) : null;
        if (effect == null) {
            Holder<MobEffect> holder = PotionEffectSupport.parsePotionType(type != null ? type.getSingle(event) : null);
            if (holder == null) {
                return new SkriptPotionEffect[0];
            }
            effect = SkriptPotionEffect.fromType(holder);
        } else if (type != null) {
            Holder<MobEffect> holder = PotionEffectSupport.parsePotionType(type.getSingle(event));
            if (holder != null) {
                effect = new SkriptPotionEffect(holder, effect.amplifier(), effect.ambient(), effect.particles(), effect.icon(), effect.duration(), effect.infinite());
            }
        }
        if (ambient) {
            effect.ambient(true);
        }
        if (amplifier != null) {
            Number value = amplifier.getSingle(event);
            if (value != null) {
                effect.amplifier(Math.max(0, value.intValue() - 1));
            }
        }
        if (duration != null) {
            Timespan span = duration.getSingle(event);
            if (span != null) {
                effect.duration((int) span.getAs(TimePeriod.TICK));
            }
        } else if (infinite) {
            effect.infinite(true);
        }
        if (trigger != null) {
            SkriptEvent sectionEvent = new SkriptEvent(effect, event.server(), event.level(), event.player());
            Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
        }
        return new SkriptPotionEffect[]{effect};
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends SkriptPotionEffect> getReturnType() { return SkriptPotionEffect.class; }
    @Override public String toString(@Nullable SkriptEvent event, boolean debug) { return "potion effect"; }
}
