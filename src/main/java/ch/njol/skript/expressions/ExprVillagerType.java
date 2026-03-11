package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVillagerType extends SimplePropertyExpression<LivingEntity, VillagerType> {

    static {
        register(ExprVillagerType.class, VillagerType.class, "villager type", "livingentities");
    }

    @Override
    public @Nullable VillagerType convert(LivingEntity entity) {
        if (entity instanceof Villager villager) {
            return villager.getVillagerData().type().value();
        }
        if (entity instanceof ZombieVillager zombieVillager) {
            return zombieVillager.getVillagerData().type().value();
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{VillagerType.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(delta != null && delta[0] instanceof VillagerType type)) {
            return;
        }
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (entity instanceof Villager villager) {
                VillagerData updated = villager.getVillagerData().withType(BuiltInRegistries.VILLAGER_TYPE.wrapAsHolder(type));
                villager.setVillagerData(updated);
            } else if (entity instanceof ZombieVillager zombieVillager) {
                VillagerData updated = zombieVillager.getVillagerData().withType(BuiltInRegistries.VILLAGER_TYPE.wrapAsHolder(type));
                zombieVillager.setVillagerData(updated);
            }
        }
    }

    @Override
    public Class<? extends VillagerType> getReturnType() {
        return VillagerType.class;
    }

    @Override
    protected String getPropertyName() {
        return "villager type";
    }
}
