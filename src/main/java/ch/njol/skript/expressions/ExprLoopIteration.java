package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Loop Iteration")
@Description("Returns the loop's current iteration count (for both normal and while loops).")
@Example("""
	while player is online:
		give player 1 stone
		wait 5 ticks
		if loop-counter > 30:
			stop loop
	""")
@Example("""
	loop {top-balances::*}:
		if loop-iteration <= 10:
			broadcast "#%loop-iteration% %loop-index% has $%loop-value%"
	""")
@Since("2.8.0")
public class ExprLoopIteration extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(ExprLoopIteration.class, Long.class,
                "[the] loop(-| )(counter|iteration)[-%-*number%]");
    }

    private LoopSection loop;
    private int loopNumber;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        loopNumber = -1;
        if (exprs[0] != null) {
            Number number = ((Literal<Number>) exprs[0]).getSingle(null);
            if (number != null) {
                loopNumber = number.intValue();
            }
        }

        int index = 1;
        LoopSection matchedLoop = null;
        for (LoopSection candidate : getParser().getCurrentSections(LoopSection.class)) {
            if (index < loopNumber) {
                index++;
                continue;
            }
            if (matchedLoop != null) {
                Skript.error("There are multiple loops. Use loop-iteration-1/2/3/etc. to specify which loop-iteration you want.");
                return false;
            }
            matchedLoop = candidate;
            if (index == loopNumber) {
                break;
            }
        }

        if (matchedLoop == null) {
            Skript.error("The loop iteration expression must be used in a loop");
            return false;
        }

        this.loop = matchedLoop;
        return true;
    }

    @Override
    protected Long[] get(SkriptEvent event) {
        return new Long[]{loop.getLoopCounter(event)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "loop-iteration" + (loopNumber != -1 ? ("-" + loopNumber) : "");
    }
}
