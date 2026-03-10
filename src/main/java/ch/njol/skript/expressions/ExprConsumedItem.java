package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Consumed Item")
@Description("Represents the item consumed within an entity shoot bow or consume event.")
@Example("""
    on consume:
        set {_item} to consumed item
    """)
@Since("2.11")
public class ExprConsumedItem extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprConsumedItem.class, ItemStack.class, "[the] consumed item");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.EntityShootBow.class, FabricEventCompatHandles.Item.class};
    }

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        if (event.handle() instanceof FabricEventCompatHandles.EntityShootBow handle) {
            return handle.consumable() == null ? null : new ItemStack[]{handle.consumable()};
        }
        if (event.handle() instanceof FabricEventCompatHandles.Item handle
                && handle.action() == FabricEventCompatHandles.ItemAction.CONSUME
                && handle.itemStack() != null) {
            return new ItemStack[]{handle.itemStack()};
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the consumed item";
    }
}
