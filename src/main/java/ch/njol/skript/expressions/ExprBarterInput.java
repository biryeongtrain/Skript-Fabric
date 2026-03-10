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

@Name("Barter Input")
@Description("The item picked up by the piglin in a piglin bartering event.")
@Example("""
    on piglin barter:
        set {_input} to barter input
    """)
@Since("2.10")
public class ExprBarterInput extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprBarterInput.class, ItemStack.class, "[the] [piglin] barter[ing] input");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.PiglinBarter.class};
    }

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.PiglinBarter handle) || handle.input() == null) {
            return null;
        }
        return new ItemStack[]{handle.input()};
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
        return "the barter input";
    }
}
