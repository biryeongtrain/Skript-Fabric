package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Damaged Item")
@Description("Directly damages an item. In MC versions 1.12.2 and lower, this can be used to apply data values to items/blocks")
@Example("give player diamond sword with damage value 100")
@Example("set player's tool to diamond hoe damaged by 250")
@Example("give player diamond sword with damage 700 named \"BROKEN SWORD\"")
@Example("set {_item} to diamond hoe with damage value 50 named \"SAD HOE\"")
@Since("2.4")
public class ExprDamagedItem extends PropertyExpression<ItemStack, ItemStack> {

    static {
        ch.njol.skript.Skript.registerExpression(ExprDamagedItem.class, ItemStack.class,
                "%itemstacks% with (damage|data) [value] %number%",
                "%itemstacks% damaged by %number%");
    }

    private Expression<Number> damage;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<ItemStack>) exprs[0]);
        damage = (Expression<Number>) exprs[1];
        return true;
    }

    @Override
    protected ItemStack[] get(SkriptEvent event, ItemStack[] source) {
        Number amount = damage.getSingle(event);
        if (amount == null) {
            return source;
        }
        return get(source, stack -> {
            ItemStack copy = stack.copy();
            if (copy.isDamageableItem()) {
                copy.setDamageValue(Math.max(0, amount.intValue()));
            }
            return copy;
        });
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr().toString(event, debug) + " with damage value " + damage.toString(event, debug);
    }
}
