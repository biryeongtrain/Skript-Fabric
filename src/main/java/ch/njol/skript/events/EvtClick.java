package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.util.function.Predicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class EvtClick extends SkriptEvent {

    private static final int RIGHT = 1;
    private static final int LEFT = 2;
    private static final int ANY = RIGHT | LEFT;

    private @Nullable Literal<?> type;
    private @Nullable Literal<FabricItemType> tools;
    private int click = ANY;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtClick.class)) {
            return;
        }
        Skript.registerEvent(
                EvtClick.class,
                "[(1:right|2:left)(| |-)][mouse(| |-)]click[ing] [on %-entitydata/itemtype%] [(with|using|holding) %-itemtype%]",
                "[(1:right|2:left)(| |-)][mouse(| |-)]click[ing] (with|using|holding) %itemtype% on %entitydata/itemtype%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        click = parseResult.mark == 0 ? ANY : parseResult.mark;
        type = args[matchedPattern];
        tools = (Literal<FabricItemType>) args[1 - matchedPattern];
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Click handle)) {
            return false;
        }
        int clickedType = handle.clickType() == FabricEventCompatHandles.ClickType.LEFT ? LEFT : RIGHT;
        if ((click & clickedType) == 0) {
            return false;
        }
        if (tools != null && !tools.check(event, itemType -> matchesTool(itemType, handle.tool()))) {
            return false;
        }
        if (type == null) {
            return true;
        }
        return type.check(event, (Predicate<Object>) object -> {
            if (object instanceof EntityData<?> entityData) {
                return handle.entity() != null && entityData.isInstance(handle.entity());
            }
            if (object instanceof FabricItemType itemType) {
                if (handle.entity() == null && handle.blockState() != null) {
                    Item item = handle.blockState().getBlock().asItem();
                    return itemType.matches(new ItemStack(item, Math.max(1, itemType.amount())));
                }
                return false;
            }
            return false;
        });
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Click.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return switch (click) {
            case LEFT -> "left";
            case RIGHT -> "right";
            default -> "";
        } + "click" + (type != null ? " on " + type.toString(event, debug) : "")
                + (tools != null ? " holding " + tools.toString(event, debug) : "");
    }

    private boolean matchesTool(FabricItemType itemType, @Nullable ItemStack tool) {
        return tool != null && itemType.matches(tool);
    }
}
