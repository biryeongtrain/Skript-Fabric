package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Dummy section wrapper for {@link SectionExpression}.
 */
@ApiStatus.Internal
public class ExpressionSection extends Section {

    protected final SectionExpression<?> expression;

    public ExpressionSection(SectionExpression<?> expression) {
        this.expression = expression;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        SectionContext context = getParser().getData(SectionContext.class);
        if (context.sectionNode == null && expression.isSectionOnly()) {
            Skript.error("This expression requires a section.");
            return false;
        }

        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            boolean claimedSection = context.claim(this, parseResult.expr);
            if (claimedSection) {
                boolean init = expression.init(
                        expressions,
                        matchedPattern,
                        isDelayed,
                        parseResult,
                        context.sectionNode,
                        context.triggerItems
                );
                if (init) {
                    log.printLog();
                    return true;
                } else {
                    context.unclaim(this);
                    log.printError();
                    return false;
                }
            }
            if (expression.isSectionOnly() || context.owner instanceof ExpressionSection) {
                log.printError();
                return false;
            }
            log.clear();
        }
        return expression.init(expressions, matchedPattern, isDelayed, parseResult, null, null);
    }

    @Override
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        return expression.init(expressions, matchedPattern, isDelayed, parseResult, sectionNode, triggerItems);
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        return super.walk(event, false);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return expression.toString(event, debug);
    }

    public SectionExpression<?> getAsExpression() {
        return expression;
    }

    @Override
    public void loadCode(SectionNode sectionNode) {
        super.loadCode(sectionNode);
    }

    @Override
    public void loadOptionalCode(SectionNode sectionNode) {
        super.loadOptionalCode(sectionNode);
    }

    public boolean runSection(SkriptEvent event) {
        return first == null || TriggerItem.walk(first, event);
    }

    @Override
    public void setTriggerItems(List<TriggerItem> items) {
        super.setTriggerItems(items);
    }

    @SafeVarargs
    public final Trigger loadCodeTask(
            SectionNode sectionNode,
            String name,
            @Nullable Runnable beforeLoading,
            @Nullable Runnable afterLoading,
            Class<?>... events
    ) {
        return super.loadCode(sectionNode, name, beforeLoading, afterLoading, events);
    }
}
