package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Is Passable")
@Description({
        "Checks whether a block is passable.",
        "A block is passable if it has no colliding parts that would prevent players from moving through it.",
        "Blocks like tall grass, flowers, signs, etc. are passable, but open doors, fence gates, trap doors, etc. "
                + "are not because they still have parts that can be collided with."
})
@Example("if player's targeted block is passable")
@Since("2.5.1")
public class CondIsPassable extends PropertyCondition<FabricBlock> {

    static {
        register(CondIsPassable.class, "passable", "blocks");
    }

    @Override
    public boolean check(FabricBlock block) {
        return !block.state().blocksMotion();
    }

    @Override
    protected String getPropertyName() {
        return "passable";
    }
}
