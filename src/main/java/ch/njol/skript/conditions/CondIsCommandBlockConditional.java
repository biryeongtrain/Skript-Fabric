package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.block.CommandBlock;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Is Conditional")
@Description("Checks whether a command block is conditional or not.")
@Example("""
    if {_block} is conditional:
        make {_block} unconditional
    """)
@Since("2.10")
public class CondIsCommandBlockConditional extends PropertyCondition<FabricBlock> {

    static {
        register(CondIsCommandBlockConditional.class, "[:un]conditional", "blocks");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<FabricBlock>) expressions[0]);
        setNegated(parseResult.hasTag("un") ^ matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(FabricBlock block) {
        return block.block() instanceof CommandBlock
                && Boolean.TRUE.equals(block.state().getOptionalValue(CommandBlock.CONDITIONAL).orElse(false));
    }

    @Override
    protected String getPropertyName() {
        return "conditional";
    }
}
