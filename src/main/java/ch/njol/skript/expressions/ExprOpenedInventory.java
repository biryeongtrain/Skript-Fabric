package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Opened Inventory")
@Description({
        "Returns the top inventory currently opened by a player.",
        "If no separate container is open, this falls back to the player's own inventory."
})
@Example("set slot 1 of player's current inventory to diamond sword")
@Since("2.2-dev24")
public final class ExprOpenedInventory extends PropertyExpression<ServerPlayer, FabricInventory> {

    static {
        ch.njol.skript.Skript.registerExpression(
                ExprOpenedInventory.class,
                FabricInventory.class,
                "[the] (current|open|top) inventory [of %players%]",
                "%players%'[s] (current|open|top) inventory"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends ServerPlayer>) expressions[0]);
        return true;
    }

    @Override
    protected FabricInventory[] get(SkriptEvent event, ServerPlayer[] source) {
        return get(source, ExprOpenedInventory::resolveInventory);
    }

    @Override
    public Class<? extends FabricInventory> getReturnType() {
        return FabricInventory.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "current inventory" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(event, debug));
    }

    private static FabricInventory resolveInventory(ServerPlayer player) {
        Object menu = player.containerMenu;
        Container playerInventory = player.getInventory();
        if (menu == null) {
            return new FabricInventory(playerInventory, MenuType.GENERIC_9x5, player.getName(), player);
        }
        Container top = findTopContainer(menu, playerInventory);
        MenuType<?> menuType = resolveMenuType(menu);
        Component title = resolveTitle(menu);
        Object holder = top == playerInventory ? player : menu;
        return new FabricInventory(top, menuType, title, holder);
    }

    private static Container findTopContainer(Object menu, Container playerInventory) {
        List<Slot> slots = extractSlots(menu);
        if (slots.isEmpty()) {
            return playerInventory;
        }
        Map<Container, Integer> counts = new IdentityHashMap<>();
        for (Slot slot : slots) {
            counts.merge(slot.container, 1, Integer::sum);
        }
        Container selected = null;
        int selectedCount = -1;
        for (Map.Entry<Container, Integer> entry : counts.entrySet()) {
            if (entry.getKey() == playerInventory) {
                continue;
            }
            if (entry.getValue() > selectedCount) {
                selected = entry.getKey();
                selectedCount = entry.getValue();
            }
        }
        return selected != null ? selected : playerInventory;
    }

    private static List<Slot> extractSlots(Object menu) {
        for (Field field : menu.getClass().getFields()) {
            if (!List.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                Object value = field.get(menu);
                if (!(value instanceof List<?> list)) {
                    continue;
                }
                List<Slot> slots = new ArrayList<>();
                for (Object element : list) {
                    if (element instanceof Slot slot) {
                        slots.add(slot);
                    }
                }
                if (!slots.isEmpty() || list.isEmpty()) {
                    return slots;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return List.of();
    }

    private static MenuType<?> resolveMenuType(Object menu) {
        for (String methodName : new String[]{"getType", "getMenuType"}) {
            try {
                Method method = menu.getClass().getMethod(methodName);
                Object value = method.invoke(menu);
                if (value instanceof MenuType<?> type) {
                    return type;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return MenuType.GENERIC_9x1;
    }

    private static Component resolveTitle(Object menu) {
        for (String methodName : new String[]{"getTitle", "title"}) {
            try {
                Method method = menu.getClass().getMethod(methodName);
                Object value = method.invoke(menu);
                if (value instanceof Component component) {
                    return component;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return Component.empty();
    }
}
