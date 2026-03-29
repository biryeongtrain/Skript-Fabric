package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Fire Resistant")
@Description("Makes items fire resistant.")
@Example("make player's tool fire resistant")
@Example("make {_items::*} not resistant to fire")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class EffFireResistant extends Effect {

    private static boolean registered;

    private Expression<FabricItemType> items;
    private boolean not;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffFireResistant.class, "make %itemtypes% [:not] (fire resistant|resistant to fire)");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = (Expression<FabricItemType>) exprs[0];
        not = parseResult.hasTag("not");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (FabricItemType item : items.getArray(event)) {
            ItemStack stack = item.toStack();
            if (not) {
                stack.remove(DataComponents.DAMAGE_RESISTANT);
            } else {
                stack.set(DataComponents.DAMAGE_RESISTANT, new net.minecraft.world.item.component.DamageResistant(
                        net.minecraft.core.HolderSet.emptyNamed(new net.minecraft.core.HolderOwner<net.minecraft.world.damagesource.DamageType>() {}, DamageTypeTags.IS_FIRE)));
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + items.toString(event, debug) + (not ? " not" : "") + " fire resistant";
    }
}
