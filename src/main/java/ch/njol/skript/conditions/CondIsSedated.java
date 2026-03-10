package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.level.block.CampfireBlock;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Beehive Is Sedated")
@Description("Checks if a beehive is sedated from a nearby campfire.")
@Example("if {_beehive} is sedated:")
@Since("2.11")
public class CondIsSedated extends PropertyCondition<FabricBlock> {

    static {
        register(CondIsSedated.class, PropertyType.BE, "sedated", "blocks");
    }

    @Override
    public boolean check(FabricBlock block) {
        return CampfireBlock.isSmokeyPos(block.level(), block.position());
    }

    @Override
    protected String getPropertyName() {
        return "sedated";
    }
}
