package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.server.level.ServerPlayer;

@Name("Is Flying")
@Description("Checks whether a player is flying.")
@Example("player is not flying")
@Since("1.4.4")
public class CondIsFlying extends PropertyCondition<ServerPlayer> {

    static {
        register(CondIsFlying.class, "flying", "players");
    }

    @Override
    public boolean check(ServerPlayer player) {
        return player.getAbilities().flying;
    }

    @Override
    protected String getPropertyName() {
        return "flying";
    }
}
