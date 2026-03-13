package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("X of Item/Entity Type")
@Description("An expression for using an item or entity type with a different amount.")
@Example("set {_scaled} to 3 of diamond")
@Since("1.2, Fabric")
@Keywords("amount")
public class ExprXOf extends PropertyExpression<Object, Object> {

    static {
        Skript.registerExpression(ExprXOf.class, Object.class, "%number% of %itemstacks/itemtypes/entitytypes/particles%");
    }

    private Class<?>[] possibleReturnTypes;
    private Expression<Number> amount;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        amount = (Expression<Number>) exprs[0];
        Expression<?> type = exprs[1];
        setExpr(type);

        List<Class<?>> resolvedReturnTypes = new ArrayList<>();
        if (type.canReturn(ItemStack.class)) {
            resolvedReturnTypes.add(ItemStack.class);
        }
        if (type.canReturn(FabricItemType.class)) {
            resolvedReturnTypes.add(FabricItemType.class);
        }
        if (type.canReturn(EntityType.class)) {
            resolvedReturnTypes.add(EntityType.class);
        }
        if (type.canReturn(ParticleEffect.class)) {
            resolvedReturnTypes.add(ParticleEffect.class);
        }
        possibleReturnTypes = resolvedReturnTypes.toArray(new Class[0]);
        return true;
    }

    @Override
    protected Object[] get(SkriptEvent event, Object[] source) {
        Number resolvedAmount = amount.getSingle(event);
        if (resolvedAmount == null) {
            return (Object[]) Array.newInstance(getReturnType(), 0);
        }

        long absoluteAmount = Math.max(resolvedAmount.longValue(), 0L);
        return get(source, object -> {
            if (object instanceof ItemStack itemStack) {
                ItemStack copy = itemStack.copy();
                copy.setCount((int) absoluteAmount);
                return copy;
            }
            if (object instanceof FabricItemType itemType) {
                FabricItemType scaled = new FabricItemType(itemType.toStack());
                scaled.amount((int) absoluteAmount);
                return scaled;
            }
            if (object instanceof EntityType originalType) {
                EntityType scaled = originalType.clone();
                scaled.amount = (int) absoluteAmount;
                return scaled;
            }
            if (object instanceof ParticleEffect particleEffect) {
                ParticleEffect effect = particleEffect.copy();
                effect.count((int) absoluteAmount);
                return effect;
            }
            return null;
        });
    }

    @Override
    public Class<?> getReturnType() {
        return possibleReturnTypes.length == 1 ? possibleReturnTypes[0] : Object.class;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return Arrays.copyOf(possibleReturnTypes, possibleReturnTypes.length);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return amount.toString(event, debug) + " of " + getExpr().toString(event, debug);
    }

    @Override
    public Expression<?> simplify() {
        if (amount instanceof Literal && getExpr() instanceof Literal) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return super.simplify();
    }
}
