package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.SecCreateGui;
import ch.njol.util.Kleenean;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Formats a GUI slot with an item inside a "create gui" section.
 */
public class EffFormatGuiSlot extends Effect {

    public static void register() {
        Skript.registerEffect(EffFormatGuiSlot.class,
                "format gui slot %number% with %itemtype%",
                "format next gui slot with %itemtype%"
        );
    }

    private boolean isNextSlot;
    private @Nullable Expression<Number> slotExpr;
    private Expression<FabricItemType> itemExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        isNextSlot = matchedPattern == 1;
        if (!isNextSlot) {
            slotExpr = (Expression<Number>) exprs[0];
            itemExpr = (Expression<FabricItemType>) exprs[1];
        } else {
            itemExpr = (Expression<FabricItemType>) exprs[0];
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        SimpleGui gui = SecCreateGui.getCurrentGui();
        if (gui == null) {
            return;
        }

        FabricItemType itemType = itemExpr.getSingle(event);
        if (itemType == null) {
            return;
        }

        ItemStack stack = itemType.toStack();
        int slot;
        if (isNextSlot) {
            slot = SecCreateGui.getAndIncrementNextSlot();
        } else {
            Number slotNumber = slotExpr != null ? slotExpr.getSingle(event) : null;
            if (slotNumber == null) {
                return;
            }
            slot = slotNumber.intValue();
            SecCreateGui.setNextSlotIndex(slot + 1);
        }

        if (slot >= 0 && slot < gui.getVirtualSize()) {
            gui.setSlot(slot, new GuiElement(stack, GuiElementInterface.EMPTY_CALLBACK));
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (isNextSlot) {
            return "format next gui slot with " + itemExpr.toString(event, debug);
        }
        return "format gui slot " + (slotExpr != null ? slotExpr.toString(event, debug) : "?") + " with " + itemExpr.toString(event, debug);
    }
}
