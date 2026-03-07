package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceTypeSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprSecDamageSource extends SectionExpression<DamageSource> {

    private @Nullable Expression<?> typeExpression;
    private @Nullable Trigger trigger;

    @Override
    public boolean init(
            Expression<?>[] expressions,
            int pattern,
            Kleenean delayed,
            ParseResult result,
            @Nullable SectionNode node,
            @Nullable List<TriggerItem> triggerItems
    ) {
        if (expressions.length > 0) {
            typeExpression = expressions[0];
        }
        if (node != null) {
            trigger = SectionUtils.loadLinkedCode(
                    "custom damage source",
                    (beforeLoading, afterLoading) -> loadCode(node, "custom damage source", beforeLoading, afterLoading, DamageSourceSectionContext.class)
            );
            return trigger != null;
        }
        return true;
    }

    @Override
    protected DamageSource @Nullable [] get(SkriptEvent event) {
        DamageSourceSectionContext context = new DamageSourceSectionContext(event.level());
        if (typeExpression != null) {
            Object rawType = typeExpression.getSingle(event);
            Holder<DamageType> holder = DamageSourceTypeSupport.parseHolder(rawType, event.level());
            if (holder != null) {
                context.damageType(holder);
            }
        }
        if (trigger != null) {
            Variables.withLocalVariables(event, new SkriptEvent(context, event.server(), event.level(), event.player()), () ->
                    TriggerItem.walk(trigger, new SkriptEvent(context, event.server(), event.level(), event.player()))
            );
            if (context.causingEntity() != null && context.directEntity() == null) {
                Skript.error("You must set a direct entity when setting a causing entity.");
                return new DamageSource[0];
            }
        }
        return new DamageSource[]{context.build()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<DamageSource> getReturnType() {
        return DamageSource.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "a custom damage source";
    }
}
