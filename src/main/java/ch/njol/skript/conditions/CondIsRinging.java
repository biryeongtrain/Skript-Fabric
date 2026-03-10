package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Bell Is Ringing")
@Description("Checks to see if a bell is currently ringing. A bell typically rings for 50 game ticks.")
@Example("target block is ringing")
@Since("2.9.0")
public class CondIsRinging extends PropertyCondition<FabricBlock> {

    static {
        register(CondIsRinging.class, "ringing", "blocks");
    }

    @Override
    public boolean check(FabricBlock value) {
        if (!(value.level().getBlockEntity(value.position()) instanceof BellBlockEntity bell)) {
            return false;
        }
        try {
            return (Boolean) BellBlockEntity.class.getMethod("isShaking").invoke(bell);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @Override
    protected String getPropertyName() {
        return "ringing";
    }
}
