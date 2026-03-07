package org.skriptlang.skript.bukkit.loottables.elements.expressions;

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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprSecCreateLootContext extends SectionExpression<LootContextWrapper> {

    private Expression<FabricLocation> location;
    private @Nullable Trigger trigger;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
        if (expressions.length != 1 || !expressions[0].canReturn(FabricLocation.class)) {
            return false;
        }
        location = (Expression<FabricLocation>) expressions[0];
        if (node != null) {
            trigger = SectionUtils.loadLinkedCode(
                    "create loot context",
                    (beforeLoading, afterLoading) -> loadCode(node, "create loot context", beforeLoading, afterLoading, LootContextWrapper.class)
            );
            return trigger != null;
        }
        return true;
    }

    @Override
    protected LootContextWrapper @Nullable [] get(SkriptEvent event) {
        FabricLocation source = location.getSingle(event);
        if (source == null) {
            return new LootContextWrapper[0];
        }
        LootContextWrapper wrapper = new LootContextWrapper(source);
        if (trigger != null) {
            SkriptEvent sectionEvent = new SkriptEvent(wrapper, event.server(), event.level(), event.player());
            Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
        }
        return new LootContextWrapper[]{wrapper};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends LootContextWrapper> getReturnType() {
        return LootContextWrapper.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "loot context at " + location.toString(event, debug);
    }
}
