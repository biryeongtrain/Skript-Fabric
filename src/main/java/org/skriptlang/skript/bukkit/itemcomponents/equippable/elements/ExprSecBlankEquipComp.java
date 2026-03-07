package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprSecBlankEquipComp extends SectionExpression<EquippableWrapper> {

    private @Nullable Trigger trigger;

    @Override
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
        if (expressions.length != 0) {
            return false;
        }
        if (node != null) {
            trigger = SectionUtils.loadLinkedCode(
                    "blank equippable component",
                    (beforeLoading, afterLoading) -> loadCode(node, "blank equippable component", beforeLoading, afterLoading, EquippableWrapper.class)
            );
            return trigger != null;
        }
        return true;
    }

    @Override
    protected EquippableWrapper @Nullable [] get(SkriptEvent event) {
        EquippableWrapper wrapper = new EquippableWrapper((net.minecraft.world.item.equipment.Equippable) null);
        if (trigger != null) {
            SkriptEvent sectionEvent = new SkriptEvent(wrapper, event.server(), event.level(), event.player());
            Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
        }
        return new EquippableWrapper[]{wrapper};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends EquippableWrapper> getReturnType() {
        return EquippableWrapper.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "a blank equippable component";
    }
}
