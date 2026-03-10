package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.server.level.ServerPlayer;

@Name("Is Sneaking")
@Description("Checks whether a player is sneaking.")
@Example("""
    # prevent mobs from seeing sneaking players if they are at least 4 meters apart
    on target:
        target is sneaking
        distance of target and the entity is bigger than 4
        cancel the event
    """)
@Since("1.4.4")
public class CondIsSneaking extends PropertyCondition<ServerPlayer> {

    static {
        register(CondIsSneaking.class, "sneaking", "players");
    }

    @Override
    public boolean check(ServerPlayer player) {
        return player.isShiftKeyDown();
    }

    @Override
    protected String getPropertyName() {
        return "sneaking";
    }
}
