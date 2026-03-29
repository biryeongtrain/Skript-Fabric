package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Name("Is Dancing")
@Description("Checks to see if an entity is dancing, such as allays, parrots, or piglins.")
@Example("""
        if last spawned allay is dancing:
            broadcast "Dance Party!"
        """)
@Since("2.11")
public class CondIsDancing extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsDancing.class, "dancing", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        if (entity instanceof Allay allay) {
            return allay.isDancing();
        }
        if (entity instanceof Parrot parrot) {
            return parrot.isPartyParrot();
        }
        return entity instanceof Piglin piglin && piglin.isDancing();
    }

    @Override
    protected String getPropertyName() {
        return "dancing";
    }
}
