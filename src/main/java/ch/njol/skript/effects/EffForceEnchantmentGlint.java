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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Force Enchantment Glint")
@Description("Forces the items to glint or not, or removes its existing enchantment glint enforcement.")
@Example("force {_items::*} to glint")
@Example("force the player's tool to stop glinting")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.10")
public final class EffForceEnchantmentGlint extends Effect {

    private static boolean registered;

    private Expression<FabricItemType> itemTypes;
    private int pattern;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffForceEnchantmentGlint.class,
                "(force|make) %itemtypes% [to] [start] glint[ing]",
                "(force|make) %itemtypes% [to] (not|stop) glint[ing]",
                "(clear|delete|reset) [the] enchantment glint override of %itemtypes%",
                "(clear|delete|reset) %itemtypes%'s enchantment glint override"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        itemTypes = (Expression<FabricItemType>) expressions[0];
        pattern = matchedPattern;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        itemTypes.changeInPlace(event, itemType -> {
            ItemStack stack = itemType.toStack();
            Boolean glint = switch (pattern) {
                case 0 -> Boolean.TRUE;
                case 1 -> Boolean.FALSE;
                default -> null;
            };
            if (glint == null) {
                stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            } else {
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
            }
            return new FabricItemType(stack);
        });
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (pattern > 1) {
            return "clear the enchantment glint override of " + itemTypes.toString(event, debug);
        }
        return "force " + itemTypes.toString(event, debug) + " to " + (pattern == 0 ? "start" : "stop") + " glinting";
    }
}
