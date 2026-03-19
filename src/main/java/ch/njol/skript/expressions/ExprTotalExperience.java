package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import kim.biryeong.skriptFabric.mixin.ExperienceOrbAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTotalExperience extends SimplePropertyExpression<Entity, Integer> {

    static {
        register(ExprTotalExperience.class, Integer.class, "[total] experience", "entities");
    }

    @Override
    public @Nullable Integer convert(Entity entity) {
        if (entity instanceof ExperienceOrb orb) {
            return orb.getValue();
        }
        if (entity instanceof ServerPlayer player) {
            return totalExperience(player);
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, DELETE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = delta == null ? 0 : ((Number) delta[0]).intValue();
        for (Entity entity : getExpr().getArray(event)) {
            if (entity instanceof ExperienceOrb orb) {
                int current = orb.getValue();
                int next = switch (mode) {
                    case SET -> amount;
                    case ADD -> current + amount;
                    case REMOVE -> current - amount;
                    case DELETE, RESET -> 0;
                    default -> current;
                };
                ((ExperienceOrbAccessor) orb).skript$setValue(Math.max(0, next));
            } else if (entity instanceof ServerPlayer player) {
                int current = totalExperience(player);
                switch (mode) {
                    case DELETE, RESET -> setTotalExperience(player, 0);
                    case SET -> setTotalExperience(player, amount);
                    case REMOVE -> setTotalExperience(player, current - amount);
                    case ADD -> {
                        if (amount > 0) {
                            player.giveExperiencePoints(amount);
                        } else {
                            setTotalExperience(player, current + amount);
                        }
                    }
                }
            }
        }
    }

    static int totalExperience(ServerPlayer player) {
        return cumulativeExperience(player.experienceLevel)
                + Math.round(levelExperience(player.experienceLevel) * player.experienceProgress);
    }

    static void setTotalExperience(ServerPlayer player, int experience) {
        int clamped = Math.max(0, experience);
        int level = 0;
        int remaining = clamped;
        while (remaining >= levelExperience(level)) {
            remaining -= levelExperience(level);
            level++;
        }
        player.totalExperience = clamped;
        player.setExperienceLevels(level);
        player.setExperiencePoints(remaining);
    }

    static int cumulativeExperience(int level) {
        if (level <= 15) {
            return level * level + 6 * level;
        }
        if (level <= 30) {
            return (int) (2.5D * level * level - 40.5D * level + 360);
        }
        return (int) (4.5D * level * level - 162.5D * level + 2220);
    }

    static int levelExperience(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        }
        if (level <= 30) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "total experience";
    }
}
