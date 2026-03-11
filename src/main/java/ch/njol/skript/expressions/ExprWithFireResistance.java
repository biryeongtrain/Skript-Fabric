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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DamageResistant;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("With Fire Resistance")
@Description("Creates a copy of an item with or without fire resistance.")
@Example("set {_x} to diamond sword with fire resistance")
@Example("equip player with netherite helmet without fire resistance")
@Example("drop fire resistant stone at player")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public final class ExprWithFireResistance extends PropertyExpression<FabricItemType, FabricItemType> {

    static {
        Skript.registerExpression(
                ExprWithFireResistance.class,
                FabricItemType.class,
                "%itemtypes% with[:out] fire[ ]resistance",
                "fire resistant %itemtypes%"
        );
    }

    private boolean without;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<FabricItemType>) expressions[0]);
        without = parseResult.hasTag("out");
        return true;
    }

    @Override
    protected FabricItemType[] get(SkriptEvent event, FabricItemType[] source) {
        return get(source, itemType -> {
            ItemStack stack = itemType.toStack();
            if (without) {
                stack.remove(DataComponents.DAMAGE_RESISTANT);
            } else {
                stack.set(DataComponents.DAMAGE_RESISTANT, new DamageResistant(DamageTypeTags.IS_FIRE));
            }
            return new FabricItemType(stack);
        });
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr().toString(event, debug) + (without ? " without" : " with") + " fire resistance";
    }
}
