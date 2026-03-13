package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item Enchantments")
@Description("All enchantments applied to an item type.")
@Example("clear enchantments of player's tool")
@Since("2.2-dev36, Fabric")
public final class ExprEnchantments extends PropertyExpression<FabricItemType, Enchantment> {

    static {
        registerDefault(ExprEnchantments.class, Enchantment.class, "enchantments", "itemtypes");
    }

    @Override
    protected Enchantment[] get(SkriptEvent event, FabricItemType[] source) {
        List<Enchantment> enchantments = new ArrayList<>();
        for (FabricItemType item : source) {
            for (var entry : item.toStack().getEnchantments().entrySet()) {
                enchantments.add(entry.getKey().value());
            }
        }
        return enchantments.toArray(Enchantment[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Enchantment.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (FabricItemType item : getExpr().getArray(event)) {
            ItemStack stack = item.toStack();
            if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
                EnchantmentHelper.setEnchantments(stack, ItemEnchantments.EMPTY);
                item.applyPrototype(stack);
                continue;
            }
            if (delta == null || delta.length == 0) {
                continue;
            }
            EnchantmentHelper.updateEnchantments(stack, mutable -> {
                if (mode == ChangeMode.SET) {
                    mutable.removeIf(holder -> true);
                }
                for (Object value : delta) {
                    if (!(value instanceof Enchantment enchantment)) {
                        continue;
                    }
                    if (mode == ChangeMode.REMOVE) {
                        mutable.removeIf(holder -> holder.value() == enchantment);
                    } else {
                        int current = levelOf(mutable.toImmutable(), enchantment);
                        mutable.set(Holder.direct(enchantment), Math.max(1, current == 0 ? 1 : current));
                    }
                }
            });
            item.applyPrototype(stack);
        }
    }

    @Override
    public Class<? extends Enchantment> getReturnType() {
        return Enchantment.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the enchantments of " + getExpr().toString(event, debug);
    }

    static int levelOf(ItemEnchantments enchantments, Enchantment target) {
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().value() == target) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
