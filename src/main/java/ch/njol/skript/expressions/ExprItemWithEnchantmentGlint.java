package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item with Enchantment Glint")
@Description("Get an item with or without enchantment glint.")
@Example("set {_item with glint} to diamond with enchantment glint")
@Example("set {_item without glint} to diamond without enchantment glint")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.10")
public class ExprItemWithEnchantmentGlint extends PropertyExpression<FabricItemType, FabricItemType> {

    static {
        Skript.registerExpression(ExprItemWithEnchantmentGlint.class, FabricItemType.class,
                "%itemtypes% with[:out] [enchant[ment]] glint");
    }

    private boolean glint;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<FabricItemType>) expressions[0]);
        glint = !parseResult.hasTag("out");
        return true;
    }

    @Override
    protected FabricItemType[] get(SkriptEvent event, FabricItemType[] source) {
        return get(source, itemType -> {
            ItemStack stack = itemType.toStack();
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
            return new FabricItemType(stack);
        });
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr().toString(event, debug) + (glint ? " with" : " without") + " enchantment glint";
    }
}
