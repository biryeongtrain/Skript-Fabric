package ch.njol.skript.expressions.base;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionSection;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Expression type that can claim and manage a section body.
 */
public abstract class SectionExpression<Value> extends SimpleExpression<Value> {

    protected final ExpressionSection section = new ExpressionSection(this);

    public abstract boolean init(
            Expression<?>[] expressions,
            int pattern,
            Kleenean delayed,
            ParseResult result,
            @Nullable SectionNode node,
            @Nullable List<TriggerItem> triggerItems
    );

    @Override
    public final boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return section.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    public boolean isSectionOnly() {
        return false;
    }

    public final Section getAsSection() {
        return section;
    }

    protected final Trigger loadCode(
            SectionNode sectionNode,
            String name,
            @Nullable Runnable beforeLoading,
            @Nullable Runnable afterLoading,
            Class<?>... events
    ) {
        return section.loadCodeTask(sectionNode, name, beforeLoading, afterLoading, events);
    }

    protected void loadCode(SectionNode sectionNode) {
        section.loadCode(sectionNode);
    }

    protected void loadOptionalCode(SectionNode sectionNode) {
        section.loadOptionalCode(sectionNode);
    }

    protected void setTriggerItems(List<TriggerItem> items) {
        section.setTriggerItems(items);
    }

    protected boolean runSection(SkriptEvent event) {
        return section.runSection(event);
    }
}
