package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVillagerLevel extends SimplePropertyExpression<LivingEntity, Number> {

    static {
        register(ExprVillagerLevel.class, Number.class, "villager (level|:experience)", "livingentities");
    }

    private boolean experience;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        experience = parseResult.hasTag("experience");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Number convert(LivingEntity entity) {
        if (!(entity instanceof Villager villager)) {
            return null;
        }
        return experience ? villager.getVillagerXp() : villager.getVillagerData().level();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = delta != null && delta[0] instanceof Number number ? number.intValue() : 1;
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (!(entity instanceof Villager villager)) {
                continue;
            }
            if (experience) {
                villager.setVillagerXp(applyVillagerValue(villager.getVillagerXp(), amount, mode, true));
                continue;
            }
            setVillagerLevel(villager, applyVillagerValue(villager.getVillagerData().level(), amount, mode, false));
        }
    }

    static int applyVillagerValue(int current, int changeValue, ChangeMode mode, boolean experience) {
        int minimum = experience ? 0 : 1;
        int maximum = experience ? Integer.MAX_VALUE : 5;
        int next = switch (mode) {
            case SET -> changeValue;
            case ADD -> current + changeValue;
            case REMOVE -> current - changeValue;
            case RESET -> minimum;
            default -> current;
        };
        return Math.max(minimum, Math.min(maximum, next));
    }

    static void setVillagerLevel(Villager villager, int newLevel) {
        int currentLevel = villager.getVillagerData().level();
        if (newLevel > currentLevel) {
            while (villager.getVillagerData().level() < newLevel) {
                if (!increaseMerchantCareer(villager)) {
                    break;
                }
            }
            if (villager.getVillagerData().level() >= newLevel) {
                return;
            }
        }
        villager.setVillagerData(villager.getVillagerData().withLevel(newLevel));
    }

    private static boolean increaseMerchantCareer(Villager villager) {
        try {
            Method method = Villager.class.getDeclaredMethod("increaseMerchantCareer");
            method.setAccessible(true);
            method.invoke(villager);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return false;
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unable to increase villager career", e.getCause());
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return experience ? "villager experience" : "villager level";
    }
}
