package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.SequencedSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Has Item Tooltips")
@Description({
        "Whether the entire or additional tooltip of an item is shown or hidden.",
        "The 'entire tooltip' is what shows to the player when they hover an item (i.e. name, lore, etc.).",
        "The 'additional tooltip' hides certain information from certain items (potions, maps, books, fireworks, and banners)."
})
@Example("send true if entire tooltip of player's tool is shown")
@Example("if additional tooltip of {_item} is hidden:")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class CondTooltip extends Condition {

    static {
        Skript.registerCondition(
                CondTooltip.class,
                "[the] [entire|:additional] tool[ ]tip[s] of %itemtypes% (is|are) (:shown|hidden)",
                "[the] [entire|:additional] tool[ ]tip[s] of %itemtypes% (isn't|is not|aren't|are not) (:shown|hidden)",
                "%itemtypes%'[s] [entire|:additional] tool[ ]tip[s] (is|are) (:shown|hidden)",
                "%itemtypes%'[s] [entire|:additional] tool[ ]tip[s] (isn't|is not|aren't|are not) (:shown|hidden)"
        );
    }

    private Expression<FabricItemType> items;
    private boolean entire;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = (Expression<FabricItemType>) exprs[0];
        entire = !parseResult.hasTag("additional");
        setNegated(parseResult.hasTag("shown") ^ (matchedPattern == 1 || matchedPattern == 3));
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (entire) {
            return items.check(event, item -> {
                TooltipDisplay display = item.toStack().get(DataComponents.TOOLTIP_DISPLAY);
                return display != null && display.hideTooltip();
            }, isNegated());
        }
        return items.check(event, item -> {
            TooltipDisplay display = item.toStack().get(DataComponents.TOOLTIP_DISPLAY);
            SequencedSet<?> hidden = display == null ? null : display.hiddenComponents();
            return hidden != null && !hidden.isEmpty();
        }, isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the " + (entire ? "entire" : "additional") + " tooltip of "
                + items.toString(event, debug) + " is " + (isNegated() ? "hidden" : "shown");
    }
}
