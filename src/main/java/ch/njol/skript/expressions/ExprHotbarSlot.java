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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Hotbar Slot")
@Description("The currently selected hotbar slot of a player.")
@Example("message \"%player's current hotbar slot%\"")
@Since("2.2-dev36")
public final class ExprHotbarSlot extends PropertyExpression<ServerPlayer, Slot> {

    static {
        registerDefault(ExprHotbarSlot.class, Slot.class, "[([current:currently] selected|current:current)] hotbar slot[s]", "players");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends ServerPlayer>) expressions[0]);
        return true;
    }

    @Override
    protected Slot @Nullable [] get(SkriptEvent event, ServerPlayer[] source) {
        return get(source, player -> new Slot(player.getInventory(), selectedSlot(player), 0, 0));
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Slot.class, Number.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0) {
            return;
        }
        Integer index = extractIndex(delta[0]);
        if (index == null || index < 0 || index > 8) {
            return;
        }
        for (ServerPlayer player : getExpr().getArray(event)) {
            setSelectedSlot(player, index);
        }
    }

    private @Nullable Integer extractIndex(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof Slot slot && slot.container instanceof Inventory) {
            return slot.getContainerSlot();
        }
        return null;
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "hotbar slot of " + getExpr().toString(event, debug);
    }

    private int selectedSlot(ServerPlayer player) {
        Object value = ReflectiveHandleAccess.invokeNoArg(player.getInventory(), "getSelected", "selected");
        return value instanceof Number number ? Math.clamp(number.intValue(), 0, 8) : 0;
    }

    private void setSelectedSlot(ServerPlayer player, int index) {
        ReflectiveHandleAccess.invokeSingleArg(player.getInventory(), "setSelected", index);
        ReflectiveHandleAccess.invokeSingleArg(player.getInventory(), "selected", index);
    }
}
