package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Has Glowing Text")
@Description("Checks whether a sign (either a block or an item) has glowing text")
@Example("if target block has glowing text")
@Since("2.8.0")
public class CondGlowingText extends PropertyCondition<Object> {

    static {
        register(CondGlowingText.class, PropertyType.HAVE, "glowing text", "blocks/itemtypes");
    }

    @Override
    public boolean check(Object obj) {
        if (obj instanceof FabricBlock block) {
            BlockEntity blockEntity = block.level().getBlockEntity(block.position());
            return isSignGlowing(blockEntity);
        }
        if (obj instanceof FabricItemType) {
            return false;
        }
        return false;
    }

    private static boolean isSignGlowing(Object value) {
        if (!(value instanceof SignBlockEntity sign)) {
            return false;
        }
        return sign.getFrontText().hasGlowingText() || sign.getBackText().hasGlowingText();
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.HAVE;
    }

    @Override
    protected String getPropertyName() {
        return "glowing text";
    }
}
