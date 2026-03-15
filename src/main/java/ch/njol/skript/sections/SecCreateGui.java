package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.placeholder.SkriptTextPlaceholders;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.List;

/**
 * Creates a GUI using SGui's SimpleGui.
 * The section body is executed to format slots before the GUI is opened.
 */
public class SecCreateGui extends EffectSection {

    private static volatile @Nullable SimpleGui currentGui;
    private static int nextSlotIndex;

    public static void register() {
        Skript.registerSection(SecCreateGui.class,
                "create gui with virtual chest inventory with %number% row[s] named %string%",
                "create gui with virtual chest inventory with %number% row[s]"
        );
    }

    private Expression<Number> rows;
    private @Nullable Expression<String> title;
    private @Nullable Trigger trigger;

    public static @Nullable SimpleGui getCurrentGui() {
        return currentGui;
    }

    public static int getAndIncrementNextSlot() {
        return nextSlotIndex++;
    }

    public static void setNextSlotIndex(int index) {
        nextSlotIndex = index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
                        @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
        rows = (Expression<Number>) exprs[0];
        if (matchedPattern == 0) {
            title = (Expression<String>) exprs[1];
        }

        if (sectionNode != null) {
            trigger = SectionUtils.loadLinkedCode("create gui", (beforeLoading, afterLoading)
                    -> loadCode(sectionNode, "create gui", beforeLoading, afterLoading));
            return trigger != null;
        }

        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        Number rowsValue = rows.getSingle(event);
        if (rowsValue == null) {
            return super.walk(event, false);
        }

        int numRows = Math.clamp(rowsValue.intValue(), 1, 6);
        MenuType<?> menuType = switch (numRows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };

        // We need a player to create the GUI, but during section execution we don't open it yet.
        // Create GUI with the event's player context.
        if (event.player() == null) {
            return super.walk(event, false);
        }

        SimpleGui gui = new SimpleGui(menuType, event.player(), false);

        Component titleComponent = Component.empty();
        if (title != null) {
            String titleStr = title.getSingle(event);
            if (titleStr != null && !titleStr.isBlank()) {
                titleComponent = SkriptTextPlaceholders.resolveComponent(titleStr, event);
            }
        }
        gui.setTitle(titleComponent);

        currentGui = gui;
        nextSlotIndex = 0;

        if (trigger != null) {
            SkriptEvent sectionEvent = new SkriptEvent(null, event.server(), event.level(), event.player());
            Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
        }

        // Keep currentGui set so "open last gui" can access it after section completes
        // It will be cleared when a new GUI is created

        return super.walk(event, false);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String titleStr = title != null ? " named " + title.toString(event, debug) : "";
        return "create gui with virtual chest inventory with " + rows.toString(event, debug) + " rows" + titleStr;
    }
}
