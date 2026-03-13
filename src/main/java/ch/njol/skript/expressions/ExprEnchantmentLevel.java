package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Enchantment Level")
@Description("The level of a particular enchantment on an item.")
@Example("message \"Sharpness %sharpness level of player's tool%\"")
@Since("2.0, Fabric")
public final class ExprEnchantmentLevel extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(ExprEnchantmentLevel.class, Long.class,
                "[the] [enchant[ment]] level[s] of %enchantments% (on|of) %itemtypes%",
                "[the] %enchantments% [enchant[ment]] level[s] (on|of) %itemtypes%",
                "%itemtypes%'[s] %enchantments% [enchant[ment]] level[s]",
                "%itemtypes%'[s] [enchant[ment]] level[s] of %enchantments%");
    }

    private Expression<FabricItemType> items;
    private Expression<Enchantment> enchantments;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        int itemIndex = matchedPattern < 2 ? 1 : 0;
        items = (Expression<FabricItemType>) exprs[itemIndex];
        enchantments = (Expression<Enchantment>) exprs[itemIndex ^ 1];
        return true;
    }

    @Override
    protected Long[] get(SkriptEvent event) {
        List<Long> levels = new ArrayList<>();
        for (FabricItemType item : items.getArray(event)) {
            ItemStack stack = item.toStack();
            for (Enchantment enchantment : enchantments.getArray(event)) {
                int level = levelOf(stack.getEnchantments(), enchantment);
                if (level > 0) {
                    levels.add((long) level);
                }
            }
        }
        return levels.toArray(Long[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
            return;
        }
        int change = number.intValue();
        for (FabricItemType item : items.getArray(event)) {
            ItemStack stack = item.toStack();
            for (Enchantment enchantment : enchantments.getArray(event)) {
                int current = levelOf(stack.getEnchantments(), enchantment);
                int next = switch (mode) {
                    case SET -> change;
                    case ADD -> current + change;
                    case REMOVE -> current - change;
                    default -> current;
                };
                EnchantmentHelper.updateEnchantments(stack, mutable -> {
                    mutable.removeIf(holder -> holder.value() == enchantment);
                    if (next > 0) {
                        mutable.set(Holder.direct(enchantment), next);
                    }
                });
            }
            item.applyPrototype(stack);
        }
    }

    @Override
    public boolean isSingle() {
        return items.isSingle() && enchantments.isSingle();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the level of " + enchantments.toString(event, debug) + " of " + items.toString(event, debug);
    }

    private static int levelOf(net.minecraft.world.item.enchantment.ItemEnchantments enchantments, Enchantment target) {
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().value() == target) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
