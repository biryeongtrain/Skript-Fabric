package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ExpressionSectionCompatibilityTest {

    @Test
    void sectionOnlyExpressionWithoutSectionNodeFailsInit() {
        DummySectionExpression expression = new DummySectionExpression(true);
        ExpressionSection section = new ExpressionSection(expression);

        ParserInstance parser = ParserInstance.get();
        Section.SectionContext context = parser.getData(Section.SectionContext.class);
        context.modify(null, null, () -> {
            boolean ok = section.init(new Expression<?>[0], 0, Kleenean.FALSE, parseResult("dummy"));
            assertFalse(ok);
            return null;
        });
        assertEquals(0, expression.initCalls);
    }

    @Test
    void nonSectionOnlyExpressionWithoutSectionNodeStillInitializes() {
        DummySectionExpression expression = new DummySectionExpression(false);
        ExpressionSection section = new ExpressionSection(expression);

        ParserInstance parser = ParserInstance.get();
        Section.SectionContext context = parser.getData(Section.SectionContext.class);
        context.modify(null, null, () -> {
            boolean ok = section.init(new Expression<?>[0], 0, Kleenean.FALSE, parseResult("dummy"));
            assertTrue(ok);
            return null;
        });
        assertEquals(1, expression.initCalls);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static class DummySectionExpression extends SectionExpression<Object> {

        private final boolean sectionOnly;
        private int initCalls;

        private DummySectionExpression(boolean sectionOnly) {
            this.sectionOnly = sectionOnly;
        }

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int pattern,
                Kleenean delayed,
                SkriptParser.ParseResult result,
                @Nullable ch.njol.skript.config.SectionNode node,
                @Nullable List<TriggerItem> triggerItems
        ) {
            initCalls++;
            return true;
        }

        @Override
        public boolean isSectionOnly() {
            return sectionOnly;
        }

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return new Object[0];
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "dummy-section-expression";
        }
    }
}
