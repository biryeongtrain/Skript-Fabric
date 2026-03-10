package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Cursor Slot")
@Description("The item currently carried on a player's inventory cursor.")
@Example("cursor slot of player is dirt")
@Since("2.2-dev17")
public final class ExprCursorSlot extends PropertyExpression<ServerPlayer, Slot> {

    static {
        register(ExprCursorSlot.class, Slot.class, "cursor slot", "players");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends ServerPlayer>) expressions[0]);
        return true;
    }

    @Override
    protected Slot[] get(SkriptEvent event, ServerPlayer[] source) {
        return get(source, CursorBackedSlot::new);
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "cursor slot of " + getExpr().toString(event, debug);
    }

    private static final class CursorBackedSlot extends Slot {

        private final ServerPlayer player;

        private CursorBackedSlot(ServerPlayer player) {
            super(new SimpleContainer(1), -1, 0, 0);
            this.player = player;
        }

        @Override
        public ItemStack getItem() {
            Object value = ReflectiveHandleAccess.invokeNoArg(player.containerMenu, "getCarried");
            return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            ReflectiveHandleAccess.invokeSingleArg(player.containerMenu, "setCarried", stack == null ? ItemStack.EMPTY : stack);
            setChanged();
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }
    }
}
