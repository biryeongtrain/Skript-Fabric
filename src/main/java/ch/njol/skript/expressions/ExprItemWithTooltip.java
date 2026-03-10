package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item with Tooltip")
@Description({
    "Get an item with or without entire/additional tooltip.",
    "If changing the 'entire' tooltip of an item, nothing will show up when a player hovers over it.",
    "If changing the 'additional' tooltip, only specific parts (which change per item) will be hidden."
})
@Example("set {_item with additional tooltip} to diamond with additional tooltip")
@Example("set {_item without entire tooltip} to diamond without entire tooltip")
@RequiredPlugins("Minecraft 1.20.5+")
@Since("2.11")
public class ExprItemWithTooltip extends PropertyExpression<FabricItemType, FabricItemType> {

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

    static {
        Skript.registerExpression(ExprItemWithTooltip.class, FabricItemType.class,
                "%itemtypes% with[:out] [entire|:additional] tool[ ]tip[s]");
    }

    private boolean without;
    private boolean entire;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<FabricItemType>) expressions[0]);
        without = parseResult.hasTag("out");
        entire = !parseResult.hasTag("additional");
        return true;
    }

    @Override
    protected FabricItemType[] get(SkriptEvent event, FabricItemType[] source) {
        return get(source, itemType -> {
            ItemStack stack = itemType.toStack();
            TooltipDisplay current = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
            TooltipDisplay updated;
            if (entire) {
                updated = new TooltipDisplay(without, new LinkedHashSet<>(current.hiddenComponents()));
            } else {
                updated = current;
                for (DataComponentType<?> componentType : additionalTooltipTypes(stack)) {
                    updated = updated.withHidden(componentType, without);
                }
            }
            stack.set(DataComponents.TOOLTIP_DISPLAY, updated);
            return new FabricItemType(stack);
        });
    }

    private List<DataComponentType<?>> additionalTooltipTypes(ItemStack stack) {
        List<DataComponentType<?>> present = new ArrayList<>();
        for (DataComponentType<?> componentType : ADDITIONAL_TOOLTIP_COMPONENTS) {
            if (stack.has(componentType)) {
                present.add(componentType);
            }
        }
        return present;
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr().toString(event, debug)
                + (without ? " without" : " with")
                + (entire ? " entire" : " additional")
                + " tooltip";
    }
}
