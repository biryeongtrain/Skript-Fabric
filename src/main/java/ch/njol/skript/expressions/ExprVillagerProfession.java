package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVillagerProfession extends SimplePropertyExpression<LivingEntity, VillagerProfession> {

    static {
        register(ExprVillagerProfession.class, VillagerProfession.class, "villager profession", "livingentities");
    }

    @Override
    public @Nullable VillagerProfession convert(LivingEntity entity) {
        if (entity instanceof Villager villager) {
            return villager.getVillagerData().profession().value();
        }
        if (entity instanceof ZombieVillager zombieVillager) {
            return zombieVillager.getVillagerData().profession().value();
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE -> new Class[]{VillagerProfession.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (entity instanceof Villager villager) {
                villager.setVillagerData(updatedProfession(villager.getVillagerData(), villager, delta, mode));
            } else if (entity instanceof ZombieVillager zombieVillager) {
                zombieVillager.setVillagerData(updatedProfession(zombieVillager.getVillagerData(), zombieVillager, delta, mode));
            }
        }
    }

    private static VillagerData updatedProfession(
            VillagerData current,
            LivingEntity entity,
            Object @Nullable [] delta,
            ChangeMode mode
    ) {
        if (mode == ChangeMode.DELETE) {
            return current.withProfession(entity.level().registryAccess(), VillagerProfession.NONE);
        }
        if (!(delta != null && delta[0] instanceof VillagerProfession profession)) {
            return current;
        }
        return current.withProfession(BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(profession));
    }

    @Override
    public Class<? extends VillagerProfession> getReturnType() {
        return VillagerProfession.class;
    }

    @Override
    protected String getPropertyName() {
        return "villager profession";
    }
}
