package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.EnchantmentType;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.List;
import java.util.stream.Stream;

@Name("Enchant/Disenchant")
@Description("Enchant or disenchant an existing item.")
@Example("enchant the player's tool with sharpness 5")
@Example("enchant the player's tool at level 30")
@Example("disenchant the player's tool")
@Since("2.0, 2.13 (at level)")
public class EffEnchant extends Effect {

    private enum Operation {
        ENCHANT,
        ENCHANT_AT_LEVEL,
        DISENCHANT
    }

    private static boolean registered;
    private static final Patterns<Operation> PATTERNS = new Patterns<>(new Object[][]{
            {"enchant %~itemtypes/slots% with %enchantmenttypes%", Operation.ENCHANT},
            {"[naturally|randomly] enchant %~itemtypes/slots% at level %number%[treasure:[,] allowing treasure enchant[ment]s]",
                    Operation.ENCHANT_AT_LEVEL},
            {"disenchant %~itemtypes/slots%", Operation.DISENCHANT}
    });

    private Expression<?> items;
    private @Nullable Expression<?> enchantments;
    private @Nullable Expression<Number> level;
    private boolean treasure;
    private Operation operation;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffEnchant.class, PATTERNS.getPatterns());
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = exprs[0];
        if (matchedPattern == 0) {
            enchantments = exprs[1];
        } else if (matchedPattern == 1) {
            level = (Expression<Number>) exprs[1];
            treasure = parseResult.hasTag("treasure");
        }
        operation = PATTERNS.getInfo(matchedPattern);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void execute(SkriptEvent event) {
        Object[] targets = items != null ? items.getArray(event) : null;
        if (targets == null || targets.length == 0) return;

        switch (operation) {
            case ENCHANT -> {
                EnchantmentType[] types = enchantments != null
                        ? ((Expression<EnchantmentType>) enchantments).getArray(event)
                        : null;
                if (types == null) return;
                for (Object target : targets) {
                    withItemStack(target, stack -> {
                        for (EnchantmentType type : types) {
                            EnchantmentHelper.updateEnchantments(stack, mutable ->
                                    mutable.set(type.enchantment(), type.level()));
                        }
                    });
                }
            }
            case DISENCHANT -> {
                for (Object target : targets) {
                    withItemStack(target, stack ->
                            EnchantmentHelper.setEnchantments(stack, ItemEnchantments.EMPTY));
                }
            }
            case ENCHANT_AT_LEVEL -> {
                if (level == null || event.server() == null) return;
                Number levelValue = level.getSingle(event);
                if (levelValue == null) return;
                int enchantLevel = levelValue.intValue();
                var registry = event.server().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                Stream<Holder<Enchantment>> enchantmentStream = treasure
                        ? registry.listElements().map(ref -> (Holder<Enchantment>) ref)
                        : java.util.stream.StreamSupport.stream(
                                registry.getTagOrEmpty(EnchantmentTags.IN_ENCHANTING_TABLE).spliterator(), false);
                ItemStack first = getItemStack(targets[0]);
                if (first == null) return;
                List<EnchantmentInstance> selected = EnchantmentHelper.selectEnchantment(
                        RandomSource.create(), first, enchantLevel, enchantmentStream);
                for (Object target : targets) {
                    withItemStack(target, stack -> {
                        for (EnchantmentInstance instance : selected) {
                            EnchantmentHelper.updateEnchantments(stack, mutable ->
                                    mutable.set(instance.enchantment(), instance.level()));
                        }
                    });
                }
            }
        }
    }

    private static @Nullable ItemStack getItemStack(Object target) {
        if (target instanceof FabricItemType itemType) {
            return itemType.toStack();
        } else if (target instanceof Slot slot) {
            return slot.getItem().copy();
        }
        return null;
    }

    private static void withItemStack(Object target, java.util.function.Consumer<ItemStack> action) {
        if (target instanceof FabricItemType itemType) {
            ItemStack stack = itemType.toStack();
            action.accept(stack);
            itemType.applyPrototype(stack);
        } else if (target instanceof Slot slot) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) return;
            action.accept(stack);
            slot.set(stack);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (operation) {
            case ENCHANT -> "enchant " + items.toString(event, debug) + " with "
                    + (enchantments == null ? "" : enchantments.toString(event, debug));
            case ENCHANT_AT_LEVEL -> "enchant " + items.toString(event, debug) + " at level "
                    + (level == null ? "" : level.toString(event, debug))
                    + (treasure ? " allowing treasure enchantments" : "");
            case DISENCHANT -> "disenchant " + items.toString(event, debug);
        };
    }
}
