package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Experience;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Enchantment Offer Cost")
@Description("The cost of an enchantment offer when the backing offer object exposes it.")
@Example("set cost of enchantment offer 1 to 50")
@Since("2.5")
public final class ExprEnchantmentOfferCost extends SimplePropertyExpression<Object, Long> {

    static {
        register(ExprEnchantmentOfferCost.class, Long.class, "[enchant[ment]] cost", "enchantmentoffers");
    }

    @Override
    public @Nullable Long convert(Object offer) {
        Object value = ReflectiveHandleAccess.invokeNoArg(offer, "cost", "getCost");
        return value instanceof Number number ? number.longValue() : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class, Experience.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0) {
            return;
        }
        Object value = delta[0];
        int change = value instanceof Experience experience ? experience.getXP() : ((Number) value).intValue();
        for (Object offer : getExpr().getArray(event)) {
            Long current = convert(offer);
            int next = switch (mode) {
                case SET -> change;
                case ADD -> (current == null ? 0 : current.intValue()) + change;
                case REMOVE -> (current == null ? 0 : current.intValue()) - change;
                default -> current == null ? 0 : current.intValue();
            };
            if (next >= 1) {
                ReflectiveHandleAccess.invokeSingleArg(offer, "setCost", next);
            }
        }
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "enchantment offer cost";
    }
}
