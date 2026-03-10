package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Damage Value/Durability")
@Description("The damage value/durability of an item.")
@Example("set damage value of player's tool to 10")
@Example("reset the durability of {_item}")
@Example("set durability of player's held item to 0")
@Since("1.2, 2.7 (durability reversed)")
public class ExprDurability extends SimplePropertyExpression<Object, Integer> {

    private boolean durability;

    static {
        register(ExprDurability.class, Integer.class, "(damage[s] [value[s]]|1:durabilit(y|ies))", "itemtypes/itemstacks/slots");
    }

    @Override
    public boolean init(ch.njol.skript.lang.Expression<?>[] exprs, int matchedPattern, ch.njol.util.Kleenean isDelayed,
                        ch.njol.skript.lang.SkriptParser.ParseResult parseResult) {
        durability = parseResult.mark == 1;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Integer convert(Object object) {
        ItemStack itemStack = asItemStack(object);
        if (itemStack == null || !itemStack.isDamageableItem()) {
            return null;
        }
        return convertToDamage(itemStack, itemStack.getDamageValue());
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int change = delta == null || delta.length == 0 ? 0 : ((Number) delta[0]).intValue();
        if (mode == ChangeMode.REMOVE) {
            change = -change;
        }
        for (Object object : getExpr().getArray(event)) {
            ItemStack itemStack = asItemStack(object);
            if (itemStack == null || !itemStack.isDamageableItem()) {
                continue;
            }

            int next = switch (mode) {
                case ADD, REMOVE -> convertToDamage(itemStack, itemStack.getDamageValue()) + change;
                case SET -> change;
                case DELETE, RESET -> 0;
            };
            int damage = convertToDamage(itemStack, next);
            ItemStack updated = itemStack.copy();
            updated.setDamageValue(Math.max(0, Math.min(updated.getMaxDamage(), damage)));

            if (object instanceof Slot slot) {
                slot.set(updated);
            } else if (object instanceof ItemStack direct) {
                direct.applyComponents(updated.getComponents());
                direct.setCount(updated.getCount());
            }
        }
    }

    private int convertToDamage(ItemStack itemStack, int value) {
        if (!durability) {
            return value;
        }
        int max = itemStack.getMaxDamage();
        return max == 0 ? 0 : max - value;
    }

    private @Nullable ItemStack asItemStack(Object object) {
        if (object instanceof ItemStack itemStack) {
            return itemStack;
        }
        if (object instanceof Slot slot) {
            return slot.getItem();
        }
        if (object instanceof FabricItemType itemType) {
            ItemStack stack = itemType.toStack();
            Integer damage = stack.get(DataComponents.DAMAGE);
            if (damage != null) {
                stack.setDamageValue(damage);
            }
            return stack;
        }
        return null;
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String getPropertyName() {
        return durability ? "durability" : "damage";
    }
}
