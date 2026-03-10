package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import net.minecraft.core.component.DataComponents;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Fire Resistant")
@Description("Checks whether an item is fire resistant.")
@Example("if player's tool is fire resistant:")
@Example("if {_items::*} aren't resistant to fire:")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class CondIsFireResistant extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsFireResistant.class, "(fire resistant|resistant to fire)", "itemtypes");
    }

    @Override
    public boolean check(FabricItemType item) {
        return item.toStack().get(DataComponents.DAMAGE_RESISTANT) != null;
    }

    @Override
    public String getPropertyName() {
        return "fire resistant";
    }
}
