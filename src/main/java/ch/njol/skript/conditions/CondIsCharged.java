package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.WitherSkull;

@Name("Is Charged")
@Description("Checks if a creeper, wither, or wither skull is charged (powered).")
@Example("""
        if the last spawned creeper is charged:
            broadcast "A charged creeper is at %location of last spawned creeper%"
        """)
@Since("2.5, 2.10 (withers, wither skulls)")
public class CondIsCharged extends PropertyCondition<Entity> {

    static {
        register(CondIsCharged.class, "(charged|powered)", "entities");
    }

    @Override
    public boolean check(Entity entity) {
        if (entity instanceof Creeper creeper) {
            return creeper.isPowered();
        }
        if (entity instanceof WitherSkull witherSkull) {
            return witherSkull.isDangerous();
        }
        return entity instanceof WitherBoss wither && wither.isPowered();
    }

    @Override
    protected String getPropertyName() {
        return "charged";
    }
}
