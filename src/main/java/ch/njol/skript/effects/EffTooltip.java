package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item Tooltips")
@Description({
        "Show or hide the tooltip of an item.",
        "If changing the 'entire' tooltip of an item, nothing will show up when a player hovers over it.",
        "If changing the 'additional' tooltip, only specific parts (which change per item) will be hidden."
})
@Example("hide the entire tooltip of player's tool")
@Example("hide {_item}'s additional tool tip")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public final class EffTooltip extends Effect {

    private static boolean registered;

    private Expression<FabricItemType> items;
    private boolean hide;
    private boolean entire;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffTooltip.class,
                "(show|reveal|:hide) %itemtypes%'[s] [entire|:additional] tool[ ]tip",
                "(show|reveal|:hide) [the] [entire|:additional] tool[ ]tip of %itemtypes%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = (Expression<FabricItemType>) exprs[0];
        hide = parseResult.hasTag("hide");
        entire = !parseResult.hasTag("additional");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (FabricItemType item : items.getArray(event)) {
            ItemStack stack = item.toStack();
            TooltipDisplay current = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
            TooltipDisplay updated;
            if (entire) {
                updated = new TooltipDisplay(hide, current.hiddenComponents());
            } else {
                SequencedSet<DataComponentType<?>> hidden = new LinkedHashSet<>(current.hiddenComponents());
                if (hide) {
                    hidden.add(DataComponents.ATTRIBUTE_MODIFIERS);
                } else {
                    hidden.remove(DataComponents.ATTRIBUTE_MODIFIERS);
                }
                updated = new TooltipDisplay(current.hideTooltip(), hidden);
            }
            stack.set(DataComponents.TOOLTIP_DISPLAY, updated);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (hide ? "hide" : "show") + " the " + (entire ? "entire" : "additional")
                + " tooltip of " + items.toString(event, debug);
    }
}
