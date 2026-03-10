package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.server.level.ServerPlayer;

@Name("Can Fly")
@Description("Whether a player is allowed to fly.")
@Example("player can fly")
@Since("2.3")
public class CondCanFly extends PropertyCondition<ServerPlayer> {

    static {
        register(CondCanFly.class, PropertyType.CAN, "fly", "players");
    }

    @Override
    public boolean check(ServerPlayer player) {
        return player.getAbilities().mayfly;
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.CAN;
    }

    @Override
    protected String getPropertyName() {
        return "fly";
    }
}
