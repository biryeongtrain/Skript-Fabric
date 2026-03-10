package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Block Redstone Powered")
@Description("Checks if a block is indirectly or directly powered by redstone")
@Example("""
    if clicked block is redstone powered:
        send "This block is well-powered by redstone!"
    """)
@Example("""
    if clicked block is indirectly redstone powered:
        send "This block is indirectly redstone powered."
    """)
@Since("2.5")
public class CondIsBlockRedstonePowered extends Condition {

    static {
        Skript.registerCondition(CondIsBlockRedstonePowered.class,
                "%blocks% (is|are) redstone powered",
                "%blocks% (is|are) indirectly redstone powered",
                "%blocks% (is|are)(n't| not) redstone powered",
                "%blocks% (is|are)(n't| not) indirectly redstone powered");
    }

    private Expression<FabricBlock> blocks;
    private boolean indirectlyPowered;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        blocks = (Expression<FabricBlock>) expressions[0];
        indirectlyPowered = matchedPattern % 2 == 1;
        setNegated(matchedPattern > 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return indirectlyPowered
                ? blocks.check(event, block -> block.level().getDirectSignalTo(block.position()) > 0, isNegated())
                : blocks.check(event, block -> block.level().hasNeighborSignal(block.position()), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(
                this,
                PropertyCondition.PropertyType.BE,
                event,
                debug,
                blocks,
                (indirectlyPowered ? "indirectly " : "") + "powered"
        );
    }
}
