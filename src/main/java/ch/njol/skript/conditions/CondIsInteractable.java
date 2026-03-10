package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Interactable")
@Description("Checks wether or not a block is interactable.")
@Example("""
    on block break:
        if event-block is interactable:
            cancel event
            send "You cannot break interactable blocks!"
    """)
@Since("2.5.2")
public class CondIsInteractable extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsInteractable.class, "interactable", "itemtypes");
    }

    @Override
    public boolean check(FabricItemType itemType) {
        if (!(itemType.item() instanceof BlockItem blockItem)) {
            return false;
        }
        return blockItem.getBlock() instanceof BaseEntityBlock
                || blockItem.getBlock() instanceof ButtonBlock
                || blockItem.getBlock() instanceof CommandBlock
                || blockItem.getBlock() instanceof DoorBlock
                || blockItem.getBlock() instanceof FenceGateBlock
                || blockItem.getBlock() instanceof LeverBlock
                || blockItem.getBlock() instanceof NoteBlock
                || blockItem.getBlock() instanceof TrapDoorBlock;
    }

    @Override
    protected String getPropertyName() {
        return "interactable";
    }
}
