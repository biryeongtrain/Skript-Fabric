package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprEntityAttribute extends PropertyExpression<LivingEntity, Number> {

    static {
        ch.njol.skript.Skript.registerExpression(
                ExprEntityAttribute.class,
                Number.class,
                "[the] %attributetype% [(1:(total|final|modified))] attribute [value] of %livingentities%",
                "%livingentities%'[s] %attributetype% [(1:(total|final|modified))] attribute [value]"
        );
    }

    private Expression<Holder<Attribute>> attributes;
    private boolean withModifiers;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        attributes = (Expression<Holder<Attribute>>) exprs[matchedPattern];
        setExpr((Expression<? extends LivingEntity>) exprs[matchedPattern ^ 1]);
        withModifiers = parseResult.mark == 1;
        return true;
    }

    @Override
    protected Number[] get(SkriptEvent event, LivingEntity[] source) {
        Holder<Attribute> attribute = attributes.getSingle(event);
        if (attribute == null) {
            return new Number[0];
        }
        List<Number> values = new ArrayList<>();
        for (LivingEntity entity : source) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                values.add(withModifiers ? instance.getValue() : instance.getBaseValue());
            }
        }
        return values.toArray(Number[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (withModifiers) {
            return null;
        }
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Holder<Attribute> attribute = attributes.getSingle(event);
        if (attribute == null) {
            return;
        }
        double value = delta == null ? 0.0 : ((Number) delta[0]).doubleValue();
        for (LivingEntity entity : getExpr().getArray(event)) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            double updated = switch (mode) {
                case SET -> value;
                case ADD -> instance.getBaseValue() + value;
                case REMOVE -> instance.getBaseValue() - value;
                case RESET -> attribute.value().getDefaultValue();
                default -> instance.getBaseValue();
            };
            instance.setBaseValue(updated);
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return attributes.toString(event, debug) + " attribute of " + getExpr().toString(event, debug);
    }
}
