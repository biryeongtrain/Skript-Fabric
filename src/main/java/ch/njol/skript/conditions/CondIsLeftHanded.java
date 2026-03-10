package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

@Name("Left Handed")
@Description({
        "Checks if living entities or players are left or right-handed. Armor stands are neither right nor left-handed."
})
@Example("""
    on damage of player:
        if victim is left handed:
            cancel event
    """)
@Since("2.8.0")
public class CondIsLeftHanded extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsLeftHanded.class, "(:left|right)( |-)handed", "livingentities");
    }

    private HumanoidArm hand;

    @Override
    public boolean init(Expression[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        hand = parseResult.hasTag("left") ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        if (livingEntity instanceof Mob mob) {
            return mob.isLeftHanded() == (hand == HumanoidArm.LEFT);
        }
        if (livingEntity instanceof Player player) {
            return player.getMainArm() == hand;
        }
        return false;
    }

    @Override
    protected String getPropertyName() {
        return (hand == HumanoidArm.LEFT ? "left" : "right") + " handed";
    }
}
