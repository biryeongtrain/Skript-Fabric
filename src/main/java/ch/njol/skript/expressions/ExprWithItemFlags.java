package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.SequencedSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWithItemFlags extends SimpleExpression<FabricItemType> {

    private static final List<DataComponentType<?>> ADDITIONAL_TOOLTIP_COMPONENTS = List.of(
            DataComponents.BANNER_PATTERNS,
            DataComponents.BEES,
            DataComponents.BLOCK_ENTITY_DATA,
            DataComponents.BLOCK_STATE,
            DataComponents.BUNDLE_CONTENTS,
            DataComponents.CHARGED_PROJECTILES,
            DataComponents.CONTAINER,
            DataComponents.CONTAINER_LOOT,
            DataComponents.FIREWORK_EXPLOSION,
            DataComponents.FIREWORKS,
            DataComponents.INSTRUMENT,
            DataComponents.MAP_ID,
            DataComponents.PAINTING_VARIANT,
            DataComponents.POT_DECORATIONS,
            DataComponents.POTION_CONTENTS,
            DataComponents.TROPICAL_FISH_PATTERN,
            DataComponents.WRITTEN_BOOK_CONTENT
    );

    private enum FlagMapping {
        HIDE_ADDITIONAL_TOOLTIP(null),
        HIDE_ATTRIBUTES(DataComponents.ATTRIBUTE_MODIFIERS),
        HIDE_CAN_BREAK(DataComponents.CAN_BREAK),
        HIDE_CAN_PLACE_ON(DataComponents.CAN_PLACE_ON),
        HIDE_DYE(DataComponents.DYED_COLOR),
        HIDE_ENCHANTS(DataComponents.ENCHANTMENTS),
        HIDE_STORED_ENCHANTS(DataComponents.STORED_ENCHANTMENTS),
        HIDE_UNBREAKABLE(DataComponents.UNBREAKABLE),
        HIDE_ARMOR_TRIM(DataComponents.TRIM);

        private final @Nullable DataComponentType<?> component;

        FlagMapping(@Nullable DataComponentType<?> component) {
            this.component = component;
        }
    }

    static {
        Skript.registerExpression(
                ExprWithItemFlags.class,
                FabricItemType.class,
                "%itemtypes% with [the] item flag[s] %strings%",
                "%itemtypes% with [the] %strings% item flag[s]",
                "%itemtypes% with all [the] item flags"
        );
    }

    private Expression<FabricItemType> itemTypes;
    private @Nullable Expression<String> itemFlags;
    private boolean allFlags;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        itemTypes = (Expression<FabricItemType>) exprs[0];
        if (matchedPattern <= 1) {
            itemFlags = (Expression<String>) exprs[1];
        }
        allFlags = matchedPattern == 2;
        return true;
    }

    @Override
    protected FabricItemType[] get(SkriptEvent event) {
        FabricItemType[] source = itemTypes.getArray(event);
        String[] flags = allFlags ? allFlags() : itemFlags == null ? new String[0] : itemFlags.getArray(event);
        FabricItemType[] result = new FabricItemType[source.length];
        for (int i = 0; i < source.length; i++) {
            ItemStack stack = source[i].toStack();
            TooltipDisplay display = applyFlags(
                    stack,
                    stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT),
                    flags
            );
            stack.set(DataComponents.TOOLTIP_DISPLAY, display);
            result[i] = new FabricItemType(stack);
        }
        return result;
    }

    private TooltipDisplay applyFlags(ItemStack stack, TooltipDisplay base, String[] flags) {
        TooltipDisplay current = base;
        for (String value : flags) {
            FlagMapping mapping = parseFlag(value);
            if (mapping == null) {
                continue;
            }
            if (mapping == FlagMapping.HIDE_ADDITIONAL_TOOLTIP) {
                for (DataComponentType<?> component : ADDITIONAL_TOOLTIP_COMPONENTS) {
                    if (stack.has(component)) {
                        current = current.withHidden(component, true);
                    }
                }
                continue;
            }
            current = current.withHidden(mapping.component, true);
        }
        return current;
    }

    private String[] allFlags() {
        return java.util.Arrays.stream(FlagMapping.values()).map(Enum::name).toArray(String[]::new);
    }

    private @Nullable FlagMapping parseFlag(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toString()
                .trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("HIDE_")) {
            normalized = "HIDE_" + normalized;
        }
        try {
            return FlagMapping.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public boolean isSingle() {
        return itemTypes.isSingle();
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (allFlags) {
            return itemTypes.toString(event, debug) + " with all item flags";
        }
        return itemTypes.toString(event, debug) + " with item flags " + itemFlags.toString(event, debug);
    }
}
