package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;

@Name("Is Blocking")
@Description("Checks whether a player is blocking with their shield.")
@Example("""
    on damage of player:
        victim is blocking
        damage attacker by 0.5 hearts
    """)
@Since("unknown (before 2.1)")
public class CondIsBlocking extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsBlocking.class, "(blocking|defending) [with [a] shield]", "livingentities");
    }

    @Override
    public boolean check(LivingEntity player) {
        return player.isBlocking();
    }

    @Override
    protected String getPropertyName() {
        return "blocking";
    }
}
