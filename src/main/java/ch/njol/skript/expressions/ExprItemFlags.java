package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
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

@Name("Item Flags")
@Description({
        "Gets or changes the hidden tooltip flags of an item.",
        "Flags are exposed as compatibility strings such as HIDE_ATTRIBUTES, HIDE_ENCHANTS, and HIDE_UNBREAKABLE."
})
@Example("set item flags of player's tool to \"hide enchants\" and \"hide unbreakable\"")
@Since("2.12")
public final class ExprItemFlags extends PropertyExpression<FabricItemType, String> {

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
        registerDefault(ExprItemFlags.class, String.class, "[item] flags", "itemtypes");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends FabricItemType>) expressions[0]);
        return true;
    }

    @Override
    protected String[] get(SkriptEvent event, FabricItemType[] source) {
        List<String> flags = new ArrayList<>();
        for (FabricItemType item : source) {
            flags.addAll(flagsOf(item.toStack()));
        }
        return flags.toArray(String[]::new);
    }

    private List<String> flagsOf(ItemStack stack) {
        TooltipDisplay display = stack.get(DataComponents.TOOLTIP_DISPLAY);
        if (display == null) {
            return List.of();
        }
        SequencedSet<DataComponentType<?>> hidden = display.hiddenComponents();
        if (hidden.isEmpty()) {
            return List.of();
        }

        List<String> flags = new ArrayList<>();
        if (hasHiddenAdditionalTooltip(hidden)) {
            flags.add(FlagMapping.HIDE_ADDITIONAL_TOOLTIP.name());
        }
        for (FlagMapping mapping : FlagMapping.values()) {
            if (mapping.component != null && hidden.contains(mapping.component)) {
                flags.add(mapping.name());
            }
        }
        return flags;
    }

    private boolean hasHiddenAdditionalTooltip(SequencedSet<DataComponentType<?>> hidden) {
        for (DataComponentType<?> component : ADDITIONAL_TOOLTIP_COMPONENTS) {
            if (hidden.contains(component)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (FabricItemType item : getExpr().getArray(event)) {
            ItemStack stack = item.toStack();
            TooltipDisplay current = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
            TooltipDisplay updated = switch (mode) {
                case DELETE, RESET -> new TooltipDisplay(current.hideTooltip(), new LinkedHashSet<>());
                case SET -> applyFlags(stack, new TooltipDisplay(current.hideTooltip(), new LinkedHashSet<>()), delta, true);
                case ADD -> applyFlags(stack, current, delta, true);
                case REMOVE -> applyFlags(stack, current, delta, false);
            };
            stack.set(DataComponents.TOOLTIP_DISPLAY, updated);
            item.applyPrototype(stack);
        }
    }

    private TooltipDisplay applyFlags(ItemStack stack, TooltipDisplay base, Object @Nullable [] delta, boolean hidden) {
        if (delta == null) {
            return base;
        }
        TooltipDisplay updated = base;
        for (Object value : delta) {
            FlagMapping mapping = parseFlag(value);
            if (mapping == null) {
                continue;
            }
            if (mapping == FlagMapping.HIDE_ADDITIONAL_TOOLTIP) {
                for (DataComponentType<?> component : ADDITIONAL_TOOLTIP_COMPONENTS) {
                    if (stack.has(component) || !hidden) {
                        updated = updated.withHidden(component, hidden);
                    }
                }
            } else {
                updated = updated.withHidden(mapping.component, hidden);
            }
        }
        return updated;
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
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "item flags of " + getExpr().toString(event, debug);
    }
}
