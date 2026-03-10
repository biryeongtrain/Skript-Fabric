package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Equip")
@Description({
        "Equips or unequips an entity with the given itemtypes (usually armor).",
        "This effect will replace any armor that the entity is already wearing."
})
@Example("equip player with diamond helmet")
@Example("equip player with diamond leggings, diamond chestplate, and diamond boots")
@Example("unequip diamond chestplate from player")
@Example("unequip player's armor")
@Since({
        "1.0, 2.7 (multiple entities, unequip), 2.10 (wolves)",
        "2.12.1 (happy ghasts)"
})
public class EffEquip extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private @Nullable Expression<FabricItemType> itemTypes;
    private boolean equip = true;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffEquip.class,
                "equip [%livingentities%] with %itemtypes%",
                "make %livingentities% wear %itemtypes%",
                "unequip %itemtypes% [from %livingentities%]",
                "unequip %livingentities%'[s] (armo[u]r|equipment)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        if (matchedPattern == 0 || matchedPattern == 1) {
            entities = (Expression<LivingEntity>) exprs[0];
            itemTypes = (Expression<FabricItemType>) exprs[1];
        } else if (matchedPattern == 2) {
            itemTypes = (Expression<FabricItemType>) exprs[0];
            entities = (Expression<LivingEntity>) exprs[1];
            equip = false;
        } else {
            entities = (Expression<LivingEntity>) exprs[0];
            itemTypes = null;
            equip = false;
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (equip) {
            return "equip " + entities.toString(event, debug) + " with " + itemTypes.toString(event, debug);
        }
        if (itemTypes != null) {
            return "unequip " + itemTypes.toString(event, debug) + " from " + entities.toString(event, debug);
        }
        return "unequip " + entities.toString(event, debug) + "'s equipment";
    }
}
