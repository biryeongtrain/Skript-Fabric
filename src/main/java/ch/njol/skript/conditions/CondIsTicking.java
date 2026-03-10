package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

@Name("Is Ticking")
@Description("Checks if an entity is ticking.")
@Example("send true if target is ticking")
@Since("2.10")
public class CondIsTicking extends PropertyCondition<Entity> {

    static {
        register(CondIsTicking.class, "ticking", "entities");
    }

    @Override
    public boolean check(Entity entity) {
        return entity.level() instanceof ServerLevel level && level.isPositionEntityTicking(entity.blockPosition());
    }

    @Override
    protected String getPropertyName() {
        return "ticking";
    }
}
